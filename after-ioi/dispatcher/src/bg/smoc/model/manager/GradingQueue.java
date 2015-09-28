/**
 * 
 */
package bg.smoc.model.manager;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import kr.or.ioi2002.RMIServer.Job;
import kr.or.ioi2002.RMIServer.Job.JobType;
import kr.or.ioi2002.RMIServer.agent.LogGraderAgent;

/**
 * @author tsvetan.bogdanov@gmail.com
 * 
 */
public class GradingQueue {
    protected Set<JobRemovalNotified> notifiedOnSubmitRemovalSet = new HashSet<JobRemovalNotified>();
    protected Set<JobRemovalNotified> notifiedOnTestRemovalSet = new HashSet<JobRemovalNotified>();
    protected Set<JobRemovalNotified> notifiedOnGradeRemovalSet = new HashSet<JobRemovalNotified>();

    GradingQueueImpl implementation;

    public GradingQueue(String workingDirectory) {
        implementation = new GradingQueueImpl(workingDirectory);
        implementation.load();
    }

    // TODO:This method's implementation is not correct in the new
    // structure. And it should probably be renamed.
    public void rePush(Job job) {
        LogGraderAgent.log("dispatchGrade: putback occured");
        push(job, null);
    }

    public synchronized void push(Job job, JobRemovalNotified notified) {
        Job modifiedJob = implementation.addJob(job);
        notifyAddition(notified, modifiedJob);
    }

    public synchronized Job popJob(JobType jobType) {
        Job job = implementation.removeJob(jobType);
        if (job == null)
            return null;

        notifyRemoval(job);
        return job;
    }

    public synchronized boolean hasJob(JobType jobType) {
        return implementation.hasJob(jobType);
    }

    public void markSuccessfullyAssignedJob(Job job) {
    }

    private Set<JobRemovalNotified> getNotificationSet(JobType jobType) {
        if (JobType.GRADE == jobType)
            return notifiedOnGradeRemovalSet;
        if (JobType.SUBMIT == jobType)
            return notifiedOnSubmitRemovalSet;
        if (JobType.TEST == jobType)
            return notifiedOnTestRemovalSet;
        throw new Error("No such job type exists:" + jobType);
    }

    public List<Job> getQueue(JobType jobType) {
        return implementation.getQueueImpl(jobType);
    }

    private void notifyAddition(JobRemovalNotified notified, Job modifiedJob) {
        Set<JobRemovalNotified> notificationSet = getNotificationSet(modifiedJob.getType());
        if (notified != null) {
            notified.notifyAddition(modifiedJob, implementation.getQueueImpl(modifiedJob.getType())
                    .size());
            synchronized (notificationSet) {
                notificationSet.add(notified);
            }
        }
    }

    private void notifyRemoval(Job job) {
        Set<JobRemovalNotified> notificationSet = getNotificationSet(job.getType());
        synchronized (notificationSet) {
            for (Iterator<JobRemovalNotified> iter = notificationSet.iterator();iter.hasNext();) {
                JobRemovalNotified notified = iter.next();
                if (!notified.notifyRemoved(job)) {
                    iter.remove();
                }
            }
        }
    }

}
