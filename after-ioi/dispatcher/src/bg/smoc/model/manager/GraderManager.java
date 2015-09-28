/**
 * 
 */
package bg.smoc.model.manager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.or.ioi2002.RMIServer.Job;
import kr.or.ioi2002.RMIServer.LogGrade;
import kr.or.ioi2002.RMIServer.LogSubmit;
import kr.or.ioi2002.RMIServer.LogTest;
import kr.or.ioi2002.RMIServer.Syslog;
import kr.or.ioi2002.RMIServer.TempFile;
import kr.or.ioi2002.RMIServer.Util;
import kr.or.ioi2002.RMIServer.Job.JobType;
import kr.or.ioi2002.RMIServer.agent.LogGraderAgent;
import kr.or.ioi2002.RMIServer.agent.TCPListner;
import bg.smoc.agent.GraderAgent;
import bg.smoc.model.AccumulatedGrade;
import bg.smoc.model.Contest;
import bg.smoc.model.GradeProto;
import bg.smoc.model.Task;
import bg.smoc.model.TestGroup;
import bg.smoc.model.GradeProto.GradeResult;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

/**
 * @author tsvetan.bogdanov@gmail.com
 * 
 */
public class GraderManager extends TCPListner {

    private static final int MAX_JOB_RETRY = 1;

    public static final String DIR_GRADE = "GRADE";

    protected MachineQueueManager machineQueueManager;

    protected HashMap<GraderAgent, String[]> obj2astr = new HashMap<GraderAgent, String[]>(); // [0]description

    private File fileRoot = null;

    private GradingQueue gradingQueueManager;

    private ManagerMediator mediator;

    private Map<String, Map<String, Map<String, AccumulatedGrade>>> mGradeResults;

