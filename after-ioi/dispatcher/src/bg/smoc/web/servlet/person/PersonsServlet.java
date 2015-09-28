package bg.smoc.web.servlet.person;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import bg.smoc.web.utils.SessionUtil;

public class PersonsServlet extends HttpServlet {

	private static final long serialVersionUID = -4339777113286444981L;

	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setAttribute("persons", (SessionUtil.getInstance().getPersonManager()
				.getAllPersons()));

		request.getRequestDispatcher("persons.jsp").forward(request, response);
	}

}
