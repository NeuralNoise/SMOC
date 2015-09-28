package bg.smoc.web.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import bg.smoc.web.utils.SessionUtil;

public class ContestSetupServlet extends HttpServlet {

	private static final long serialVersionUID = -3011913826471651846L;

	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setAttribute("contests", (SessionUtil.getInstance().getContestManager()
				.getContests()));
		request.getRequestDispatcher("contestSetup.jsp").forward(request, response);
	}

}
