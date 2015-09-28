/**
 * 
 */
package bg.smoc.web.servlet.contestant;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import bg.smoc.model.manager.LoginManager;
import bg.smoc.web.utils.SessionUtil;

/**
 * @author tsvetan.bogdanov@gmail.com
 * 
 */
public class ContestantLoginServlet extends HttpServlet {

	private static final long serialVersionUID = -6115442374691006590L;

	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String login = extractRequestParam(request, "id");
		String password = extractRequestParam(request, "password");
		String clientIP = request.getRemoteAddr();

		LoginManager loginManager = SessionUtil.getInstance().getLoginManager();
		if (loginManager.isLoginValid(login, password, clientIP)) {
			loginManager.initiateSession(request, login);
			response.sendRedirect("chooseContest");
		} else {
			loginManager.finalizeSession(request);
			response.setContentType("text/html");
			response.getOutputStream().println("<script language=\"javascript\">"
					+ "alert(\"Login Failed\");"
					+ "history.go(-1);"
					+ "</script>");
		}
	}

	/**
	 * Extracts the request parameter with name paramName. If non-null returns
	 * the trimmed value otherwise null.
	 * 
	 * @param request
	 *			the request whose parameter is extracted
	 * @param paramName
	 *			the parameter name
	 * @return the trimmed string value of the parameter or null
	 */
	private String extractRequestParam(HttpServletRequest request, String paramName) {
		String param = request.getParameter(paramName);
		if (param == null)
			return null;

		return param.trim();
	}
}
