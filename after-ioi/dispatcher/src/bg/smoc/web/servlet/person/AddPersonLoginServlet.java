package bg.smoc.web.servlet.person;

import java.io.IOException;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import bg.smoc.model.Person;
import bg.smoc.model.UserAccount;
import bg.smoc.web.utils.SessionUtil;

public class AddPersonLoginServlet extends HttpServlet {

	private static final long serialVersionUID = -3458635172211160234L;

	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String personId = request.getParameter("personId");
		String login = request.getParameter("login");

		SessionUtil sessionUtil = SessionUtil.getInstance();
		Person person = sessionUtil.getPersonManager().getPerson(personId);
		UserAccount userAccount = sessionUtil.getUserAccountManager().getUserAccount(login);
		if (userAccount != null) {
			if (person.getLogins() == null)
				person.setLogins(new Vector<String>());
			person.getLogins().add(login);
			sessionUtil.getPersonManager().update(person);
		}

		String page = request.getParameter("page");
		if (page == null)
			response.sendRedirect("editPerson?personId=" + personId);
		else
			response.sendRedirect(page);
	}
}
