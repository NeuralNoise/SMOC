package bg.smoc.web.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import bg.smoc.model.Contest;
import bg.smoc.web.utils.ServletUtil;
import bg.smoc.web.utils.SessionUtil;

public class AddNewContestServlet extends HttpServlet {

	private static final long serialVersionUID = -5536007961372634443L;

	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		SessionUtil.getInstance().getContestManager()
				.addContest(populateContestFromRequest(request));

		response.sendRedirect("contestSetup");
	}

	private Contest populateContestFromRequest(HttpServletRequest request) {
		Contest contest = new Contest();
		contest.setName(request.getParameter("name"));
		contest.setOpenContest(ServletUtil.isCheckboxSelected(request.getParameter("isPublic")));
		contest.setShortName(request.getParameter("shortName"));
		contest.setMaxUploadSize(Integer.parseInt(request.getParameter("maxUploadSize")));
		return contest;
	}

}
