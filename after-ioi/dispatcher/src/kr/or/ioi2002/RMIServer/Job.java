/*
 * Copyright 2002 HM Research, Ltd. All rights reserved.
 */

package kr.or.ioi2002.RMIServer;

/**
 * 
 * @author Sunglim Lee
 * @version 1.00, 11/01/03
 */

import java.io.File;

import bg.smoc.model.GradeProto.GradeResult;

public class Job {

    public enum JobType {
        TEST, SUBMIT, GRADE, SETUP,
    }

    private int jobId = 0;

    /**
     * input
     */
    private String userid;
    private String contestId;
    private JobType type;
    private String task;
    private String language;
    private String testIndex;
    private GradeResult gradeResult;
    private boolean isNotGradeFeedback = false;
    private boolean isAlwaysAccept = true;

    /**
     * public TempFile src = null; public TempFile stdin = null; output
     */
    // public byte[] output = null;
    public String result = null;
    public String gmid = null;
    public int iRetryCount = 0;

    public String srcFilename = null;
    public File src = null;
    public File stdin = null;
    public File deployFile = null; // used in DEPLOY

    /**
     * Optional
     */
    public byte[] log = null;

    public int getJobId() {
        return jobId;
    }

    public void setJobId(int jobId) {
        this.jobId = jobId;
    }

    public String getContestId() {
        return contestId;
    }

    public void setContestId(String contestId) {
        this.contestId = contestId;
    }

    public void setTask(String task) {
        this.task = task;
    }

    public String getTask() {
        return task;
    }

    public void setType(JobType type) {
        this.type = type;
    }

    public JobType getType() {
        return type;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getUserid() {
        return userid;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getLanguage() {
        return language;
    }

    public String getTestIndex() {
        return testIndex;
    }

    public void setTestIndex(String testIndex) {
        this.testIndex = testIndex;
    }

    public GradeResult getGradeResult() {
        return gradeResult;
    }

    public void setGradeResult(GradeResult gradeResult) {
        this.gradeResult = gradeResult;
    }

    public boolean isNotGradeFeedback() {
        return isNotGradeFeedback;
    }

    public void setNotGradeFeedback(boolean isNotGradeFeedback) {
        this.isNotGradeFeedback = isNotGradeFeedback;
    }
    
    public boolean isAlwaysAccept() {
        return isAlwaysAccept;
    }
    
    public void setAlwaysAccept(boolean isAlwaysAccept) {
        this.isAlwaysAccept = isAlwaysAccept;
    }
}
