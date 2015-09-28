package bg.smoc.model;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

public class Task implements Serializable {

    public enum TestType {
        IN, SOL
    }

    private static final long serialVersionUID = 999981131076839340L;

    public static final int PROBLEM_TYPE_STANDARD = 1;
    public static final int PROBLEM_TYPE_OUTPUT = 2;
    public static final int PROBLEM_TYPE_MODULE = 3;

    // TODO: export it somewhere else
    public static final int TESTCASE_PADDING_LENGTH = 3;

    private String name;

    private Integer type;

    private int numberOfTests;

    private String id;

    private int maxSubmitSize = 50 * 1024;

    private List<TestGroup> testGroups;

    private boolean feedbackEnabled;

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
        return testGroups;
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
}