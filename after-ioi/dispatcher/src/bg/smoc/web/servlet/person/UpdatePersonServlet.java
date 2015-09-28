package bg.smoc.web.servlet.person;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import bg.smoc.model.Person;
import bg.smoc.model.manager.PersonManager;
import bg.smoc.web.utils.SessionUtil;

public class UpdatePersonServlet extends HttpServlet {

	private static final long serialVersionUID = 2679642844221004340L;

	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String personId = request.getParameter("id");

		PersonManager personManager = SessionUtil.getInstance().getPersonManager();
		Person person = personManager.getPerson(personId);
		if (person != null) {
			person.setNames(request.getParameter("names"));
			person.setTown(request.getParameter("town"));
			person.setSchool(request.getParameter("school"));
			person.setSchoolYear(Integer.parseInt(request.getParameter("schoolYear")));
			personManager.update(person);
		}

		response.sendRedirect("persons");
	}
}
