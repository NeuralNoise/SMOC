package bg.smoc.web.servlet;

import java.io.IOException;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import bg.smoc.model.Task;
import bg.smoc.model.TaskType;
import bg.smoc.web.utils.SessionUtil;

public class UpdateTaskListServlet extends HttpServlet {

	private static final long serialVersionUID = 4121634480950273292L;

	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String contestId = request.getParameter("contestId");
		Vector<Task> tasks = SessionUtil.getInstance().getContestManager().getTasks(contestId);
		request.setAttribute("tasks", tasks);
		request.setAttribute("contestId", contestId);
		request.setAttribute("taskTypes", TaskType.getAllTaskTypes());

		request.getRequestDispatcher("updateTaskList.jsp").forward(request, response);
	}
}
