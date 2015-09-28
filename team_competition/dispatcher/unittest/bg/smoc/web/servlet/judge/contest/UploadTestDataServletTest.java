package bg.smoc.web.servlet.judge.contest;

import java.io.FileInputStream;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import bg.smoc.model.Task;
import bg.smoc.unittest.EasyMockTest;
import bg.smoc.unittest.TestCaseWithEasyMock;

public class UploadTestDataServletTest extends TestCaseWithEasyMock {

    private UploadTestDataServlet servlet;
    
    @EasyMockTest
    public void testZipExtraction() throws ServletException, IOException {
        servlet = new UploadTestDataServlet();
        HttpServletRequest request = createMock(HttpServletRequest.class);
        HttpServletResponse response = createMock(HttpServletResponse.class);
        Task task = createTask();
        
        replayAll();
        
        servlet.unzipStream(request, response, new FileInputStream("/home/zbogi/Desktop/t1.zip"), task, null, null);
    }

    private Task createTask() {
        Task task = new Task();
        task.setId("task0");
        task.setName("t1");
        task.setNumberOfTests(5);
        task.setType(Task.PROBLEM_TYPE_STANDARD);
        return task;
    }
}
