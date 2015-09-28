package bg.smoc.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import kr.or.ioi2002.RMIServer.Job.JobType;

public class Task implements Serializable {

    private static final BigDecimal DEFAULT_TEST_SCORE = new BigDecimal(10);

    private static final int SAMPLE_DATA_TEST_NUMBER = 0;

    public enum TestType {
        IN, SOL
    }

    private static final long serialVersionUID = 999981131076839340L;

    // TODO: Export there 4 constants to an enum in a protocol buffer.
    public static final int PROBLEM_TYPE_STANDARD = 1;
    public static final int PROBLEM_TYPE_OUTPUT = 2;
    public static final int PROBLEM_TYPE_MODULE = 3;

    public static final int TESTCASE_PADDING_LENGTH = 3;

    private String name;

    private Integer type;

    private int numberOfTests;

    private String id;

    private int maxSubmitSize = 50 * 1024;

    private List<TestGroup> testGroups;

    private boolean feedbackEnabled;

    private int timeLimit;
    private int memoryLimit;
    private int outputLimit;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name.toLowerCase();
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getNumberOfTests() {
        return numberOfTests;
    }

    public void setNumberOfTests(int numberOfTests) {
        this.numberOfTests = numberOfTests;
    }

    public List<TestGroup> getTestGroups() {
        if (testGroups == null) {
            testGroups = new ArrayList<TestGroup>();
        }
        return testGroups;
    }

    public int getTimeLimit() {
        return timeLimit;
    }

    public void setTimeLimit(int timeLimit) {
        this.timeLimit = timeLimit;
    }

    public int getMemoryLimit() {
        return memoryLimit;
    }

    public void setMemoryLimit(int memoryLimit) {
        this.memoryLimit = memoryLimit;
    }

    public int getOutputLimit() {
        return outputLimit;
    }

    public void setOutputLimit(int outputLimit) {
        this.outputLimit = outputLimit;
    }

    public void setTestGroups(List<TestGroup> testGroups) {
        this.testGroups = testGroups;
        feedbackEnabled = false;
        if (testGroups != null) {
            for (TestGroup group : testGroups) {
                if (group.isFeedbackEnabled()) {
                    feedbackEnabled = true;
                }
            }
        }
    }

    public void populateFrom(Task newTask) {
        this.name = newTask.name;
        this.type = newTask.type;
        this.numberOfTests = newTask.numberOfTests;
        this.maxSubmitSize = newTask.maxSubmitSize;
        this.testGroups = (newTask.getTestGroups() == null) ? null : new LinkedList<TestGroup>(
                newTask.getTestGroups());
    }

    public String getNameAppenedTest(int index) {
        String suffix = String.valueOf(index + 1);
        while (suffix.length() < Task.TESTCASE_PADDING_LENGTH)
            suffix = ("0") + suffix;
        return getName() + suffix;
    }

    public void addTestGroup(TestGroup group) {
        if (testGroups == null) {
            testGroups = new LinkedList<TestGroup>();
        }
        testGroups.add(group);
    }

    public void setMaxSubmitSize(int maxSubmitSize) {
        this.maxSubmitSize = maxSubmitSize;
    }

    public int getMaxSubmitSize() {
        return maxSubmitSize;
    }

    public boolean isFeedbackEnabled() {
        return feedbackEnabled;
    }

    public Set<Integer> getTestIndexesByJobType(JobType jobType, boolean gradeFeedback,
            boolean isFeedbackOn) {
        Set<Integer> result = new TreeSet<Integer>();
        if (jobType == JobType.SUBMIT) {
            result.add(SAMPLE_DATA_TEST_NUMBER);
            if (isFeedbackOn) {
                for (TestGroup group : testGroups) {
                    if (group.isFeedbackEnabled()) {
                        result.addAll(group.getTestCases());
                    }
                }
            }
        } else {
            for (TestGroup group : testGroups) {
                if (!isFeedbackOn || gradeFeedback || !group.isFeedbackEnabled()) {
                    result.addAll(group.getTestCases());
                }
            }
        }
        return result;
    }

    public List<BigDecimal> getTestsPoints(List<Integer> testIndexesList) {
        HashMap<Integer, BigDecimal> points = getAllTestPoints();
        List<BigDecimal> result = new ArrayList<BigDecimal>(testIndexesList.size());
        for (Integer testIndex : testIndexesList) {
            if (testIndex == 0) {
                result.add(DEFAULT_TEST_SCORE);
            } else {
                BigDecimal score = points.get(testIndex);
                if (score != null) {
                    result.add(score);
                } else {
                    result.add(DEFAULT_TEST_SCORE);
                }
            }
        }
        return result;
    }

    private HashMap<Integer, BigDecimal> getAllTestPoints() {
        HashMap<Integer, BigDecimal> values = new HashMap<Integer, BigDecimal>();
        for (TestGroup group : testGroups) {
            for (Integer i : group.getTestCases()) {
                if (values.containsKey(i) && group.getPoints().compareTo(values.get(i)) <= 0) {
                    continue;
                }
                values.put(i, group.getPoints());
            }
        }
        return values;
    }

    public String getNameForTest(int index) {
        return getName() + String.format("%0" + Task.TESTCASE_PADDING_LENGTH + "d", index);
    }
}