package bg.smoc.model.manager;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import java.util.zip.ZipInputStream;

import javax.servlet.http.HttpServletRequest;

import kr.or.ioi2002.RMIServer.LogSubmit;
import kr.or.ioi2002.RMIServer.LogTest;
import kr.or.ioi2002.RMIServer.Syslog;
import kr.or.ioi2002.RMIServer.TempFile;
import kr.or.ioi2002.RMIServer.User;
import kr.or.ioi2002.RMIServer.Util;
import bg.smoc.model.Contest;
import bg.smoc.model.Task;
import bg.smoc.model.UserAccount;
import bg.smoc.model.Contest.Language;
import bg.smoc.model.Task.TestType;
import bg.smoc.model.serializer.ContestSerializer;
import bg.smoc.model.serializer.FileInfo;

public class ContestManager extends GenericManager {

    private ContestSerializer contestSerializer;

    // ContestId, UserId, User
    private Map<String, Map<String, User>> activeUsers;
    private String workingDirectory;

    private Map<Contest, ContestStopTask> runningContests;
    private Timer timer;

    public ContestManager(String workingDirectory, ContestSerializer contestSerializer) {
        this.workingDirectory = workingDirectory;
        this.contestSerializer = contestSerializer;
        this.timer = new Timer();
        this.runningContests = new Hashtable<Contest, ContestStopTask>();
    }

    public void addContest(Contest contest) {
        contestSerializer.addContest(contest);
        activeUsers.put(contest.getId(), new HashMap<String, User>());
    }

    public Vector<Contest> getContests() {
        return contestSerializer.getContests();
    }

    public void deleteContest(String contestId) {
        activeUsers.remove(contestId);
        contestSerializer.deleteContest(contestId);
    }

    public Contest getContest(String contestId) {
        return contestSerializer.getContestById(contestId);
    }

    public void updateContest(Contest contest) {
        contestSerializer.updateContest(contest);
    }

    public Vector<Task> getTasks(String contestId) {
        return contestSerializer.getTasks(contestId);
    }

    public void addTask(String contestId, Task task) {
        contestSerializer.addTask(contestId, task);
    }

    public Task getTask(String contestId, String taskId) {
        return contestSerializer.getTask(contestId, taskId);
    }

    public void updateTask(String contestId, Task task) {
        contestSerializer.updateTask(contestId, task);
    }

    public void deleteTask(String contestId, String taskId) {
        contestSerializer.deleteTask(contestId, taskId);
    }

    /**
     * Schedules the specified contest for auto-termination at the contest's end
     * time. If the contest's end time is in the past, nothing will be done.
     */
    public void scheduleContest(Contest contest) {
        String endTime = contest.getExpectedEndTime();
        if (endTime == null) {
            // do nothing if there is no end time set
            return;
        }
        Date endDate = null;
        try {
            endDate = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss").parse(endTime);
        } catch (ParseException e) {
            return;
        }
        ContestStopTask stopTask = new ContestStopTask(contest);
        runningContests.put(contest, stopTask);
        timer.schedule(stopTask, endDate);
    }

    /**
     * Cancels the auto-termination of the specified contest.
     */
    public void cancelContest(Contest contest) {
        ContestStopTask stopTask = runningContests.remove(contest);
        if (stopTask != null) {
            stopTask.cancel();
        }
    }

    /**
     * Cancels the auto-termination of all scheduled contests.
     */
    public void cancelAllContests() {
        timer.cancel();
        runningContests.clear();
    }

    private User getUser(Contest contest, String userLogin) {
        return getUser(contest.getId(), userLogin);
    }

    public Hashtable<String, String> queryStatus(Contest contest, String userLogin) {
        User user = getUser(contest, userLogin);
        if (user == null)
            return null;

        Hashtable<String, String> hash = user.getStatus();

        hash.put("submitProgress", String.valueOf(user.getSubmitProgress()));
        hash.put("testProgress", String.valueOf(user.getTestProgress()));

        return hash;
    }

    public User getUser(String contestId, String userLogin) {
        Map<String, User> contestants = activeUsers.get(contestId);
        if (contestants == null)
            return null;
        return contestants.get(userLogin);
    }
    
    public boolean test(Contest contest, String userId, String task,
            String language, String sourceFileName, byte[] sourceFileData, String stdinFileName, byte[] stdinFileData) {
        User user = getUser(contest, userId);
        if (user == null)
            return false;

        TempFile tmp = null;
        TempFile tmp2 = null;
        try {
            tmp = TempFile.createFromByteArray(sourceFileData);
            if (stdinFileName != null && stdinFileData != null) {
                tmp2 = TempFile.createFromByteArray(stdinFileData);
            }
        } catch (java.io.IOException e) {
            Syslog.log("IOIGraderImpl: submit: " + e.toString());
            return false;
        }

        if (contest == null || !contest.isRunning() || !contest.isTestingOn())
            return false;

        if (!user.testStart())
            return false;

        mediator.test(contest.getId(), userId, task, language, tmp, tmp2, user);
        LogTest.log(userId + ",START");
        return true;
    }

    public boolean submit(Contest contest, String userId, Task task,
            String language, String sourceFileName, byte[] sourceFileData) {
        User user = getUser(contest, userId);
        if (user == null)
            return false;

        TempFile tmp = null;
        try {
            tmp = TempFile.createFromByteArray(sourceFileData);
        } catch (java.io.IOException e) {
            Syslog.log("ContestManager: submit: " + e.toString());
            return false;
        }

        if (contest == null || !contest.isRunning())
            return false;

        if (!user.submitStart())
            return false;

        mediator.submit(contest.getId(),
                userId,
                task,
                language,
                tmp,
                sourceFileName, user);
        LogSubmit.log(userId + ",START");
        return true;
    }

