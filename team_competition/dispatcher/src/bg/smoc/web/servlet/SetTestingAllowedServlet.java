package bg.smoc.web.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import bg.smoc.model.Contest;
import bg.smoc.model.manager.ContestManager;
import bg.smoc.web.utils.SessionUtil;

public class SetTestingAllowedServlet extends HttpServlet {

	private static final long serialVersionUID = -5390064906133463505L;

	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		boolean value = Boolean.parseBoolean(request.getParameter("value"));
		String contestId = request.getParameter("id");

		ContestManager contestManager = SessionUtil.getInstance().getContestManager();
		Contest contest = contestManager.getContest(contestId);
		contest.setTestingOn(value);
		contestManager.updateContest(contest);

		response.sendRedirect("contestSetup");
	}

}
