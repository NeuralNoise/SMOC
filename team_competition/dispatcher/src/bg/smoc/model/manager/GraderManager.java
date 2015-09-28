/**
 * 
 */
package bg.smoc.model.manager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;

import kr.or.ioi2002.RMIServer.Job;
import kr.or.ioi2002.RMIServer.LogGrade;
import kr.or.ioi2002.RMIServer.LogSubmit;
import kr.or.ioi2002.RMIServer.LogTest;
import kr.or.ioi2002.RMIServer.RecordManager;
import kr.or.ioi2002.RMIServer.Syslog;
import kr.or.ioi2002.RMIServer.TempFile;
import kr.or.ioi2002.RMIServer.Util;
import kr.or.ioi2002.RMIServer.Job.JobType;
import kr.or.ioi2002.RMIServer.agent.LogGraderAgent;
import kr.or.ioi2002.RMIServer.agent.TCPListner;
import bg.smoc.agent.GraderAgent;
import bg.smoc.model.Contest;
import bg.smoc.model.Task;
import bg.smoc.model.TestGroup;

/**
 * @author tsvetan.bogdanov@gmail.com
 * 
 */
public class GraderManager extends TCPListner {

    private static final int MAX_JOB_RETRY = 1;

    public static final String DIR_GRADE = "GRADE";

    protected MachineQueueManager machineQueueManager;

    protected HashMap<GraderAgent, String[]> obj2astr = new HashMap<GraderAgent, String[]>(); // [0]description

    private RecordManager recordManager = null;

    private File fileRoot = null;

    private GradingQueue gradingQueueManager;

    private ManagerMediator mediator;

    public GraderManager(int gradingPort, String workingDirectory) {
        super(gradingPort);
        recordManager = new RecordManager();
        gradingQueueManager = new GradingQueue(workingDirectory);
        machineQueueManager = new MachineQueueManager();
        this.fileRoot = new File(workingDirectory, "USERS");
    }

    public boolean[] hasSubmission(Contest contest, String login, Task task) {
        boolean[] res = new boolean[task.getNumberOfTests()];
        if (task == null || task.getType() == null || task.getType() != Task.PROBLEM_TYPE_OUTPUT) {
            File srcfile = null;
            if (task != null)
                srcfile = mediator.getSourceCode(contest, login, task.getName());

            for (int i = 0; i < task.getNumberOfTests(); ++i)
                res[i] = (srcfile != null);
        } else {
            for (int i = 0; i < task.getNumberOfTests(); ++i) {
                File srcfile = mediator.getSourceCode(contest, login, task.getNameAppenedTest(i));
                res[i] = (srcfile != null);
            }
        }
        return res;
    }

    public List<String> getResult(String contestId, String login, Task task) {
        List<String> result = new ArrayList<String>();

        if (task.getType() == null || task.getType() != Task.PROBLEM_TYPE_OUTPUT) {
            String raw_data = getResultFile(contestId, login, task.getName());
            StringTokenizer tokenizer = new StringTokenizer(raw_data, ",");
            tokenizer.nextToken(); // skip task name
            if (!tokenizer.hasMoreTokens()) {
                for (int i = 0; i < task.getNumberOfTests(); ++i) {
                    result.add("!");
                    return result;
                }
            }

            tokenizer.nextToken(); // skip language
            tokenizer.nextToken();
            tokenizer.nextToken();
            while (tokenizer.hasMoreTokens()) {
                tokenizer.nextToken(); // skip test number
                tokenizer.nextToken();
                result.add(tokenizer.nextToken());
                tokenizer.nextToken();
                tokenizer.nextToken();
                tokenizer.nextToken();
            }
        } else {
            for (int i = 0; i < task.getNumberOfTests(); ++i) {
                String raw_data = getResultFile(contestId, login, task.getNameAppenedTest(i)
                        .toLowerCase());
                StringTokenizer tokenizer = new StringTokenizer(raw_data, ",");
                tokenizer.nextToken(); // skip task name
                if (tokenizer.hasMoreTokens()) {
                    tokenizer.nextToken(); // skip language
                    tokenizer.nextToken();
                    tokenizer.nextToken();
                    tokenizer.nextToken(); // skip test number
                    tokenizer.nextToken();
                    result.add(tokenizer.nextToken());
                } else {
                    result.add("!");
                }
            }
        }
        return result;
    }

