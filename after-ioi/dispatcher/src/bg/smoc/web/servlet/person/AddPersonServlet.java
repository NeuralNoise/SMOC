package bg.smoc.web.servlet.person;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import bg.smoc.model.Person;
import bg.smoc.web.utils.SessionUtil;

public class AddPersonServlet extends HttpServlet {

	private static final long serialVersionUID = -7502991439138101734L;

	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		Person person = new Person();
		person.setNames(request.getParameter("names"));
		person.setSchool(request.getParameter("school"));
		try {
		    person.setSchoolYear(Integer.parseInt(request.getParameter("schoolYear")));
		} catch (NumberFormatException exception) {		    
		}
		person.setTown(request.getParameter("town"));
		SessionUtil.getInstance().getPersonManager().addPerson(person);

		response.sendRedirect("persons");
	}
}
