/**
 * 
 */
package bg.smoc.web.servlet.contestant;

import java.io.IOException;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import bg.smoc.model.Person;
import bg.smoc.model.UserAccount;
import bg.smoc.web.utils.ServletUtil;
import bg.smoc.web.utils.SessionUtil;

/**
 * @author tsvetan.bogdanov@gmail.com
 * 
 */
public class RegisterServlet extends HttpServlet {

    /**
     * 
     */
    private static final long serialVersionUID = 4929305359709832060L;

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        UserAccount userAccount = new UserAccount();
        Person person = new Person();
        if (isFormFilledCorrectly(request, userAccount, person)) {
            userAccount.setPasswordHash(ServletUtil.encryptPassword(userAccount.getPassword()));
            userAccount.setPassword(null);

            person.getLogins().add(userAccount.getLogin());

            SessionUtil util = SessionUtil.getInstance();
            util.getUserAccountManager().createUser(userAccount);
            util.getPersonManager().addPerson(person);

            response.sendRedirect("/");
        } else {
            request.setAttribute("username", userAccount.getLogin());
            request.setAttribute("password", userAccount.getPassword());
            request.setAttribute("names", person.getNames());
            request.setAttribute("town", person.getTown());
            request.setAttribute("email", person.getEmail());

            request.getRequestDispatcher("registerUser.jsp").forward(request, response);
        }
    }

    private boolean isFormFilledCorrectly(HttpServletRequest request, UserAccount userAccount,
            Person person) {
        if (request.getParameter("submitted") == null)
            return false;

        userAccount.setLogin(request.getParameter("username"));
        userAccount.setPassword(request.getParameter("password"));

        if (isEmpty(userAccount.getLogin()) ||
            !Pattern.matches("(\\w|[.#@!_])+", userAccount.getLogin()) ||
            userAccount.getLogin().startsWith(".") // can result in CWD exploit 
            ) {
            request.setAttribute("errorMessage", "Login is not valid.");
            return false;
        }

        if (SessionUtil.getInstance().getUserAccountManager()
                .getUserAccount(userAccount.getLogin()) != null) {
            request.setAttribute("errorMessage", "Login is already taken.");
            return false;
        }

        if (isEmpty(userAccount.getPassword())) {
            logError(request, "Password is not valid");
            return false;
        }

        if ((request.getParameter("repassword") == null) ||
            !userAccount.getPassword().equals(request.getParameter("repassword"))) {
            logError(request, "The re-entered password doesn't match.");
            return false;
        }

        person.setNames(request.getParameter("names"));
        person.setTown(request.getParameter("town"));
        person.setEmail(request.getParameter("email"));
        if (isInvalidName(person.getNames())) {
            logError(request, "Names are invalid.");
            return false;
        }

        if (isInvalidName(person.getTown())) {
            logError(request, "Town is invalid.");
            return false;
        }

        if (isEmpty(person.getEmail()) || person.getEmail().indexOf("@") == -1) {
            logError(request, "Email address is invalid.");
            return false;
        }

        return true;
    }

    private boolean isEmpty(String token) {
        if ((token == null) || (token == "") || (token.length() < 0))
            return true;

        return false;
    }
    
    private boolean isInvalidName(String token) {
        return isEmpty(token) || token.contains("<") || token.contains(">") || token.contains("&");
    }

    private void logError(HttpServletRequest request, String errorMessage) {
        request.setAttribute("errorMessage", errorMessage);
    }
}
