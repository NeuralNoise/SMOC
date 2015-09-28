package bg.smoc.model.serializer;

import java.io.File;
import java.io.InputStream;
import java.util.Vector;

import bg.smoc.model.Contest;
import bg.smoc.model.Task;
import bg.smoc.model.Task.TestType;

public interface ContestSerializer {

    public void addContest(Contest modelContest);

    public Vector<Contest> getContests();

    public void deleteContest(String contestId);

    public Contest getContestById(String contestId);

    public void updateContest(Contest modifiedContest);

    public Vector<Task> getTasks(String contestId);

    public void addTask(String contestId, Task newTask);

    public Task getTask(String contestId, String taskId);

    public void updateTask(String contestId, Task newTask);

    public void deleteTask(String contestId, String taskId);

    public boolean storeTaskData(String contestId, String taskName, int testNumber,
            Task.TestType testType, InputStream inputStream);

    public boolean hasTestDataFile(String contestId, String taskName, int testNumber,
            Task.TestType testType);

    public FileInfo getTestDataFile(String contestId, String taskName, int testNumber,
            TestType testType);

    public File updateTestGroupsFile(String contestId, Task task);
}
