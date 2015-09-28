package bg.smoc.model;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

public class TaskTest {

    @Test
    public void testGetTestPoints() {
        Task task = new Task();
        task.setNumberOfTests(3);
        task.setTestGroups(generateTestGroups());

        List<BigDecimal> result = task.getTestsPoints(Arrays.asList(0, 1, 2, 3));
        Assert.assertEquals(4, result.size());
        Assert.assertEquals(new BigDecimal(10), result.get(0));
        Assert.assertEquals(new BigDecimal(20), result.get(1));
        Assert.assertEquals(new BigDecimal(20), result.get(2));
        Assert.assertEquals(new BigDecimal(20), result.get(3));
    }

    private List<TestGroup> generateTestGroups() {
        return Arrays.asList(createGroup(1, 20), createGroup(2, 20), createGroup(3, 20));
    }

    private TestGroup createGroup(int index, int maxPoints) {
        TestGroup group = new TestGroup();
        group.setFeedbackEnabled(true);
        group.setTestCases(Arrays.asList(index));
        group.setPoints(new BigDecimal(maxPoints));
        return group;
    }
}
