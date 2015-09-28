/**
 * 
 */
package bg.smoc.model.manager;

import kr.or.ioi2002.RMIServer.Job;

/**
 * @author tsvetan.bogdanov@gmail.com
 * 
 */
public interface JobRemovalNotified {
	/**
	 * Called once just after the job is created and added to the queue. It is
	 * guaranteed that jobs in the queue have growing jobIds.
	 * 
	 * @param job
	 *            the job that was just added, with the jobId set
	 * @param queueSize
	 *            the size of the queue just after adding the job.
	 */
	public void notifyAddition(Job job, int queueSize);

	/**
	 * Called every time a job is completely removed from the queue.
	 * 
	 * @param job
	 *            the job that has just been removed
	 * @return whether this object should still receive further notifications
	 *         about finished jobs
	 */
	public boolean notifyRemoved(Job job);
}
