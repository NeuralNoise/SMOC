package bg.smoc.web.servlet.person;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import bg.smoc.web.utils.SessionUtil;

public class SelectPersonServlet extends HttpServlet {

	private static final long serialVersionUID = 8795379903390138700L;

	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setAttribute("login", request.getParameter("login"));
		request.setAttribute("persons", (SessionUtil.getInstance().getPersonManager()
				.getAllPersons()));

		request.getRequestDispatcher("selectPerson.jsp").forward(request, response);
	}
}
