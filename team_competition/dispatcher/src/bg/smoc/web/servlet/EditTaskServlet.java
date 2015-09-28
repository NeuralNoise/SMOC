package bg.smoc.web.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import bg.smoc.model.Task;
import bg.smoc.model.TaskType;
import bg.smoc.model.manager.ContestManager;
import bg.smoc.web.utils.SessionUtil;

public class EditTaskServlet extends HttpServlet {

	private static final long serialVersionUID = 4948961990273099321L;

	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String contestId = request.getParameter("contestId");
		String taskId = request.getParameter("taskId");
		ContestManager contestManager = SessionUtil.getInstance().getContestManager();
        Task task = contestManager.getTask(contestId, taskId);

		request.setAttribute("contestId", contestId);
		request.setAttribute("task", task);
		request.setAttribute("taskTypes", TaskType.getAllTaskTypes());
		request.setAttribute("taskTests", contestManager.getTaskTests(contestId, task));
		request.setAttribute("testGroups", task.getTestGroups());

		request.getRequestDispatcher("editTask.jsp").forward(request, response);
	}
}
