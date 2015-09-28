package bg.smoc.web.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import bg.smoc.model.UserAccount;
import bg.smoc.web.utils.SessionUtil;

public class EditAccountServlet extends HttpServlet {

	private static final long serialVersionUID = 1732743356040269247L;

	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String login = request.getParameter("login");
		UserAccount user = SessionUtil.getInstance().getUserAccountManager().getUserAccount(login);
		if (user == null) {
			response.sendRedirect("accounts");
			return;
		}

		request.setAttribute("user", user);
		request.setAttribute("contests", (SessionUtil.getInstance().getContestManager()
				.getContests()));
		request.getRequestDispatcher("editAccount.jsp").forward(request, response);
	}
}
