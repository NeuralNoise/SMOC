/**
 * 
 */
package bg.smoc.model.manager;

import bg.smoc.model.manager.GradingQueue;
import junit.framework.TestCase;
import kr.or.ioi2002.RMIServer.Job;
import kr.or.ioi2002.RMIServer.Job.JobType;

/**
 * @author tsvetan.bogdanov@gmail.com
 * 
 */
public class GraderManagerTest extends TestCase {

    /*
     * (non-Javadoc)
     * 
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
    }

    /*
     * (non-Javadoc)
     * 
     * @see junit.framework.TestCase#tearDown()
     */
    @Override
    protected void tearDown() throws Exception {
    }

    public void testSerialization() {
        Job job = new Job();
        job.setType(JobType.SUBMIT);
        {
            GradingQueue gradingQueueManager = new GradingQueue("./");
            gradingQueueManager.push(job, null);
        }
        {
            GradingQueue gradingQueueManager = new GradingQueue("./");
            assertEquals(1, gradingQueueManager.getQueue(JobType.SUBMIT).size());
        }
    }

}