    private String getResultFile(String contestId, String userid, String task) {
        if (userid == null || task == null) {
            return "Not Graded(invalid userid or task";
        }

        File taskpath = getTaskPath(contestId, userid, task);
        File grade_output = new File(taskpath, task + ".grader.csv");
        if (!grade_output.exists()) {
            return "Not Graded";
        } else {
            try {
                BufferedReader in = new BufferedReader(new FileReader(grade_output));
                String output = in.readLine();
                in.close();
                return output;
            } catch (IOException e) {
                return "Not Graded(Problem found)";
            }
        }
    }

    private String getCompilerOutput(String contestId, String userid, String task) {
        if (userid == null || task == null) {
            return "System error! No ouput from compilation.";
        }

        File taskpath = getTaskPath(contestId, userid, task);
        File grade_output = new File(taskpath, task + ".compiler.out");
        if (!grade_output.exists()) {
            return "System error! No ouput from compilation.";
        } else {
            try {
                BufferedReader in = new BufferedReader(new FileReader(grade_output));
                StringBuffer result = new StringBuffer();
                String line = null;
                while ((line = in.readLine()) != null) {
                    result.append(line);
                    result.append("\n");
                }
                in.close();
                return result.toString();
            } catch (IOException e) {
                return "Not Graded(Problem found)";
            }
        }
    }

    private String getFullGraderCsv(String contestId, String userid, String task) {
        if (userid == null || task == null) {
            return "System error! No ouput from compilation.";
        }

        File taskpath = getTaskPath(contestId, userid, task);
        File grade_output = new File(taskpath, task + ".grader.csv");
        if (!grade_output.exists()) {
            return "System error! No ouput from compilation.";
        } else {
            try {
                BufferedReader in = new BufferedReader(new FileReader(grade_output));
                StringBuffer result = new StringBuffer();
                String line = null;
                while ((line = in.readLine()) != null) {
                    result.append(line);
                    result.append("\n");
                }
                in.close();
                return result.toString();
            } catch (IOException e) {
                return "Not Graded(Problem found)";
            }
        }
    }

    public boolean grade(Contest contest, String userId, String task) {
        File srcfile = mediator.getSourceCode(contest, userId, task);
        if (srcfile == null)
            return false;

        grade(contest.getId(),
                userId,
                task,
                mediator.getSourceCodeLanguage(contest, userId, task),
                srcfile);
        LogGrade.log(userId + ",START");
        return true;
    }

    public void grade(String contestId, String userid, String task, String language, File tmp) {
        Job job = new Job();
        job.setContestId(contestId);
        job.setUserid(userid);
        job.setType(JobType.GRADE);
        job.setTask(task);
        job.setLanguage(language);
        job.src = tmp;
        gradingQueueManager.push(job, null);
        dispatchJob();
    }

    public void submit(String contestId, String userId, Task task, String language, File tmp,
            String sourceFileName, JobRemovalNotified notified) {
        Job job = new Job();
        job.setContestId(contestId);
        job.setUserid(userId);
        if (task.isFeedbackEnabled()) {
            job.setType(JobType.FEEDBACK);
        } else {
            job.setType(JobType.SUBMIT);
        }
        // TODO: remove HACK
        if (task.getType() != Task.PROBLEM_TYPE_OUTPUT) {
            job.setTask(task.getName());
        } else {
            job.setTask(task.getName() + language);
        }
        job.setLanguage(language);
        job.srcFilename = sourceFileName;
        job.src = tmp;
        gradingQueueManager.push(job, notified);
        dispatchJob();
    }

