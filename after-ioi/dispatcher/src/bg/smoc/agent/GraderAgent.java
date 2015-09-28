package bg.smoc.agent;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.Socket;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import kr.or.ioi2002.RMIServer.Job;
import kr.or.ioi2002.RMIServer.Syslog;
import kr.or.ioi2002.RMIServer.TempFile;
import kr.or.ioi2002.RMIServer.Job.JobType;
import kr.or.ioi2002.RMIServer.agent.AgentException;
import kr.or.ioi2002.RMIServer.agent.LogAgentIO;
import bg.smoc.model.Contest;
import bg.smoc.model.GradeProto;
import bg.smoc.model.Task;
import bg.smoc.model.GradeProto.Grade;
import bg.smoc.model.GradeProto.GradeResult;
import bg.smoc.model.GradeProto.Grade.Builder;
import bg.smoc.model.manager.ContestManager;
import bg.smoc.model.manager.GraderManager;

// public: accessed from Contest to generate post data
public class GraderAgent extends Thread {
    private static final int GRADE_PROCESS_TIMEOUT = 600 * 1000 * 2; // 1200 sec

    private static final int SUBMIT_PROCESS_TIMEOUT = GRADE_PROCESS_TIMEOUT; // 1200
    // sec

    private static final int TEST_PROCESS_TIMEOUT = 600000; // 600 sec

    private GraderManager graderManager;

    private ContestManager contestManager;

    public GraderAgent(GraderManager graderManager, ContestManager contestManager, Socket socket) {
        this.socket = socket;
        setDaemon(true);
        this.graderManager = graderManager;
        this.contestManager = contestManager;
        start();
    }

    public void postMessageEntry() {
        currentJob = null;
        graderManager.OnMsgGaEntry(this);
    }

    public void postMessageFail() {
        graderManager.OnMsgGaFail(this, resetCurrentJob());
    }

    /**
     * Returns the Job which until this call was current. And resets the current
     * job to null.
     * 
     * @return the just finished job
     */
    private Job resetCurrentJob() {
        Job finishedJob = currentJob;
        currentJob = null;
        return finishedJob;
    }

    public void assureNoCurrentJob() {
        currentJob = null;
    }

    protected void doJob() throws IOException, AgentException {
        if ("SETUP".equals(currentJob.getType())) {
            // Contest contest =
            // contestManager.getContest(currentJob.getContestId());
            // for (Task task : contest.getTasks()) {
            // writeLine("DATA " + task.getName());
            // sendFile(contestManager.updateTestGroupsFile(contest.getId(),
            // task));
            // TODO verification code should go here.
            // }
            currentJob = null;
        } else {
            if (!(JobType.GRADE == currentJob.getType() || JobType.SUBMIT == currentJob.getType() || JobType.TEST == currentJob
                    .getType())) {
                Syslog.log("!Discarding Job, GraderAgent:"
                        + " doJob: Job invaild, type:"
                        + (currentJob.getType() != null ? currentJob.getType() : "null"));
                currentJob = null;
                return;
            }

            sendSourceCode();
            readResponse();
            handleReturnedData();
            sendDoneMessage();
        }
    }

    private int getTimeoutForJobType(JobType jobType) throws AgentException {
        if (JobType.GRADE == currentJob.getType())
            return GRADE_PROCESS_TIMEOUT;
        if (JobType.SUBMIT == currentJob.getType())
            return SUBMIT_PROCESS_TIMEOUT;
        if (JobType.TEST == currentJob.getType())
            return TEST_PROCESS_TIMEOUT;

        throw new AgentException("Invalid job type - not supported by this agent.");
    }

    private void sendSourceCode() throws IOException, AgentException {
        if (currentJob.getType() == JobType.TEST) {
            writeLine("REQUEST TEST");
            sendProtobuf();
            sendFile(currentJob.src);
            if (currentJob.stdin != null)
                sendFile(currentJob.stdin);
            else
                sendFileNull();
        } else {
            writeLine("REQUEST GRADE");
            sendProtobuf();
            sendFile(currentJob.src);
        }

        // writeLine("REQUEST "
        // + ((currentJob.getType() == JobType.TEST) ?
        // currentJob.getType().toString()
        // : "GRADE")
        // + " "
        // + currentJob.getTask()
        // + " "
        // + currentJob.getLanguage());
        // sendFile(currentJob.src);
        // if (JobType.TEST.equals(currentJob.getType())) {
        // if (currentJob.stdin != null)
        // sendFile(currentJob.stdin);
        // else
        // sendFileNull();
        // }
        os.flush();
    }

