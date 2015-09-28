package bg.smoc.web.servlet.judge.contest;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import bg.smoc.model.Task;
import bg.smoc.model.manager.ContestManager;
import bg.smoc.model.serializer.FileInfo;
import bg.smoc.web.utils.FileUtils;
import bg.smoc.web.utils.SessionUtil;

public class DownloadTestDataServlet extends HttpServlet {

    /**
     * 
     */
    private static final long serialVersionUID = -836352779255986453L;

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // http://localhost:8080/judge/downloadTest?contestId=Contest_0&taskId=Contest_0_0&testNum=1&type=SOL
        String contestId = request.getParameter("contestId");
        String taskId = request.getParameter("taskId");
        String testNum = request.getParameter("testNum");
        String testType = request.getParameter("type");
        ContestManager contestManager = SessionUtil.getInstance().getContestManager();
        Task task = contestManager.getTask(contestId, taskId);
        if (task == null || testType == null || testNum == null) {
            response.sendRedirect("");
            return;
        }
        int testNumber = -1;
        try {
            testNumber = Integer.parseInt(testNum);
        } catch (NumberFormatException e) {
            response.sendRedirect("");
            return;
        }
        Task.TestType type;
        try {
            type = Task.TestType.valueOf(testType);
        } catch (IllegalArgumentException e) {
            response.sendRedirect("");
            return;
        }
        FileInfo fileInfo = contestManager.getTestDataFile(contestId,
                task.getName(),
                testNumber,
                type);
        if (fileInfo == null || fileInfo.getSize() > Integer.MAX_VALUE) {
            response.sendRedirect("");
            return;
        }

        response.setContentType("plain/text");
        response.setContentLength((int) fileInfo.getSize());
        response.setHeader("Content-Disposition", "attachment; filename=\""
                + new File(fileInfo.getAbsolutePath()).getName()
                + "\"");
        
        FileUtils.copyStreams(fileInfo.getInputStream(), response.getOutputStream());
        fileInfo.getInputStream().close();
    }
}
