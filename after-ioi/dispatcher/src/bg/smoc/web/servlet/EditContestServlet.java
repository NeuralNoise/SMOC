package bg.smoc.web.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import bg.smoc.model.Contest;
import bg.smoc.web.utils.SessionUtil;

public class EditContestServlet extends HttpServlet {

	private static final long serialVersionUID = -3305732065681942630L;

	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String contestId = request.getParameter("id");
		if (contestId == null) {
			response.sendRedirect("contestSetup");
			return;
		}

		Contest contest = SessionUtil.getInstance().getContestManager().getContest(contestId);
		request.setAttribute("contest", contest);

		request.getRequestDispatcher("editContest.jsp").forward(request, response);
	}
}
