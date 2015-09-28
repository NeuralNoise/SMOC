package bg.smoc.web.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import bg.smoc.model.Contest;
import bg.smoc.model.manager.ContestManager;
import bg.smoc.web.utils.SessionUtil;

public class StopContestServlet extends HttpServlet {

	private static final long serialVersionUID = 8968095236135506252L;

	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String contestId = request.getParameter("id");
		ContestManager contestManager = SessionUtil.getInstance().getContestManager();
		Contest contest = contestManager.getContest(contestId);
		contest.setRunning(false);
		contest.setTestingOn(false);
		contestManager.updateContest(contest);
		contestManager.cancelContest(contest);
		response.sendRedirect("contestSetup");
	}

}