    private void sendProtobuf() throws AgentException, IOException {
        Builder protoBuilder = GradeProto.Grade.newBuilder();
        protoBuilder.setTaskName(currentJob.getTask());
        protoBuilder.setLanguage(currentJob.getLanguage());
        Task task = getTask();
        if (currentJob.getType() != JobType.TEST) {
            if (task.getType() == Task.PROBLEM_TYPE_OUTPUT) {
                try {
                    protoBuilder.addTestIndexes(Integer.parseInt(currentJob.getTestIndex()));
                } catch (NumberFormatException e) {
                    throw new AgentException("Invalid test index.");
                }
            } else {
                Contest contest = extractContest();
                protoBuilder.addAllTestIndexes(task.getTestIndexesByJobType(currentJob.getType(),
                        !currentJob.isNotGradeFeedback(),
                        contest.isFeedbackOn()));
            }
            for (BigDecimal points : task.getTestsPoints(protoBuilder.getTestIndexesList())) {
                protoBuilder.addMaxPoints(points.toString());
            }
        }
        protoBuilder.setExplicitGrade(currentJob.getType() == JobType.GRADE);
        protoBuilder.setTaskType(task.getType());
        protoBuilder.setTimeLimit(task.getTimeLimit());
        protoBuilder.setMemoryLimit(task.getMemoryLimit());
        protoBuilder.setOutputLimit(task.getOutputLimit());
        protoBuilder.setTestsCount(task.getNumberOfTests());

        Grade gradeProto = protoBuilder.build();
        DataOutputStream dataOutput = new DataOutputStream(os);
        dataOutput.writeInt(gradeProto.getSerializedSize());
        gradeProto.writeTo(dataOutput);
        // we will continue work with os, so do not close dataOutput.
    }

    private Task getTask() throws AgentException {
        if (currentJob == null) {
            throw new AgentException("No current job!");
        }
        Task task = contestManager.getTaskByName(currentJob.getContestId(), currentJob.getTask());
        if (task == null) {
            throw new AgentException("Cannot read task"
                    + currentJob.getTask()
                    + " in contest:"
                    + currentJob.getContestId());
        }
        return task;
    }

    private Contest extractContest() throws AgentException {
        if (currentJob == null) {
            throw new AgentException("No current job!");
        }
        Contest contest = contestManager.getContest(currentJob.getContestId());
        if (contest == null) {
            throw new AgentException("Cannot read contest:" + currentJob.getContestId());
        }
        return contest;
    }

    private void readResponse() throws AgentException, IOException {
        socket.setSoTimeout(getTimeoutForJobType(currentJob.getType()));
        String command = readLine();
        if (command == null)
            throw new AgentException("RESULT expected, but connection terminated; " + command);
        StringTokenizer tokenizer = new StringTokenizer(command, " ");
        try {
            if (!tokenizer.nextToken().equals("RESULT"))
                throw new AgentException("RESULT expected; " + command);
            assertHasMoreTokens(tokenizer, command);
            String expectedType = (currentJob.getType() == JobType.TEST) ? JobType.TEST.toString()
                    : JobType.GRADE.toString();
            if (!tokenizer.nextToken().equals(expectedType))
                throw new AgentException(currentJob.getType() + " expected; " + command);
            assertHasMoreTokens(tokenizer, command);
            String result = tokenizer.nextToken();
            if (result.equals("OK") || result.equals("FAIL"))
                currentJob.result = result;
            else
                throw new AgentException("OK or FAIL expected;" + command);
            assertHasMoreTokens(tokenizer, command);
            currentJob.setTask(tokenizer.nextToken());
        } catch (NoSuchElementException e) {
            throw new AgentException("NoSuchElementException: error parsing RESULT");
        }

        currentJob.setGradeResult(receiveGradeResultProto());
    }

    private GradeResult receiveGradeResultProto() throws IOException, AgentException {
        byte proto[] = readBytes(receiveKeyAndLength());
        GradeResult result = GradeProto.GradeResult.parseFrom(proto);
        return result;
    }

    private void assertHasMoreTokens(StringTokenizer t, String command) throws AgentException {
        if (!t.hasMoreTokens())
            throw new AgentException("invalid RESULT format from grading machine; " + command);
    }

