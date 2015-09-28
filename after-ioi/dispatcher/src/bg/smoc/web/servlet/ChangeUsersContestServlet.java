package bg.smoc.web.servlet;

import java.io.IOException;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import bg.smoc.model.UserAccount;
import bg.smoc.model.manager.UserAccountManager;
import bg.smoc.web.utils.ServletUtil;
import bg.smoc.web.utils.SessionUtil;

public class ChangeUsersContestServlet extends HttpServlet {

	private static final String REQUEST_PARAM_PREFIX = "changeForUser";

	private static final long serialVersionUID = 2760082068230445866L;

	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String contestId = request.getParameter("selectedContest");

		SessionUtil sessionUtil = SessionUtil.getInstance();
        UserAccountManager userAccountManager = sessionUtil.getUserAccountManager();
		Vector<UserAccount> users = userAccountManager.getAllUsers();
		for (UserAccount userAccount : users) {
			if (ServletUtil.isCheckboxSelected(request.getParameter(REQUEST_PARAM_PREFIX
					+ userAccount.getLogin()))) {
			    userAccount.getContestIds().add(contestId);
				userAccountManager.update(userAccount);
                sessionUtil.getContestManager().registerUserForContest(contestId, userAccount.getLogin());
			}
		}
		response.sendRedirect("accounts");
	}
}
