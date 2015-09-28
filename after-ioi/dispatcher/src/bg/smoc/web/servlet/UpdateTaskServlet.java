package bg.smoc.web.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import bg.smoc.model.Task;
import bg.smoc.model.manager.ContestManager;
import bg.smoc.web.utils.SessionUtil;

public class UpdateTaskServlet extends HttpServlet {

    private static final long serialVersionUID = 7197263366728454198L;

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String contestId = request.getParameter("contestId");
        String taskId = request.getParameter("id");

        ContestManager contestManager = SessionUtil.getInstance().getContestManager();
        Task task = contestManager.getTask(contestId, taskId);

        if (task != null) {
            task.setName(request.getParameter("name"));
            try {
                task.setType(new Integer(request.getParameter("type")));
                task.setNumberOfTests(Integer.parseInt(request.getParameter("numberOfTests")));
                task.setMaxSubmitSize(Integer.parseInt(request.getParameter("maxSubmitSize")));
                task.setTimeLimit(Integer.parseInt(request.getParameter("timeLimit")));
                task.setMemoryLimit(Integer.parseInt(request.getParameter("memoryLimit")));
                task.setOutputLimit(Integer.parseInt(request.getParameter("outputLimit")));
            } catch (NumberFormatException e) {
            }
            contestManager.updateTask(contestId, task);
        }

        response.sendRedirect("updateTaskList?contestId=" + contestId);
    }
}
