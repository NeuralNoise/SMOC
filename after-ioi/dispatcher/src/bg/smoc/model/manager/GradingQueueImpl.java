package bg.smoc.model.manager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;

import kr.or.ioi2002.RMIServer.Job;
import kr.or.ioi2002.RMIServer.Job.JobType;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

public class GradingQueueImpl {

    protected LinkedList<Job> submitQueue;
    protected LinkedList<Job> testQueue;
    protected LinkedList<Job> gradeQueue;
    protected LinkedList<Job> setupQueue;
    private String workingDirectory;

    public GradingQueueImpl(String workingDirectory) {
        this.workingDirectory = workingDirectory;
    }

    public void load() {
        XStream xstream = new XStream(new DomDriver());
        submitQueue = deserializeQueue(xstream, "submit_queue.xml");
        testQueue = deserializeQueue(xstream, "test_queue.xml");
        gradeQueue = deserializeQueue(xstream, "grade_queue.xml");
        setupQueue = deserializeQueue(xstream, "setup_queue.xml");
    }

    @SuppressWarnings("unchecked")
    private LinkedList<Job> deserializeQueue(XStream xstream, String fileName) {
        try {
            FileInputStream fileStream = new FileInputStream(new File(workingDirectory, fileName));
            LinkedList<Job> queue = (LinkedList<Job>) xstream.fromXML(fileStream);
            fileStream.close();
            if (queue != null)
                return queue;
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new LinkedList<Job>();
    }

    public LinkedList<Job> getSubmitQueue() {
        return submitQueue;
    }

    public LinkedList<Job> getTestQueue() {
        return testQueue;
    }

    public LinkedList<Job> getGradeQueue() {
        return gradeQueue;
    }
    
    private void store() {
        XStream xstream = new XStream(new DomDriver());
        serializeQueue(xstream, "submit_queue.xml", submitQueue);
        serializeQueue(xstream, "test_queue.xml", testQueue);
        serializeQueue(xstream, "grade_queue.xml", gradeQueue);
        serializeQueue(xstream, "setup_queue.xml", setupQueue);
    }

    private void serializeQueue(XStream xstream, String fileName, LinkedList<Job> queue) {
        try {
            FileOutputStream fileStream = new FileOutputStream(new File(workingDirectory, fileName));
            xstream.toXML(queue, fileStream);
            fileStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Job removeJob(JobType jobType) {
        LinkedList<Job> queue = getQueueImpl(jobType);
        synchronized (queue) {
            if (queue.isEmpty())
                return null;
            Job job = queue.remove();
            store();
            return job;
        }
    }

    public boolean hasJob(JobType jobType) {
        return !getQueueImpl(jobType).isEmpty();
    }

    public Job addJob(Job job) {
        LinkedList<Job> queue = getQueueImpl(job.getType());
        synchronized (queue) {
            job.setJobId(getNextIdFromQueue(queue));
            queue.add(job);
            store();
        }
        return job;
    }

    public LinkedList<Job> getQueueImpl(JobType jobType) {
        if (JobType.GRADE == jobType)
            return gradeQueue;
        if (JobType.SUBMIT == jobType)
            return submitQueue;
        if (JobType.TEST == jobType)
            return testQueue;
        if (JobType.SETUP == jobType)
            return setupQueue;
        
        throw new Error("No such job type exists:" + jobType);
    }

    private int getNextIdFromQueue(LinkedList<Job> queue) {
        if (queue.isEmpty())
            return 1;

        return queue.getLast().getJobId() + 1;
    }

}