    public void test(String contestId, String userId, String task, String language, TempFile tmp,
            TempFile tmp2, JobRemovalNotified notified) {
        Job job = new Job();
        job.setContestId(contestId);
        job.setUserid(userId);
        job.setType(JobType.TEST);
        job.setTask(task);
        job.setLanguage(language);
        job.src = tmp;
        job.stdin = tmp2;
        gradingQueueManager.push(job, notified);
        dispatchJob();
    }

    private void releaseMachine(Object objMachineid) {
        GraderAgent gm = null;
        if (objMachineid instanceof GraderAgent)
            gm = (GraderAgent) objMachineid;
        else {
            Syslog.log("!AgentManager: releaseMachine: obj not instanceof Agent");
            return;
        }

        boolean bBusy = machineQueueManager.releaseMachineFromBusyMode(gm);
        if (!bBusy)
            LogGraderAgent.log("!releaseMachine(set it idle): gm already not busy");
        dispatchJob();
    }

    private void dispatchJob() {
        dispatchJobToGrader(JobType.SETUP);
        dispatchJobToGrader(JobType.SUBMIT);
        dispatchJobToGrader(JobType.FEEDBACK);
        dispatchJobToGrader(JobType.TEST);
        dispatchJobToGrader(JobType.GRADE);
    }

    public void OnMsgGaEntry(Object objMachineid) {
        registerMachine(objMachineid);
    }

    private void registerMachine(Object objMachineid) {
        GraderAgent gm = null;
        if (objMachineid instanceof GraderAgent)
            gm = (GraderAgent) objMachineid;
        else {
            Syslog.log("!AgentManager: registerMachine: obj not instanceof Agent");
            return;
        }

        machineQueueManager.registerMachine(gm);

        // provide obj2astr
        String version = gm.getVersion();
        if (version == null)
            version = "";
        String desc = gm.getIP().toString();
        Date current = new Date();

        String[] aRecord = new String[5];
        aRecord[0] = desc;
        aRecord[1] = version;
        aRecord[2] = "RESERVED";
        aRecord[3] = kr.or.ioi2002.RMIServer.Util.sdfDate.format(current);
        aRecord[4] = kr.or.ioi2002.RMIServer.Util.sdfTime.format(current);
        Object objRet = obj2astr.put(gm, aRecord);
        if (objRet != null)
            Syslog.log("!AgentManager: registerMachine: overwriting existing key in obj2astr: "
                    + objRet.toString());

        LogGraderAgent.log("registerMachine[" + version + "][" + gm.getIP() + "]");

        dispatchJob();
    }

    public void OnMsgGaFail(Object objMachineid, Job job) {
        removeMachine(objMachineid);

        if (job != null) {
            job.iRetryCount++;
            boolean bRetrySuccess = false;
            if (job.iRetryCount <= MAX_JOB_RETRY) {
                bRetrySuccess = retryJob(job);
            }

            if (!bRetrySuccess) {
                String task = job.getTask();
                if (task == null)
                    task = "NA_OnGaFail";
                if (job.getType().equals(JobType.GRADE)) {
                    String userid = job.getUserid();
                    String gmid = job.gmid;
                    if (userid == null)
                        userid = "NA_FAILED";
                    if (gmid == null)
                        gmid = "NA_FAILED";
                    recordManager.add(job.getUserid()
                            + ","
                            + job.gmid
                            + ","
                            + task
                            + ",NA_FAILED,NA_FAILED,0,");
                } else {
                    if (job.getType() == JobType.SUBMIT || job.getType() == JobType.FEEDBACK) {
                        mediator.submitAttemptFailed(job.getContestId(), job.getUserid(), job
                                .getTask(), job.output);

                    }
                    // type error
                    Syslog.log("!IOIServer: OnGaFail: Job Type invalid");
                }
            }
        }
    }

    private boolean retryJob(Job job) {
        if (job == null
                || job.getUserid() == null
                || job.getTask() == null
                || job.src == null
                || job.getType() == null) {
            LogGraderAgent.log("!retryJob: tried to retry invalid job");
            return false;
        } else {
            job.output = null;
            gradingQueueManager.rePush(job);
            dispatchJob();
            LogGraderAgent.log("retryJob: " + job.getUserid() + "," + job.getType());
            return true;
        }
    }

