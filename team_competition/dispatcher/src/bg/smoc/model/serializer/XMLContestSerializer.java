package bg.smoc.model.serializer;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Vector;

import bg.smoc.model.Contest;
import bg.smoc.model.ContestState;
import bg.smoc.model.Task;
import bg.smoc.model.TestGroup;
import bg.smoc.model.Task.TestType;
import bg.smoc.web.utils.FileUtils;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

public class XMLContestSerializer implements ContestSerializer {

    private static final String CONTESTS_XML_FILENAME = "contests.xml";

    Vector<Contest> contests;

    private File workingFile;

    public XMLContestSerializer(String workingDirectory) {
        workingFile = new File(workingDirectory, CONTESTS_XML_FILENAME);
    }

    @SuppressWarnings("unchecked")
    public void init() {
        contests = new Vector<Contest>();
        XStream xstream = new XStream(new DomDriver());
        try {
            FileInputStream fileStream = new FileInputStream(workingFile);
            Object serializedContests = xstream.fromXML(fileStream);
            if (serializedContests != null) {
                contests = (Vector<Contest>) serializedContests;
            }
            fileStream.close();
        } catch (IOException e) {
            return;
        }
        ValidateConsitency();
    }

    public void addContest(Contest modelContest) {
        Contest newContest = new Contest();
        newContest.populateFrom(modelContest);
        newContest.setId(getNextId());
        contests.add(newContest);
        storeContests();
        modelContest.setId(newContest.getId());
    }

    synchronized private void storeContests() {
        XStream xstream = new XStream(new DomDriver());
        try {
            FileOutputStream fileStream = new FileOutputStream(workingFile);
            xstream.toXML(contests, fileStream);
            fileStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    synchronized private String getNextId() {
        long nextAvailableId = 0;
        while (true) {
            String nextId = "Contest_" + Long.toString(nextAvailableId);
            boolean okToUse = true;
            for (Contest contest : contests) {
                if (nextId.equals(contest.getId())) {
                    okToUse = false;
                    break;
                }
            }
            if (okToUse)
                return nextId;
            nextAvailableId++;
        }
    }

    private void ValidateConsitency() {
        for (int i = 0; i < contests.size(); ++i) {
            String contestId = contests.get(i).getId();
            if (contestId == null) {
                contests.remove(i);
                --i;
            } else {
                if (contests.get(i).getState() == null) {
                    contests.get(i).setState(new ContestState());
                }
            }
            for (int j = i + 1; j < contests.size(); ++j) {
                if (contestId.equals(contests.get(j).getId())) {
                    contests.remove(j);
                    --j;
                }
            }
        }
    }

    public Vector<Contest> getContests() {
        return contests;
    }

    public void deleteContest(String contestId) {
        if (contestId == null)
            return;

        for (int i = 0; i < contests.size(); ++i) {
            if (contestId.equals(contests.get(i).getId())) {
                contests.remove(i);
                --i;
            }
        }
    }

    public Contest getContestById(String contestId) {
        if (contestId == null) {
            System.out.println("How dare you pass empty.");
            return null;
        }
        for (Contest contest : contests) {
            if (contestId.equals(contest.getId()))
                return contest;
        }
        System.out.println(contestId + " not found.");
        return null;
    }

    public void updateContest(Contest modifiedContest) {
        Contest storeContest = getContestById(modifiedContest.getId());
        if (storeContest == null)
            return;

        storeContest.populateFrom(modifiedContest);
        storeContests();
    }

    public Vector<Task> getTasks(String contestId) {
        Contest contest = getContestById(contestId);
        if (contest == null)
            return null;

        return contest.getTasks();
    }

    public void addTask(String contestId, Task newTask) {
        Contest contest = getContestById(contestId);
        if (contest == null)
            return;

        int i;
        for (i = 0;; ++i)
            if (null == contest.getTaskById(contest.getId() + "_" + Integer.toString(i)))
                break;

        newTask.setId(contest.getId() + "_" + Integer.toString(i));
        contest.getTasks().add(newTask);
        storeContests();
    }

    public Task getTask(String contestId, String taskId) {
        if (taskId == null)
            return null;

        Contest contest = getContestById(contestId);
        if (contest == null)
            return null;

        return contest.getTaskById(taskId);
    }

    public void updateTask(String contestId, Task newTask) {
        Contest contest = getContestById(contestId);
        if (contest == null)
            return;

        Task task = contest.getTaskById(newTask.getId());
        task.populateFrom(newTask);
        storeContests();
    }

    public void deleteTask(String contestId, String taskId) {
        Contest contest = getContestById(contestId);
        if (contest == null)
            return;

        contest.getTasks().remove(contest.getTaskById(taskId));
        storeContests();
    }

    public boolean storeTaskData(String contestId, String taskName, int testNumber,
            Task.TestType testType, InputStream inputStream) {
        File dataFile = getCorrectFileName(contestId, taskName, testNumber, testType);
        dataFile.mkdirs();
        if (dataFile.exists()) {
            if (!dataFile.delete())
                return false;
        }
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(dataFile.getAbsolutePath());
            if (!FileUtils.copyStreams(inputStream, fileOutputStream)) {
                return false;
            }
        } catch (FileNotFoundException e) {
            // This should be impossible.
            return false;
        }
        return true;
    }

    private File getCorrectFileName(String contestId, String taskName, int testNumber,
            TestType testType) {
        return new File(new File(
                new File(new File(workingFile.getParentFile(), "TESTS"), contestId), taskName),
                String
                        .format("%s.%0" + Task.TESTCASE_PADDING_LENGTH + "d.%s",
                        taskName,
                        testNumber,
                        testType.toString().toLowerCase()));
    }
    
    private File getCorrectMetaFileName(String contestId, String taskName) {
        return new File(new File(
                new File(new File(workingFile.getParentFile(), "TESTS"), contestId), taskName),
                taskName + ".txt");
    }

    public boolean hasTestDataFile(String contestId, String taskName, int testNumber,
            TestType testType) {
        return getCorrectFileName(contestId, taskName, testNumber, testType).exists();
    }

    public FileInfo getTestDataFile(String contestId, String taskName, int testNumber,
            TestType testType) {
        try {
            File file = getCorrectFileName(contestId, taskName, testNumber, testType);
            if (file.exists()) {
                FileInfo info = new FileInfo(new BufferedInputStream(new FileInputStream(file)),
                        file.length(), file.getAbsolutePath());
                return info;
            } else {
                return null;
            }
        } catch (FileNotFoundException e) {
            // We should never really get here.
            return null;
        }
    }

    public File updateTestGroupsFile(String contestId, Task task) {
        File metaFile = getCorrectMetaFileName(contestId, task.getName());
        metaFile.mkdirs();
        try {
            PrintWriter out = new PrintWriter(new FileWriter(metaFile));
            for (TestGroup group : task.getTestGroups()) {
                StringBuffer st = new StringBuffer();
                st.append(group.getPoints().toString());
                st.append(" ");
                if (group.isFeedbackEnabled()) {
                    st.append("yes\n");
                } else {
                    st.append("no\n");
                }
                out.append(st.toString());
                
                st = new StringBuffer();
                boolean isFirst = true;
                for (Integer testNumber : group.getTestCases()) {
                    if (isFirst) {
                        st.append(testNumber.toString());
                        isFirst = false;
                    } else {
                        st.append(" ");
                        st.append(testNumber.toString());
                    }
                }
                st.append("\n");
                out.append(st.toString());
            }
            out.append("");
            out.close();
        } catch (IOException e) {
        }
        return metaFile;
    }
}