    public File getSourceCode(Contest contest, String userid, String task) {
        User user = getUser(contest, userid);
        if (user == null)
            return null;

        return user.getSourceCode(task);
    }
    
	public String getSourceCodeLanguage(Contest contest, String userid, String task) {
		User user = getUser(contest, userid);
        if (user == null)
            return null;
        
		return user.getSourceCodeLanguage(task);
	}

    public byte[] getSourceCodeAsByteArray(Contest contest, String userid, String taskname) {
        if (userid == null || taskname == null)
            return null;

        // load file to byte[]
        java.io.File sourcefile = getSourceCode(contest, userid, taskname);
        if (sourcefile == null) {
            Syslog.log("ContestManager: getFile: failed, userid["
                    + userid
                    + "] taskname["
                    + taskname
                    + "]");
            return null;
        }

        byte[] bytearray = null;

        try {
            bytearray = Util.file2byte(sourcefile);
        } catch (java.io.IOException e) {
            Syslog.log("IOIUserImpl: getFile: failed, userid["
                    + userid
                    + "] taskname["
                    + taskname
                    + "], IOException"
                    + e.toString());
            bytearray = null;
        }

        return bytearray;
    }

    public Contest getContest(HttpServletRequest request) {
        return getContest((String) request.getSession().getAttribute("contestId"));
    }

    public void initActiveUsers() {
        activeUsers = new HashMap<String, Map<String, User>>();
        Vector<Contest> allContests = contestSerializer.getContests();
        Vector<UserAccount> userAccounts = mediator.getAllUsers();
        for (Contest contest : allContests) {
            Map<String, User> userMap = new HashMap<String, User>();
            for (UserAccount account : userAccounts) {
                if (account.getContestIds().contains(contest.getId())) {
                    userMap.put(account.getLogin(), new User(contest.getId(), account.getLogin(),
                            workingDirectory));
                }
            }
            activeUsers.put(contest.getId(), userMap);
        }
    }

    /**
     * Schedules all running contests for auto-termination.
     */
    public void scheduleRunningContests() {
        Vector<Contest> allContests = contestSerializer.getContests();
        for (Contest contest : allContests) {
            if (contest.isRunning()) {
                scheduleContest(contest);
            }
        }
    }

    public void registerUserForContest(String contestId, String username) {
        Map<String, User> contestants = activeUsers.get(contestId);
        if (contestants == null)
            return;

        contestants.put(username, new User(contestId, username, workingDirectory));
    }

    public void removeUser(String login) {
        for (Map<String, User> contestants : activeUsers.values()) {
            contestants.remove(login);
        }
    }

    class ContestStopTask extends TimerTask {
        Contest contest;

        public ContestStopTask(Contest contest) {
            this.contest = contest;
        }

        @Override
        public void run() {
            contest.setRunning(false);
            contest.setTestingOn(false);
            updateContest(contest);
            runningContests.remove(contest);
        }
    }

    public boolean uploadTaskData(String contestId, Task task, int testNumber,
            Task.TestType testType, ZipInputStream zipStream) {
        if (task.getName() == null || "".equals(task.getName())) {
            return false;
        }
        return contestSerializer.storeTaskData(contestId,
                task.getName(),
                testNumber,
                testType,
                zipStream);
    }

    public List<List<String>> getTaskTests(String contestId, Task task) {
        LinkedList<List<String>> result = new LinkedList<List<String>>();
        for (int i = 1; i <= task.getNumberOfTests(); ++i) {
            LinkedList<String> testEntry = new LinkedList<String>();
            for (Task.TestType type : Arrays.asList(Task.TestType.IN, Task.TestType.SOL)) {
                if (contestSerializer.hasTestDataFile(contestId, task.getName(), i, type)) {
                    testEntry.add("downloadTest?contestId="
                            + contestId
                            + "&taskId="
                            + task.getId()
                            + "&testNum="
                            + Integer.toString(i)
                            + "&type="
                            + type);
                } else {
                    testEntry.add("");
                }
            }
            result.add(testEntry);
        }
        return result;
    }
    
    public String getTaskFromFilename(Contest contest, String filename, String task) {
        if (contest.getTaskByName(task) != null)
            return task;
        if (filename!=null && contest.getTaskByName(filename.split("\\.")[0]) != null )
            return filename.split("\\.")[0];
    	return null;
    }
    
    public String getLanguageFromFilename(Contest contest, String filename, String language) {
    	if (language!=null && !language.equals(""))
    	{	for (Language lang : Contest.Language.values())
    			if (lang.getLanguage().equals(language))
    				return lang.getLanguage();
    	}
    	if (filename!=null && filename.contains(".")){
    		String ext = filename.split("\\.")[1];
    		for (Language lang : Contest.Language.values())
    			if (lang.matchesExtension(ext))
    				return lang.getLanguage();
    	}
    	return null;
    }
    
    public FileInfo getTestDataFile(String contestId, String taskName, int testNumber,
            TestType testType) {
        return contestSerializer.getTestDataFile(contestId, taskName, testNumber, testType);
    }

    public File updateTestGroupsFile(String contestId, Task task) {
        return contestSerializer.updateTestGroupsFile(contestId, task);
    }
}
