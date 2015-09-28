package bg.smoc.model;

import java.util.Vector;
import java.util.regex.Pattern;

import kr.or.ioi2002.RMIServer.TempFile;

public class Contest {
	
	static public enum Language {
		C ("C", "c"),
		CPP ("C++", "(cpp)|(cc)"),
		PASCAL ("PASCAL", "(pas)|(pp)"),
		/*
		CSHARP ("C#", "cs"),
		JAVA ("Java", "java"),
		EXE ("exe", "exe"),
		*/
		;
		
		private String language;

		private String extensionRegex;

		Language(String language, String extensionRegex) {
			this.language = language;
			this.extensionRegex = extensionRegex;
		}
		
		public String getExtensionRegex() {
			return extensionRegex;
		}

		public void setExtensionRegex(String extensionRegex) {
			this.extensionRegex = extensionRegex;
		}

		public String getLanguage() {
			return language;
		}

		public void setLanguage(String language) {
			this.language = language;
		}
		
		public boolean matchesFilename(String filename)
		{	return Pattern.matches(".*\\." + "(" + extensionRegex + ")", filename);
		}
	}
	
    private String id;

    private String name;

    private String shortName;

    private Vector<Task> tasks;

    private boolean running = false;

    private boolean testingOn;
    
    private boolean feedbackOn = false;

    private String lastStartTime;

    private String expectedEndTime;

    private String announcement;

    private int maxUploadSize = 10 * 1024;

    private boolean isOpenContest;

    private ContestState state;

    public Contest() {
        state = new ContestState();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public void setMaxUploadSize(int maxUploadSize) {
        this.maxUploadSize = maxUploadSize;
    }

    public int getMaxUploadSize() {
        return maxUploadSize;
    }

    public void populateFrom(Contest contest) {
        this.id = contest.id;
        this.name = contest.name;
        this.shortName = contest.shortName;
        this.tasks = contest.tasks;
        this.running = contest.running;
        this.testingOn = contest.testingOn;
        this.feedbackOn = contest.feedbackOn;
        this.expectedEndTime = contest.expectedEndTime;
        this.lastStartTime = contest.lastStartTime;
        this.announcement = contest.announcement;
        this.maxUploadSize = contest.maxUploadSize;
        this.isOpenContest = contest.isOpenContest;
        if (contest.state != null) {
            this.state = contest.state;
        } else {
            this.state = new ContestState();
        }
    }

    public Vector<Task> getTasks() {
        if (tasks == null)
            tasks = new Vector<Task>();
        return tasks;
    }

    public void setTasks(Vector<Task> tasks) {
        this.tasks = tasks;
    }

    public Task getTaskById(String taskId) {
        if (taskId == null)
            return null;

        for (Task task : tasks) {
            if (taskId.equals(task.getId()))
                return task;
        }
        return null;
    }
    
    public Task getTaskByName(String taskName) {
        if (taskName == null)
            return null;

        for (Task task : tasks) {
            if (taskName.equals(task.getName()) || 
                (task.getType() == Task.PROBLEM_TYPE_OUTPUT && // FIXME: ugly hack
                 taskName.startsWith(task.getName()) &&
                 taskName.substring(task.getName().length()).matches("^\\d{" + String.valueOf(Task.TESTCASE_PADDING_LENGTH) + "}$")))
                return task;
        }
        return null;
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public boolean isTestingOn() {
        return testingOn;
    }

    public void setTestingOn(boolean testingOn) {
        this.testingOn = testingOn;
    }

    public String getExpectedEndTime() {
        return expectedEndTime;
    }

    public void setExpectedEndTime(String expectedEndTime) {
        this.expectedEndTime = expectedEndTime;
    }

    public String getLastStartTime() {
        return lastStartTime;
    }

    public void setLastStartTime(String lastStartTime) {
        this.lastStartTime = lastStartTime;
    }

    public String getAnnouncement() {
        return announcement;
    }

    public void setAnnouncement(String announcement) {
        this.announcement = announcement;
    }

    public boolean hasTaskNamed(String taskName) {
        if (taskName == null)
            return false;
        String multiName = "";
        if (taskName.length() > 2)
            multiName = taskName.substring(0, taskName.length() - 2);
        for (Task task : tasks) {
            if (taskName.equalsIgnoreCase(task.getName()))
                return true;
            if (multiName.equalsIgnoreCase(task.getName())
                    && task.getType() != null
                    && task.getType() == Task.PROBLEM_TYPE_OUTPUT)
                return true;
        }
        return false;
    }

    public String getTask(TempFile tmp) {
        return "NA_NotProcessedInServer";
    }

    public boolean isOpenContest() {
        return isOpenContest;
    }

    public void setOpenContest(boolean isOpenContest) {
        this.isOpenContest = isOpenContest;
    }

    public ContestState getState() {
        return state;
    }

    public void setState(ContestState state) {
        this.state = state;
    }

    public boolean isFeedbackOn() {
        return feedbackOn;
    }

    public void setFeedbackOn(boolean feedbackOn) {
        this.feedbackOn = feedbackOn;
    }
}
