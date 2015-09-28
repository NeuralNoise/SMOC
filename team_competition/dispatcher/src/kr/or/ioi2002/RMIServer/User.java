/*
 * Copyright 2002 HM Research, Ltd. All rights reserved.
 */

package kr.or.ioi2002.RMIServer;

/**
 * 
 * @author Sunglim Lee
 * @version 1.00, 11/01/03
 */

import java.util.Date;
import java.util.Hashtable;

import kr.or.ioi2002.RMIServer.Job.JobType;

import bg.smoc.model.manager.JobRemovalNotified;

// TODO: This object should be removed as it does both serializing and storing
// session data.
public class User implements JobRemovalNotified {
    private boolean processingSubmit = false;

	private boolean processingTest = false;

	private byte outputSubmit[] = null;

	private Date dateLastSubmit = new Date();

	private String strLastTaskSubmit = "NA";

	private byte outputTest[] = null;

	private Date dateLastTest = new Date();

	private String strLastTaskTest = "NA";

	private UserFileManager ufm;

	// TODO:Initialize these on servlet context load !!!
	private int submitJobsPendingOnSubmit;

	private int submitJobsStillPending;

	private int submitJobId;

	private int testJobsPendingOnSubmit;

	private int testJobsStillPending;

	private int testJobId;

	public User(String contestId, String userLogin, String workingDirectory) {
		ufm = new UserFileManager(contestId, userLogin, workingDirectory);
	}

	/**
	 * @return java.util.Hashtable
	 */
	public Hashtable<String, String> getStatus() {
		Hashtable<String, String> hash = ufm.getStatus();
		; // get a copy
		hash.put("processingSubmit", String.valueOf(processingSubmit));
		hash.put("processingTest", String.valueOf(processingTest));
		String strOutputSubmit = getOutputSubmit() == null ? "" : new String(getOutputSubmit());
		String strOutputTest = getOutputTest() == null ? "" : new String(getOutputTest());
		hash.put("outputSubmit", "["
				+ Util.sdfTime.format(dateLastSubmit)
				+ "] "
				+ getStrLastTaskSubmit()
				+ "\n"
				+ strOutputSubmit);
		hash.put("outputTest", "["
				+ Util.sdfTime.format(dateLastTest)
				+ "] "
				+ getStrLastTaskTest()
				+ "\n"
				+ strOutputTest);

		return hash;
	}

	/**
	 * @return boolean
	 */
	public boolean submitStart() {
		if (processingSubmit)
			return false;
		else {
			processingSubmit = true;
			dateLastSubmit.setTime(System.currentTimeMillis());
			return true;
		}
	}

	/**
	 * @param task
	 * @param src
	 * @param language TODO
	 * @param output
	 * @return boolean
	 * @throws java.io.IOException
	 */
	public boolean submitSourceCode(String task, TempFile src, String language, String srcFilename, byte[] output)
			throws java.io.IOException {
		processingSubmit = false;
		setOutputSubmit(output);
		setStrLastTaskSubmit(task);
		return ufm.submitSourceCode(task, src, language, srcFilename, dateLastSubmit);
	}

	/**
	 * @param task
	 * @param output
	 * @return boolean
	 */
	public boolean submitAttemptFailed(String task, byte[] output) {
		processingSubmit = false;
		setStrLastTaskSubmit(task);
		if (output != null) {
		    setOutputSubmit(output);
		} else {
		    setOutputSubmit("System error!\nNo message from grader.\n".getBytes());
		}
		ufm.submitAttemptFailed(task);
		return true;
	}

	/**
	 * @return boolean
	 */
	public boolean testStart() {
		if (processingTest)
			return false;
		else {
			processingTest = true;
			dateLastTest.setTime(System.currentTimeMillis());
			return true;
		}
	}

	/**
	 * @param output
	 * @return boolean
	 */
	public boolean testFinish(String task, byte[] output) {
		processingTest = false;
		setStrLastTaskTest(task);
		setOutputTest(output);
		return true;
	}

	/**
	 * @param task
	 * @return java.io.File
	 */
	public java.io.File getSourceCode(String task) {
		return ufm.getSourceCode(task);
	}
	
	public String getSourceCodeLanguage(String task) {
		return ufm.getSourceCodeLanguage(task);
	}

	public int getSubmitProgress() {
		if (submitJobsPendingOnSubmit == 0 || submitJobsStillPending == 0)
			return 91;
		return Math.min(90, 90
				* (submitJobsPendingOnSubmit - submitJobsStillPending)
				/ submitJobsPendingOnSubmit);
	}

	public int getTestProgress() {
		if (testJobsPendingOnSubmit == 0 || testJobsStillPending == 0)
			return 91;
		return Math.min(90, 90
				* (testJobsPendingOnSubmit - testJobsStillPending)
				/ testJobsPendingOnSubmit);
	}

	public boolean notifyRemoved(Job job) {
		if (JobType.SUBMIT.equals(job.getType()) || JobType.FEEDBACK.equals(job.getType())) {
			if (job.getJobId() < submitJobId)
				submitJobsPendingOnSubmit--;
			if (job.getJobId() == submitJobId)
				return false;
		}
		if (JobType.TEST.equals(job.getType())) {
			if (job.getJobId() < testJobId)
				testJobsPendingOnSubmit--;
			if (job.getJobId() == testJobId)
				return false;
		}
		return true;
	}

	public void notifyAddition(Job job, int queueSize) {
		if (JobType.SUBMIT.equals(job.getType()) || JobType.FEEDBACK.equals(job.getType())) {
			submitJobId = job.getJobId();
			submitJobsPendingOnSubmit = queueSize;
			submitJobsStillPending = queueSize;
		}
		
        if (JobType.TEST.equals(job.getType())) {
            testJobId = job.getJobId();
            testJobsPendingOnSubmit = queueSize;
            testJobsStillPending = queueSize;
        }
	}

	public void setOutputSubmit(byte outputSubmit[]) {
		this.outputSubmit = outputSubmit;
	}

	public byte[] getOutputSubmit() {
		return outputSubmit;
	}

	public void setOutputTest(byte outputTest[]) {
		this.outputTest = outputTest;
	}

	public byte[] getOutputTest() {
		return outputTest;
	}

	public void setStrLastTaskTest(String strLastTaskTest) {
		this.strLastTaskTest = strLastTaskTest;
	}

	public String getStrLastTaskTest() {
		return strLastTaskTest;
	}

	public void setStrLastTaskSubmit(String strLastTaskSubmit) {
		this.strLastTaskSubmit = strLastTaskSubmit;
	}

	public String getStrLastTaskSubmit() {
		return strLastTaskSubmit;
	}
}
