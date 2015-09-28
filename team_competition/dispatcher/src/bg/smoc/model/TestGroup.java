package bg.smoc.model;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;

public class TestGroup {
    private List<Integer> testCases;
    private boolean isFeedbackEnabled;
    private BigDecimal points;

    public List<Integer> getTestCases() {
        return testCases;
    }

    public void setTestCases(List<Integer> testCases) {
        this.testCases = new LinkedList<Integer>(testCases);
    }

    public boolean isFeedbackEnabled() {
        return isFeedbackEnabled;
    }

    public void setFeedbackEnabled(boolean isFeedbackEnabled) {
        this.isFeedbackEnabled = isFeedbackEnabled;
    }

    public BigDecimal getPoints() {
        return points;
    }

    public void setPoints(BigDecimal points) {
        this.points = points;
    }

}
