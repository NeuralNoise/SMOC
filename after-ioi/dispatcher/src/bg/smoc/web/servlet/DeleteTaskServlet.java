package bg.smoc.web.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import bg.smoc.web.utils.SessionUtil;

public class DeleteTaskServlet extends HttpServlet {

	private static final long serialVersionUID = -1125669175742309311L;

	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String contestId = request.getParameter("contestId");
		String taskId = request.getParameter("taskId");
		SessionUtil.getInstance().getContestManager().deleteTask(contestId, taskId);
		
		response.sendRedirect("updateTaskList?contestId="+contestId);
	}
}