    private void removeMachine(Object objMachineid) {
        GraderAgent gm = null;
        if (objMachineid instanceof GraderAgent)
            gm = (GraderAgent) objMachineid;
        else {
            Syslog.log("!AgentManager: removeMachine: obj not instanceof Agent");
            return;
        }

        String state = machineQueueManager.removeMachine(gm);

        String version = "";
        String date = "";
        String time = "";

        String[] astr = (String[]) obj2astr.remove(gm);
        if (astr == null) {
            Syslog.log("!AgentManager: removeMachine: obj not found in obj2astr: " + gm.toString());
        } else {
            if (astr[0] != null) {
            }
            if (astr[1] != null)
                version = astr[1];
            if (astr[3] != null)
                date = astr[3];
            if (astr[4] != null)
                time = astr[4];
        }

        LogGraderAgent.log("removeMachine["
                + version
                + "]["
                + gm.getIP()
                + "] registered at["
                + date
                + " "
                + time
                + "] in state ["
                + state
                + "] Job["
                + gm.getJob()
                + "]");
    }

    public void OnMsgTestDone(Object objMachineid, Job job) {
        releaseMachine(objMachineid);

        if (job != null && job.getType() != null && !job.getType().equals(JobType.TEST)) {
            Syslog.log("OnMsgGmTestDone: !job.type.equals(TEST)");
            return;
        }

        String task = job.getTask();
        if (task == null)
            task = "NA_GmTestDone";
        mediator.testFinish(job.getContestId(), job.getUserid(), task, job.output);
        if (job != null && job.result != null && job.result.equals("OK")) {
            LogTest.log(job.getUserid() + ",OK");
        } else {
            LogTest.log(job.getUserid() + ",FAIL");
        }
    }

    public void OnMsgGradeDone(Object objMachineid, Job job) {
        releaseMachine(objMachineid);

        if (job != null && job.getType() != null && !job.getType().equals(JobType.GRADE)) {
            Syslog.log("OnMsgGmGradeDone: !job.type.equals(GRADE)");
            return;
        }

        String strCsvRecord = job.getUserid() + "," + job.gmid + "," + new String(job.output);

        // append grader log
        if (job.log != null) {
            strCsvRecord = strCsvRecord + "," + (new String(job.log)).replace('\n', '|');
        }

        recordManager.add(strCsvRecord);

        if (job != null && job.result != null && job.result.equals("OK")) {
            LogGrade.log(job.getUserid() + ",OK");
        } else {
            LogGrade.log(job.getUserid() + ",FAIL");
        }
    }

    public void OnMsgSubmitDone(Object objMachineid, Job job) {
        // release GM
        releaseMachine(objMachineid);

        // process job
        if (job != null && job.getType() != null && !job.getType().equals(JobType.SUBMIT)) {
            // should not happen
            Syslog.log("OnMsgCaSubmitDone:"
                    + " job != null && "
                    + "job.type != null && "
                    + "!job.type.equals('SUBMIT')");
            return;
        }

        try {
            if (job != null && job.result != null && job.result.equals("OK")) {
                TempFile tmpsrc = null;
                if (job.src instanceof TempFile) {
                    tmpsrc = (TempFile) job.src;
                } else {
                    throw new java.io.IOException(
                            "!critical: OnMsgGmSubmitDone: job.src not type of TempFile-impossible");
                }
                String srcFilename = job.srcFilename;
                if (srcFilename == null) {
                    throw new java.io.IOException(
                            "!critical: OnMsgGmSubmitDone: job.srcFilename is null-impossible");
                }

                mediator.submitSourceCode(job.getContestId(),
                        job.getUserid(),
                        job.getTask(),
                        tmpsrc,
                        job.getLanguage(),
                        srcFilename,
                        job.output);
                if (job.getTask() == null)
                    job.setTask("");
                LogSubmit.log(job.getUserid() + ",OK," + job.getTask());
            } else {
                mediator.submitAttemptFailed(job.getContestId(),
                        job.getUserid(),
                        job.getTask(),
                        job.output);
                if (job.getTask() == null)
                    job.setTask("");
                LogSubmit.log(job.getUserid() + ",FAIL," + job.getTask());
            }
        } catch (java.io.IOException e) {
            Syslog.log("!OnMsgCaSubmitDone: " + Util.stackTrace(e));
        }
    }

