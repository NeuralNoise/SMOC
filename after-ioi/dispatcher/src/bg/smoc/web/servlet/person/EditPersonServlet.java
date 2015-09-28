package bg.smoc.web.servlet.person;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import bg.smoc.web.utils.SessionUtil;

public class EditPersonServlet extends HttpServlet {

	private static final long serialVersionUID = -1692899330497385710L;

	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String personId = request.getParameter("personId");
		request.setAttribute("person", SessionUtil.getInstance().getPersonManager()
				.getPerson(personId));

		request.getRequestDispatcher("editPerson.jsp").forward(request, response);
	}
}
