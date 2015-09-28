package bg.smoc.web.servlet.person;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import bg.smoc.model.Person;
import bg.smoc.model.manager.PersonManager;
import bg.smoc.web.utils.SessionUtil;

public class RemovePersonLoginServlet extends HttpServlet {

	private static final long serialVersionUID = 4505374554496042806L;

	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String personId = request.getParameter("personId");
		String login = request.getParameter("login");

		PersonManager personManager = SessionUtil.getInstance().getPersonManager();
		Person person = personManager.getPerson(personId);
		person.getLogins().remove(login);
		personManager.update(person);

		response.sendRedirect("editPerson?personId=" + personId);
	}
}