    public void OnMsgFeedbackDone(Object objMachineid, Job job) {
        // release GM
        releaseMachine(objMachineid);

        // process job
        if (job != null && job.getType() != null && !job.getType().equals(JobType.FEEDBACK)) {
            // should not happen
            Syslog.log("OnMsgFeedbackDone:"
                    + " job != null && "
                    + "job.type != null && "
                    + "!job.type.equals(FEEDBACK)");
            return;
        }

        if (job != null) {
            if (job.result != null && job.result.equals("OK")) {
                swapFeedbackInformation(job);
            } else {
                swapFeedbackInformationWithGraderCsv(job);
            }
        }

        try {
            if (job != null && job.result != null && job.result.equals("OK")) {
                TempFile tmpsrc = null;
                if (job.src instanceof TempFile) {
                    tmpsrc = (TempFile) job.src;
                } else {
                    throw new java.io.IOException(
                            "!critical: OnMsgGmSubmitDone: job.src not type of TempFile-impossible");
                }
                String srcFilename = job.srcFilename;
                if (srcFilename == null) {
                    throw new java.io.IOException(
                            "!critical: OnMsgGmSubmitDone: job.srcFilename is null-impossible");
                }

                mediator.submitSourceCode(job.getContestId(),
                        job.getUserid(),
                        job.getTask(),
                        tmpsrc,
                        job.getLanguage(),
                        srcFilename,
                        job.output);
                if (job.getTask() == null)
                    job.setTask("");
                LogSubmit.log(job.getUserid() + ",OK," + job.getTask());
            } else {
                mediator.submitAttemptFailed(job.getContestId(),
                        job.getUserid(),
                        job.getTask(),
                        job.output);
                if (job.getTask() == null)
                    job.setTask("");
                LogSubmit.log(job.getUserid() + ",FAIL," + job.getTask());
            }
        } catch (IOException e) {
            Syslog.log("!OnMsgFeedbackDone: " + Util.stackTrace(e));
        }
    }

    private void swapFeedbackInformationWithGraderCsv(Job job) {
        Contest contest = mediator.getContestManager().getContest(job.getContestId());
        if (contest == null)
            return;
        Task task = contest.getTaskByName(job.getTask());
        if (task == null)
            return;

        StringBuffer output = new StringBuffer();
        output.append(getFullGraderCsv(contest.getId(), job.getUserid(), task.getName()));
        job.output = output.toString().getBytes();
    }

    /**
     * Swap job.out with feedback information.
     * 
     * @param job
     */
    private void swapFeedbackInformation(Job job) {
        Contest contest = mediator.getContestManager().getContest(job.getContestId());
        if (contest == null)
            return;
        Task task = contest.getTaskByName(job.getTask());
        if (task == null)
            return;

        Set<Integer> testsCovered = new TreeSet<Integer>();
        for (TestGroup group : task.getTestGroups()) {
            if (group.isFeedbackEnabled()) {
                testsCovered.addAll(group.getTestCases());
            }
        }

        List<String> byTestResults = getResult(contest.getId(), job.getUserid(), task);
        int accepted = 0;
        int wrong = 0;
        int timeLimit = 0;
        int exception = 0;
        int system = 0;
        int compile = 0;
        for (int i = 0; i < task.getNumberOfTests(); ++i) {
            if (!testsCovered.contains(i + 1)) {
                continue;
            }

            String execResult = byTestResults.get(i);
            try {
                new BigDecimal(execResult);
                accepted++;
            } catch (NumberFormatException e) {
                if ("x".equals(execResult)) {
                    wrong++;
                } else if ("e".equals(execResult)) {
                    exception++;
                } else if ("t".equals(execResult)) {
                    timeLimit++;
                } else if ("c".equals(execResult)) {
                    compile++;
                } else {
                    System.out.print("Unknown output result:" + execResult);
                    system++;
                }
            }
        }

        StringBuffer output = new StringBuffer();
        output.append(getCompilerOutput(contest.getId(), job.getUserid(), task.getName()));
        if (testsCovered.size() == task.getNumberOfTests()) {
            output.append("Feedback for all test cases:\n");
        } else {
            output.append("Feedback for part of all test cases:\n");
        }
        asPercentage("Correct", output, accepted, testsCovered);
        asPercentage("Wrong answer", output, wrong, testsCovered);
        asPercentage("Time limit exceeded", output, timeLimit, testsCovered);
        asPercentage("Runtime exception", output, exception, testsCovered);
        asPercentage("Unknown type of error(s):", output, system, testsCovered);
        asPercentage("Compilation error", output, compile, testsCovered);
        job.output = output.toString().getBytes();
    }