    private void handleReturnedData() throws IOException, AgentException {
        assert JobType.SETUP != currentJob.getType();
        if (JobType.TEST == currentJob.getType())
            return;

        // recv grader_log
        currentJob.log = recvBytes();

        if (currentJob.getUserid() == null || currentJob.getTask() == null)
            throw new AgentException(
                    "!currentJob.userid == null || currentJob.task == null: doGrade");

        // store grader-generated files to the server
        // save csv and log as file
        TempFile tmpfilelog = TempFile.createFromByteArray(currentJob.log);
        saveGradeResultFile(currentJob.getContestId(),
                currentJob.getUserid(),
                currentJob.getTask(),
                tmpfilelog,
                currentJob.getTask() + "." + currentJob.getType() + ".grader.log");
        TempFile tmpfilecsv = TempFile.createFromByteArray(currentJob.getGradeResult().toString()
                .getBytes("UTF-8"));
        saveGradeResultFile(currentJob.getContestId(),
                currentJob.getUserid(),
                currentJob.getTask(),
                tmpfilecsv,
                currentJob.getTask() + "." + currentJob.getType() + ".grader.csv");

        // retreive filelist and recv files
        byte[] abyFilelist = recvBytes();
        String strFilelist = new String(abyFilelist);
        StringTokenizer tFile = new StringTokenizer(strFilelist, "\n");
        try {
            String strFilename = null;
            while (tFile.hasMoreTokens() && !(strFilename = tFile.nextToken().trim()).equals("")) {
                TempFile tmpFile = recvFile();
                saveGradeResultFile(currentJob.getContestId(), currentJob.getUserid(), currentJob
                        .getTask(), tmpFile, strFilename);
            }
        } catch (NoSuchElementException e) {
            throw new AgentException("!NoSuchElementException: doGrade");
        }
    }

    private void sendDoneMessage() {
        currentJob.gmid = getIP().toString();
        Job job = resetCurrentJob();
        graderManager.releaseMachine(this);

        // It is important that currentJob is null-ified before a call to the
        // gradeManager occurs, otherwise a putback occurs and this go awry.
        if (JobType.GRADE == job.getType())
            graderManager.OnMsgGradeDone(job);
        else if (JobType.SUBMIT == job.getType())
            graderManager.OnMsgSubmitDone(job);
        else if (JobType.TEST == job.getType())
            graderManager.OnMsgTestDone(job);
    }

    private void saveGradeResultFile(String contestId, String userid, String task, TempFile tmp,
            String strFilename) {
        if (userid == null || task == null || tmp == null || strFilename == null) {
            Syslog.log("saveGradeResultFile: (userid == null || task == null "
                    + "|| tmp == null || strFilename == null)");
            return;
        }

        File taskpath = graderManager.getTaskPath(contestId, userid, task);
        if (!taskpath.isDirectory()) {
            if (!taskpath.mkdirs()) {
                Syslog.log("!saveGradeResultFile: " + "taskpath.mkdirs() returned false");
                return;
            }
        }

        File file = new File(taskpath, strFilename);
        // overwrite previous
        if (file.exists()) {
            if (!file.delete())
                Syslog.log("!delete failed: saveGradeResultFile: " + file);
        }

        if (!tmp.makePermanent(file)) {
            Syslog.log("!saveGradeResultFile: " + "TempFile.makePermanent returned false: " + file);
        }
    }

    protected static long FILESIZE_LIMIT = 100 * 1024 * 1024;
    protected static int FILE_TRANSFER_INTEGRITY_KEY = 0x37fd8a20;

    protected String agentVersion = null;
    protected Job currentJob = null;
    protected BufferedInputStream is = null;
    protected BufferedOutputStream os = null;
    protected Socket socket = null;

    protected LinkedList<String> contestsToBeUpdated = new LinkedList<String>();

    protected Integer cs = new Integer(0); // critical section for synchronized

    public String getVersion() {
        return agentVersion;
    }

    public InetAddress getIP() {
        return socket.getInetAddress();
    }

    public Job getJob() {
        return currentJob;
    }

    public boolean assignJob(Job job) {
        if (this.currentJob != null) {
            LogAgentIO
                    .log("Could not assign job, previous job: "
                            + ((currentJob.getContestId() != null) ? "Contest:"
                                    + currentJob.getContestId() : "No contest;")
                            + ((currentJob.getUserid() != null) ? "Login:" + currentJob.getUserid()
                                    : "No login;")
                            + ((currentJob.getTask() != null) ? "Task:" + currentJob.getTask()
                                    : "No task;")
                            + ((currentJob.result != null) ? "Result:" + currentJob.result
                                    : "No result"));
            return false;
        }
        currentJob = job;
        synchronized (cs) {
            cs.notifyAll();
        }
        return true;
    }

    protected void waitforJob() throws InterruptedException, IOException {
        while (currentJob == null) {
            synchronized (cs) {
                if (currentJob == null)
                    cs.wait(10000); // Keep Alive 10000 sec
            }
            os.write('\n');
            os.flush();
        }
    }