    public GraderManager(int gradingPort, String workingDirectory) {
        super(gradingPort);
        gradingQueueManager = new GradingQueue(workingDirectory);
        machineQueueManager = new MachineQueueManager();
        this.fileRoot = new File(workingDirectory, "USERS");
        mGradeResults = new HashMap<String, Map<String, Map<String, AccumulatedGrade>>>();

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

    public final AccumulatedGrade getResult(String contestId, String login, Task task) {
        AccumulatedGrade gradeResult = getAccumulatedResult(contestId, task, login);
        if (handleTestCasesResize(task, gradeResult) || handleTestGroupsResize(task, gradeResult)) {
            recalculateGroups(contestId, login, task, gradeResult);
        }
        return gradeResult;
    }

    private boolean handleTestGroupsResize(Task task, AccumulatedGrade gradeResult) {
        List<String> testGroups = gradeResult.getTestGroups();
        int testGroupsSize = task.getTestGroups().size();
        if (testGroups.size() == testGroupsSize) {
            return false;
        }
        while (testGroupsSize < testGroups.size()) {
            testGroups.remove(testGroups.size() - 1);
        }
        while (testGroupsSize > testGroups.size()) {
            testGroups.add("-");
        }
        return true;
    }

    private void updateAccumulatedResult(String contestId, Task task, String userid,
            GradeResult gradeResult) {
        AccumulatedGrade accumulatedGrade = getAccumulatedResult(contestId, task, userid);
        if (handleTestCasesResize(task, accumulatedGrade)
                || handleTestGroupsResize(task, accumulatedGrade)) {
            // Should never happen
            Syslog
                    .log("This should never happen. A resize has been called on GraderManager#updateAccumulatedResult");
        }
        for (int i = 0; i < gradeResult.getTestIndexesCount(); ++i) {
            int testIndex = gradeResult.getTestIndexes(i) - 1;
            if (testIndex < 0 || testIndex >= task.getNumberOfTests()) {
                continue;
            }
            accumulatedGrade.getTestCases().set(testIndex, gradeResult.getResult(i));
        }
        recalculateGroups(contestId, userid, task, accumulatedGrade);
    }

    private void recalculateGroups(String contestId, String login, Task task,
            AccumulatedGrade gradeResult) {
        BigDecimal total = BigDecimal.ZERO;
        for (int i = 0; i < task.getTestGroups().size(); ++i) {
            TestGroup group = task.getTestGroups().get(i);
            String result = null;
            BigDecimal value = BigDecimal.ZERO;
            for (int testIndex : group.getTestCases()) {
                String testResult = gradeResult.getTestCases().get(testIndex - 1);
                if (result == null) {
                    result = testResult;
                    try {
                        value = new BigDecimal(result);
                    } catch (NumberFormatException e) {
                        value = BigDecimal.ZERO;
                        break;
                    }
                } else {
                    try {
                        BigDecimal newValue = new BigDecimal(testResult);
                        if (newValue.compareTo(value) < 0) {
                            result = testResult;
                            value = newValue;
                        }
                    } catch (NumberFormatException e) {
                        result = testResult;
                        value = BigDecimal.ZERO;
                        break;
                    }
                }
            }
            gradeResult.getTestGroups().set(i, (result != null) ? result : " ");
            total = total.add(value);
        }
        gradeResult.setTotal(total.toString());
        storeAccumulatedGrade(contestId, login, task, gradeResult);
    }

    private boolean handleTestCasesResize(Task task, AccumulatedGrade gradeResult) {
        List<String> testCases = gradeResult.getTestCases();
        int numberOfTests = task.getNumberOfTests();
        if (numberOfTests == testCases.size()) {
            return false;
        }
        while (numberOfTests < testCases.size()) {
            testCases.remove(testCases.size() - 1);
        }
        while (numberOfTests > testCases.size()) {
            testCases.add("-");
        }
        return true;
    }

    private AccumulatedGrade getAccumulatedResult(String contestId, Task task, String login) {
        Map<String, Map<String, AccumulatedGrade>> contestResult = mGradeResults.get(contestId);
        if (contestResult == null) {
            contestResult = new HashMap<String, Map<String, AccumulatedGrade>>();
            mGradeResults.put(contestId, contestResult);
        }
        Map<String, AccumulatedGrade> taskResult = contestResult.get(task.getName());
        if (taskResult == null) {
            taskResult = new HashMap<String, AccumulatedGrade>();
            contestResult.put(task.getName(), taskResult);
        }
        AccumulatedGrade gradeResult = taskResult.get(login);
        if (gradeResult != null) {
            return gradeResult;
        }
        gradeResult = parseAccumulatedResult(contestId, task, login);
        if (gradeResult == null) {
            gradeResult = buildNewAccumulatedGrade(task);
            taskResult.put(login, gradeResult);
            storeAccumulatedGrade(contestId, login, task, gradeResult);
        } else {
            taskResult.put(login, gradeResult);
        }
        return gradeResult;
    }

    private AccumulatedGrade parseAccumulatedResult(String contestId, Task task, String login) {
        File accumulatedGradeFile = new File(getTaskPath(contestId, login, task.getName()),
                "accumulated.xml");
        if (!accumulatedGradeFile.exists()) {
            return null;
        }
        XStream xstream = new XStream(new DomDriver());
        try {
            FileInputStream fileStream = new FileInputStream(accumulatedGradeFile);
            Object scannedObject = xstream.fromXML(fileStream);
            fileStream.close();
            if (scannedObject != null) {
                return (AccumulatedGrade) scannedObject;
            }
        } catch (IOException e) {
        }

        return null;
    }

    private void storeAccumulatedGrade(String contestId, String login, Task task,
            AccumulatedGrade gradeResult) {
        File accumulatedGradeFile = new File(getTaskPath(contestId, login, task.getName()),
                "accumulated.xml");
        XStream xstream = new XStream(new DomDriver());
        try {
            FileOutputStream fileStream = new FileOutputStream(accumulatedGradeFile);
            xstream.toXML(gradeResult, fileStream);
            fileStream.close();
        } catch (IOException e) {
            Syslog.log(e.toString());
            e.printStackTrace();
        }
    }

    private AccumulatedGrade buildNewAccumulatedGrade(Task task) {
        AccumulatedGrade accumulated = new AccumulatedGrade();
        for (int i = 0; i < task.getNumberOfTests(); ++i) {
            accumulated.getTestCases().add("-");
        }
        for (int i = 0; i < task.getTestGroups().size(); ++i) {
            accumulated.getTestGroups().add("-");
        }
        accumulated.setTotal("0");
        return accumulated;
    }

    public boolean grade(Contest contest, String userId, String task) {
        File srcfile = mediator.getSourceCode(contest, userId, task);
        if (srcfile == null)
            return false;

        grade(contest.getId(), userId, task, mediator.getSourceCodeLanguage(contest.getId(),
                userId,
                task), srcfile);
        LogGrade.log(userId + ",START");
        return true;
    }

    public boolean gradeAfterSubmit(String contestId, String userId, String task, String language) {
        File srcfile = mediator.getSourceCode(contestId, userId, task);
        if (srcfile == null)
            return false;

        Job job = new Job();
        job.setContestId(contestId);
        job.setUserid(userId);
        job.setType(JobType.GRADE);
        job.setTask(task);
        job.setLanguage(language);
        job.setNotGradeFeedback(true);
        job.src = srcfile;
        gradingQueueManager.push(job, null);
        LogGrade.log(userId + ",START-lean");
        dispatchJob();
        return true;
    }

    private boolean handleOuputOnlyTask(Job job) {
        Task task = mediator.getContestManager().getTaskByName(job.getContestId(), job.getTask());
        if (task == null) {
            return false;
        }
        if (task.getType() != Task.PROBLEM_TYPE_OUTPUT
                || job.getTask().equalsIgnoreCase(task.getName())
                || !job.getTask().startsWith(task.getName())) {
            return false;
        }
        job.setTestIndex(job.getTask().substring(task.getName().length()));
        job.setTask(task.getName());
        return true;
    }

    public void grade(String contestId, String userid, String task, String language, File tmp) {
        Job job = new Job();
        job.setContestId(contestId);
        job.setUserid(userid);
        job.setType(JobType.GRADE);
        job.setTask(task);
        job.setLanguage(language);
        handleOuputOnlyTask(job);
        job.src = tmp;
        gradingQueueManager.push(job, null);
        dispatchJob();
    }

    public void submit(String contestId, String userId, Task task, String language, File tmp,
            String sourceFileName, JobRemovalNotified notified, boolean isAlwaysAccept) {
        Job job = new Job();
        job.setContestId(contestId);
        job.setUserid(userId);
        job.setType(JobType.SUBMIT);
        job.setTask(task.getName());
        job.setAlwaysAccept(isAlwaysAccept || (task.getType() == Task.PROBLEM_TYPE_OUTPUT));
        if (task.getType() == Task.PROBLEM_TYPE_OUTPUT) {
            job.setTestIndex(language);
            job.setLanguage("N/A");
        } else {
            job.setLanguage(language);
        }
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

    public void releaseMachine(Object objMachineid) {
        GraderAgent gm = null;
        if (objMachineid instanceof GraderAgent) {
            gm = (GraderAgent) objMachineid;
            gm.assureNoCurrentJob();
        } else {
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
        dispatchJobToGrader(JobType.TEST);
        if (machineQueueManager.isIdleEnoughToGrade()) {
            dispatchJobToGrader(JobType.GRADE);
        }
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
        aRecord[3] = kr.or.ioi2002.RMIServer.Util.DATETIME_FORMAT_DATE.format(current);
        aRecord[4] = kr.or.ioi2002.RMIServer.Util.DATETIME_FORMAT_TIME.format(current);
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
                } else {
                    if (job.getType() == JobType.SUBMIT) {
                        mediator.submitAttemptFailed(job.getContestId(), job.getUserid(), job
                                .getTask(), (job.getGradeResult() != null) ? job.getGradeResult()
                                .getSampleOutput() : "system error!!");

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

    public void OnMsgTestDone(Job job) {
        if (job != null && job.getType() != null && !job.getType().equals(JobType.TEST)) {
            Syslog.log("OnMsgGmTestDone: !job.type.equals(TEST)");
            return;
        }

        String task = job.getTask();
        if (task == null)
            task = "NA_GmTestDone";
        mediator.testFinish(job.getContestId(), job.getUserid(), task, job.getGradeResult()
                .getSampleOutput());
        if (job != null && job.result != null && job.result.equals("OK")) {
            LogTest.log(job.getUserid() + ",OK");
        } else {
            LogTest.log(job.getUserid() + ",FAIL");
        }
    }

    public void OnMsgGradeDone(Job job) {
        if (job != null && job.getType() != null && !job.getType().equals(JobType.GRADE)) {
            Syslog.log("OnMsgGmGradeDone: !job.type.equals(GRADE)");
            return;
        }

        if (job != null && job.result != null && job.result.equals("OK")) {
            Task task = mediator.getContestManager().getTaskByName(job.getContestId(),
                    job.getTask());
            if (task == null || job.getUserid() == null) {
                LogGrade.log(job.getUserid() + ", Exception - NOT OK");
                return;
            }
            updateAccumulatedResult(job.getContestId(), task, job.getUserid(), job.getGradeResult());
            LogGrade.log(job.getUserid() + ",OK");
        } else {
            LogGrade.log(job.getUserid() + ",FAIL");
        }
    }

    public void OnMsgSubmitDone(Job job) {
        boolean acceptedSolution = true;
        String log = "FAIL";
        if (job != null
                && job.result != null
                && job.result.equals("OK")
                && job.getGradeResult() != null
                && job.getGradeResult().getAccept()) {

            if (!job.isAlwaysAccept()) {
                try {
                    acceptedSolution = !BigDecimal.ZERO.equals(new BigDecimal(job.getGradeResult()
                            .getResult(0)));
                } catch (NumberFormatException e) {
                    acceptedSolution = false;
                }
            }
            appendFeedbackInformation(job);
        } else {
            acceptedSolution = false;
        }

        appendIsSolutionAccepted(job, acceptedSolution);

        if (acceptedSolution) {
            TempFile tmpsrc = null;

            Task task = mediator.getContestManager().getTaskByName(job.getContestId(),
                    job.getTask());
            if (task == null || job.getUserid() == null) {
                LogSubmit.log(job.getUserid() + ", Exception - NOT OK");
            } else {
                updateAccumulatedResult(job.getContestId(), task, job.getUserid(), job
                        .getGradeResult());
            }

            try {
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
                        job.getGradeResult().getSampleOutput());

                if (Task.PROBLEM_TYPE_OUTPUT != task.getType()) {
                    gradeAfterSubmit(job.getContestId(), job.getUserid(), job.getTask(), job
                            .getLanguage());
                }
            } catch (java.io.IOException e) {
                Syslog.log("!OnMsgCaSubmitDone: " + Util.stackTrace(e));
            }

            log = "OK";
        } else {
            mediator.submitAttemptFailed(job.getContestId(), job.getUserid(), job.getTask(), job
                    .getGradeResult().getSampleOutput());
        }
        if (job.getTask() == null)
            job.setTask("");
        LogSubmit.log(job.getUserid() + "," + log + "," + job.getTask());
    }

    /**
     * Add to the sample output if accepted or not.
     * 
     * @param job
     * @param acceptedSolution
     */
    private void appendIsSolutionAccepted(Job job, boolean acceptedSolution) {
        if (job == null
                || job.getGradeResult() == null
                || job.getGradeResult().getSampleOutput() == null)
            return;
        StringBuffer output = new StringBuffer(job.getGradeResult().getSampleOutput());
        output.append("\n... Submission ");
        if (acceptedSolution) {
            output.append(" Successful\n");
        } else {
            output.append(" Failed\n");
        }
        job.setGradeResult(GradeProto.GradeResult.newBuilder().mergeFrom(job.getGradeResult())
                .setSampleOutput(output.toString()).build());
    }

    /**
     * Swap the sampleOutput in the job with feedback information.
     * 
     * @param job
     */
    private void appendFeedbackInformation(Job job) {
        Task task = mediator.getContestManager().getTaskByName(job.getContestId(), job.getTask());
        if (task == null)
            return;

        if (!task.isFeedbackEnabled()) {
            return;
        }

        StringBuffer output = new StringBuffer(job.getGradeResult().getSampleOutput());

        int accepted = 0;
        int partial = 0;
        int wrong = 0;
        int timeLimit = 0;
        int exception = 0;
        int system = 0;
        int compile = 0;
        int total = 0;
        List<BigDecimal> maxPoints = task.getTestsPoints(job.getGradeResult().getTestIndexesList());
        for (int i = 0; i < job.getGradeResult().getResultCount(); i++) {
            if (job.getGradeResult().getTestIndexes(i) == 0)
                continue;
            total++;
            String execResult = job.getGradeResult().getResult(i);
            try {
                BigDecimal pointsGained = new BigDecimal(execResult);
                if (pointsGained.equals(maxPoints.get(i))) {
                    accepted++;
                } else {
                    partial++;
                }
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
        if (total == 0)
            return;

        output.append("\n--- [DETAILED FEEDBACK] ---\n");
        asPercentage("Correct (full score for the test case)", output, accepted, total);
        asPercentage("Correct (partial score for the test case)", output, partial, total);
        asPercentage("Wrong answer(zero points for the test case)", output, wrong, total);
        asPercentage("Time limit exceeded", output, timeLimit, total);
        asPercentage("Run-time error (possible causes:Segmentation fault, "
                + "Memory limit exceeded, Output limit exceeded, etc.)", output, exception, total);
        asPercentage("Unknown type of error(s):", output, system, total);
        asPercentage("Compilation error", output, compile, total);
        job.setGradeResult(GradeProto.GradeResult.newBuilder().mergeFrom(job.getGradeResult())
                .setSampleOutput(output.toString()).build());
    }

    private void asPercentage(String type, StringBuffer output, int count, int testsCount) {
        if (count > 0) {
            output.append(type);
            output.append(" : ");
            output.append(count);
            output.append(" test cases (");
            output.append(Integer.toString(100 * count / testsCount));
            output.append("%)\n");
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
            
            if (agent.getJob()!=null) {
                sb.append("[" + agent.getJob().getType().name() + "] " + agent.getJob().getUserid() + "|" + agent.getJob().getTask());
                sb.append(",");
            } else {
                sb.append("-");
                sb.append(",");
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
            if (job != null) {
                gradingQueueManager.rePush(job);
            }
        }
    }

    @Override
    protected void makeNewProduct(Socket socket) {
        while (mediator == null) {
            try {
                sleep(400);
            } catch (InterruptedException e) {
                return;
            }
        }
        new GraderAgent(this, mediator.getContestManager(), socket);
    }

    private List<String> printQueue(List<Job> queue) {
        List<String> itemList = new ArrayList<String>();

        for (Job job : queue)
            itemList.add(job.getUserid() + "|" + job.getTask());

        return itemList;
    }

    public File getTaskPath(String contestId, String userid, String task) {
        File result = new File(
                new File(new File(new File(fileRoot, contestId), userid), DIR_GRADE), task);
        if (!result.isDirectory()) {
            result.mkdirs();
        }
        return result;
    }

    public File getAccountPath(String contestId, String userid) {
        File result = new File(new File(fileRoot, contestId), userid);
        if (!result.isDirectory()) {
            result.mkdirs();
        }
        return result;
    }

    public List<String> getSubmitQueue() {
        return printQueue(gradingQueueManager.getQueue(JobType.SUBMIT));
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

    public void storeContestReport(String contestId, String login, String report) {
        File reportFile = new File(getAccountPath(contestId, login), "contestReport.txt");
        try {
            FileOutputStream fileStream = new FileOutputStream(reportFile);
            fileStream.write(report.getBytes("UTF-8"));
            fileStream.close();
        } catch (IOException e) {
            Syslog.log(e.toString());
            e.printStackTrace();
        }
    }

    public File getContestReportFile(String contestId, String login) {
        return new File(getAccountPath(contestId, login), "contestReport.txt");
    }
}
