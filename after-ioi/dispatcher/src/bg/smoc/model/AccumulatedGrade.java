package bg.smoc.model;

import java.util.ArrayList;
import java.util.List;

public class AccumulatedGrade {

    private List<String> testCases = new ArrayList<String>();

    private List<String> testGroups = new ArrayList<String>();

    private String total = "0";

    public List<String> getTestCases() {
        return testCases;
    }

    public void setTestCases(List<String> testCases) {
        this.testCases = testCases;
    }

    public List<String> getTestGroups() {
        return testGroups;
    }

    public void setTestGroups(List<String> testGroups) {
        this.testGroups = testGroups;
    }

    public String getTotal() {
        return total;
    }

    public void setTotal(String total) {
        this.total = total;
    }

}
