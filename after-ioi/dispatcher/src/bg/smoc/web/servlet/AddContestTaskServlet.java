package bg.smoc.web.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import bg.smoc.model.Task;
import bg.smoc.web.utils.SessionUtil;

public class AddContestTaskServlet extends HttpServlet {

	private static final long serialVersionUID = -3837067568576496764L;

	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String contestId = request.getParameter("contestId");
		Task task = new Task();
        SessionUtil.getInstance().getContestManager().addTask(contestId, task);

		response.sendRedirect("editTask?contestId=" + contestId + "&taskId=" + task.getId());
	}
}
