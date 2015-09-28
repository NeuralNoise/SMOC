package bg.smoc.web.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import bg.smoc.web.utils.SessionUtil;

public class DeleteAccountServlet extends HttpServlet {

	private static final long serialVersionUID = -6778318371115661794L;

	@Override
	protected void service(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		String login = request.getParameter("login");
		SessionUtil.getInstance().getUserAccountManager().delete(login);
		
		response.sendRedirect("accounts");
	}
}
