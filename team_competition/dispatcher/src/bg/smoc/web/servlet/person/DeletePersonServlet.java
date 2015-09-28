package bg.smoc.web.servlet.person;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import bg.smoc.web.utils.SessionUtil;

public class DeletePersonServlet extends HttpServlet {

	private static final long serialVersionUID = 1151670282434844290L;

	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String personId = request.getParameter("personId");
		SessionUtil.getInstance().getPersonManager().delete(personId);

		response.sendRedirect("persons");
	}

}
