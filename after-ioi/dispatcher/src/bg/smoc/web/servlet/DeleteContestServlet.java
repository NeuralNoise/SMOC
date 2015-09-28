package bg.smoc.web.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import bg.smoc.web.utils.SessionUtil;

public class DeleteContestServlet extends HttpServlet {

	private static final long serialVersionUID = -3758822118125555274L;

	@Override
	protected void service(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		String contestId = request.getParameter("id");
		if (contestId != null) {
			SessionUtil.getInstance().getContestManager().deleteContest(contestId);
		}
		
		response.sendRedirect("contestSetup");
	}

}