    public void run() {
        try {
            is = new BufferedInputStream(socket.getInputStream());
            os = new BufferedOutputStream(socket.getOutputStream());

            String command = null;

            // expect READY in 10 sec
            socket.setSoTimeout(10000);
            command = readLine();
            if (command.length() < 5)
                throw new AgentException("READY expected; " + command);
            if (!command.substring(0, 5).equals("READY"))
                throw new AgentException("READY expected; " + command);
            if (command.length() > 5) // version information available
            {
                agentVersion = command.substring(5).trim();
            }
            writeLine("ACK");
            os.flush();

            postMessageEntry();

            while (true) {
                if (currentJob == null)
                    waitforJob();
                doJob();
            }

        } catch (AgentException e) {
            Syslog.log(e.toString());
        } catch (IOException e) {
            LogAgentIO.log(e.toString());
        } catch (InterruptedException e) {
            Syslog.log(e.toString());
        }

        try {
            is.close();
            os.close();
            socket.close();
        } catch (IOException e) {
            Syslog.log(e.toString());
        }

        postMessageFail();

        System.out.println("IOI Server: Agent Disconnected [" + getIP() + "]");
    }

    protected void sendFile(File file) throws IOException {
        long length = file.length();
        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));

        ByteArrayOutputStream baos = new ByteArrayOutputStream(8);
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeInt(FILE_TRANSFER_INTEGRITY_KEY); // keyvalue
        dos.writeInt((int) length);
        byte[] bHeader = baos.toByteArray();
        dos.close();
        baos.close();

        os.write(bHeader);
        byte[] bBuffer = new byte[1024];
        int iTotal = 0;
        int iRead;
        while ((iRead = bis.read(bBuffer)) > 0) {
            os.write(bBuffer, 0, iRead);
            iTotal += iRead;
        }
        bis.close();
    }

    protected void sendFileNull() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(8);
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeInt(FILE_TRANSFER_INTEGRITY_KEY); // keyvalue
        dos.writeInt(0);
        byte[] bHeader = baos.toByteArray();
        dos.close();
        baos.close();

        os.write(bHeader);
    }

    protected TempFile recvFile() throws IOException, AgentException {
        byte[] bHeader = new byte[8];
        int toRead = 8;
        while (toRead > 0) {
            int iRead = is.read(bHeader, 8 - toRead, toRead);
            if (iRead < 0)
                throw new AgentException("closed during recvFile");
            toRead -= iRead;
        }
        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(bHeader));
        int keyvalue = dis.readInt();
        if (keyvalue != FILE_TRANSFER_INTEGRITY_KEY)
            throw new AgentException("recvFile: Wrong Integrity Key");
        int length = dis.readInt();
        dis.close();

        if (length > FILESIZE_LIMIT)
            throw new AgentException("recvFile: FILESIZE_LIMIT("
                    + String.valueOf(FILESIZE_LIMIT)
                    + ") exceeded, length="
                    + String.valueOf(length));

        return TempFile.createFromStream(is, length);
    }

    protected byte[] recvBytes() throws IOException, AgentException {
        int length = receiveKeyAndLength();

        if (length < 0 || length > FILESIZE_LIMIT)
            throw new AgentException("recvBytes: FILESIZE_LIMIT("
                    + String.valueOf(FILESIZE_LIMIT)
                    + ") exceeded, length="
                    + String.valueOf(length));

        byte[] buffer = new byte[length];
        int iRead = 0;
        int iOffset = 0;
        while (iOffset < length && (iRead = is.read(buffer, iOffset, length - iOffset)) > 0) {
            iOffset += iRead;
        }

        return buffer;
    }

    private int receiveKeyAndLength() throws IOException, AgentException {
        byte[] bHeader = readBytes(8);
        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(bHeader));
        int keyvalue = dis.readInt();
        if (keyvalue != FILE_TRANSFER_INTEGRITY_KEY)
            throw new AgentException("recvBytes: Wrong Integrity Key");
        int length = dis.readInt();
        dis.close();
        return length;
    }

    private byte[] readBytes(int size) throws IOException, AgentException {
        byte[] bHeader = new byte[size];
        int toRead = size;
        while (toRead > 0) {
            int iRead = is.read(bHeader, size - toRead, toRead);
            if (iRead < 0)
                throw new AgentException("closed during recvBytes");
            toRead -= iRead;
        }
        return bHeader;
    }

    protected String readLine() throws IOException, AgentException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(80);
        int iRead = -1;
        do {
            iRead = is.read();
            if (iRead < 0)
                throw new AgentException("closed during readLine");
            baos.write(iRead);
        } while (iRead != '\n');
        return baos.toString().trim();
    }

    protected void writeLine(String line) throws IOException {
        byte[] bLine = line.getBytes();
        os.write(bLine);
        os.write('\n');
    }

    public void setNeedsUpdate(String id) {
        contestsToBeUpdated.add(id);
    }
}
