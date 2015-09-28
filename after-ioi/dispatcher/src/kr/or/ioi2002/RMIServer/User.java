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

import bg.smoc.model.manager.JobRemovalNotified;

// TODO: This object should be removed as it does both serializing and storing
// session data.
public class User {
    private static final String GENERAL = "#General#"; // TODO: move somewhere
    // else
    private static final String TEST = "#TEST#"; // TODO: move somewhere else

    // Includes GENERAL AND TEST
    public class SubmitState implements JobRemovalNotified {
        private boolean processing = false;
        private String output = "";
        private Date dateLast = new Date();
        private int jobId = -1;
        private int jobsPendingOnSubmit = 0;
        private int jobsStillPending = 0;

        public void setProcessing(boolean processing) {
            this.processing = processing;
        }

        public boolean isProcessing() {
            return processing;
        }

        public void setOutput(String output) {
            this.output = output;
        }
        
        public void setOutputNow(String output) {
            dateLast.setTime(System.currentTimeMillis());
            this.output = output;
        }

        public String getOutput() {
            return output;
        }

        public void setDateLast(Date dateLast) {
            this.dateLast = dateLast;
        }

        public Date getDateLast() {
            return dateLast;
        }

        public void setJobId(int jobId) {
            this.jobId = jobId;
        }

        public int getSubmitJobId() {
            return jobId;
        }

        /**
         * @return boolean
         */
        public boolean start() {
            if (processing)
                return false;
            else {
                processing = true;
                dateLast.setTime(System.currentTimeMillis());
                return true;
            }
        }

        public boolean finished(String output) {
            this.processing = false;
            this.output = output;
            return true;
        }

        public int getProgress() {
            if (jobsPendingOnSubmit == 0 || jobsStillPending == 0)
                return 91;
            return Math
                    .min(90, 90 * (jobsPendingOnSubmit - jobsStillPending) / jobsPendingOnSubmit);
        }

        public boolean notifyRemoved(Job job) {
            if (job.getJobId() < jobId)
                jobsPendingOnSubmit--;
            if (job.getJobId() == jobId)
                return false;
            return true;
        }

        public void notifyAddition(Job job, int queueSize) {
            jobId = job.getJobId();
            jobsPendingOnSubmit = queueSize;
            jobsStillPending = queueSize;
        }
        
        // TODO: maybe for someplace else 
        public String getContent() {
            if (isProcessing()) {
                return ("Processing " + String.valueOf(getProgress()) + "% ...\n" +
                        "Please Reload...");
            }
            else {
                return ("[" + Util.DATETIME_FORMAT_TIME.format(getDateLast()) + "]\n" +
                        getOutput());
            }
        }
    }

    private Hashtable<String, SubmitState> submitStates;

    private String strLastTaskTest = "N/A"; // TODO:

    private UserFileManager ufm;

    public User(String contestId, String userLogin, String workingDirectory) {
        ufm = new UserFileManager(contestId, userLogin, workingDirectory);
        submitStates = new Hashtable<String, SubmitState>();
    }

    /**
     * @return java.util.Hashtable
     */
    public Hashtable<String, String> getStatus() {
        Hashtable<String, String> hash = ufm.getStatus(); // get a copy
        return hash;
    }


    /**
     * @param task
     * @param src
     * @param language
     * @param output
     * @return boolean
     * @throws java.io.IOException
     */
    public boolean submitSourceCode(String task, TempFile src, String language, String srcFilename,
            String output) throws java.io.IOException {
        getSubmitState(task).finished(output);
        return ufm.submitSourceCode(task, src, language, srcFilename, getSubmitState(task)
                .getDateLast());
    }

    /**
     * @param task
     * @param output
     * @return boolean
     */
    public boolean submitAttemptFailed(String task, String output) {
        if (output == null)
            output = "System error!\nNo message from grader.\n";
        getSubmitState(task).finished(output);
        ufm.submitAttemptFailed(task);
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

    public void setStrLastTaskTest(String strLastTaskTest) {
        this.strLastTaskTest = strLastTaskTest;
    }

    public String getStrLastTaskTest() {
        return strLastTaskTest;
    }

    /**
     * @param component
     * @return SubmitState not null
     */
    public SubmitState getSubmitState(String component) {
        component = component.split("\\d")[0];
        SubmitState state = submitStates.get(component);
        if (state == null) {
            state = new SubmitState();
            submitStates.put(component, state);
        }
        return state;
    }

    public SubmitState getTestState() {
        return getSubmitState(TEST);
    }

    public SubmitState getGeneralState() {
        return getSubmitState(GENERAL);
    }
}