    private void asPercentage(String type, StringBuffer output, int count, Set<Integer> testsCovered) {
        if (count > 0) {
            output.append(type);
            output.append(" : ");
            output.append(Integer.toString(100 * count / testsCovered.size()));
            output.append("%\n");
        }
    }

    public String[] getIdleMachineQueue() {
        return pretifyMachineQueue(machineQueueManager.getIdleMachineQueue());
    }

    public String[] getBusyMachineQueue() {
        return pretifyMachineQueue(machineQueueManager.getBusyMachineQueue());
    }

    private String[] pretifyMachineQueue(List<GraderAgent> machineQueue) {
        String[] printedQueue = new String[machineQueue.size()];
        int i = 0;
        for (GraderAgent agent : machineQueue) {
            StringBuffer sb = new StringBuffer();
            if (obj2astr.get(agent) != null)
                for (String machineInfo : obj2astr.get(agent)) {
                    sb.append(machineInfo);
                    sb.append(',');
                }
            printedQueue[i++] = sb.toString();
        }
        return printedQueue;
    }

    protected void dispatchJobToGrader(JobType jobType) {
        if (!gradingQueueManager.hasJob(jobType)) {
            return;
        }
        GraderAgent gm = machineQueueManager.moveMachineToBusy();
        if (gm == null) {
            return;
        }
        Job job = gradingQueueManager.popJob(jobType);

        if (gm != null && job != null && gm.assignJob(job)) {
            gradingQueueManager.markSuccessfullyAssignedJob(job);
        } else {
            machineQueueManager.releaseMachineFromBusyMode(gm);
            if (job != null)
                gradingQueueManager.rePush(job);
            dispatchJob();
        }
    }

    @Override
    protected void makeNewProduct(Socket socket) {
        new GraderAgent(this, mediator.getContestManager(), socket);
    }

    private List<String> printQueue(List<Job> queue) {
        List<String> itemList = new ArrayList<String>();

        for (Job job : queue)
            itemList.add(job.getUserid() + "|" + job.getTask());

        return itemList;
    }

    public File getTaskPath(String contestId, String userid, String task) {
        return new File(new File(new File(new File(fileRoot, contestId), userid), DIR_GRADE), task);
    }

    public List<String> getSubmitQueue() {
        return printQueue(gradingQueueManager.getQueue(JobType.SUBMIT));
    }

    public List<String> getFeedbackQueue() {
        return printQueue(gradingQueueManager.getQueue(JobType.FEEDBACK));
    }

    public List<String> getTestQueue() {
        return printQueue(gradingQueueManager.getQueue(JobType.TEST));
    }

    public List<String> getGradeQueue() {
        return printQueue(gradingQueueManager.getQueue(JobType.GRADE));
    }

    public void setMediator(ManagerMediator managerMediator) {
        mediator = managerMediator;
    }

    public void pushTestData(Contest contest) {
        int jobsCount = machineQueueManager.markAllGradersToUpdate(contest);
        for (int i = 0; i < jobsCount; ++i) {
            Job job = new Job();
            job.setContestId(contest.getId());
            job.setType(JobType.SETUP);
            gradingQueueManager.push(job, null);
            dispatchJob();
        }
    }
}
