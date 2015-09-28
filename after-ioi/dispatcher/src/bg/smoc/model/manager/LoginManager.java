/**
 * 
 */
package bg.smoc.model.manager;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;

import kr.or.ioi2002.RMIServer.LogIPConsistency;
import kr.or.ioi2002.RMIServer.LogLogin;
import bg.smoc.web.utils.ServletUtil;

/**
 * @author tsvetan.bogdanov@gmail.com
 * 
 * This class should have a single instance, available for use through the
 * SessionUtil. It handles user login and logout as well as password
 * verification.
 * 
 * TODO: Store the userLastIPAddress map in consistent-store and use it only
 * where needed. TODO: Start using Log4j instead of LogIPConsistency and
 * LogLogin objects.
 */
public class LoginManager extends GenericManager {

    private HashMap<String, String> userLastIPAddress = new HashMap<String, String>();

    public LoginManager() {
    }

    /**
     * Returns whether the supplied login and password are valid and log the
     * log-in attempt.
     * 
     * @param login
     *            username supplied by the person trying to log in
     * @param password
     *            supplied password by the user
     * @param clientIP
     *            IPAddress from which the log-in request was made
     * @return whether the user can be logged in with this username and password
     * @throws UnsupportedEncodingException
     * @throws NoSuchAlgorithmException
     */
    public boolean isLoginValid(String login, String password, String clientIP) {
        if (clientIP == null)
            clientIP = "IP_NA";

        if (password == null) {
            LogLogin.log(login + ",FAIL," + clientIP + ",Wrong password");
            return false;
        }

        if (!mediator.verifyUserPassword(login, ServletUtil.encryptPassword(password))) {
            LogLogin.log(login + ",FAIL," + clientIP + ",Invalid username or password");
            return false;
        }

        LogLogin.log(login + ",OK," + clientIP);
        checkForIPAddressConsistency(login, clientIP);
        return true;
    }

    private void checkForIPAddressConsistency(String login, String clientIP) {
        String previousAddress = userLastIPAddress.put(login, clientIP);
        if (previousAddress != null && !previousAddress.equals(clientIP)) {
            LogIPConsistency.log(login + "," + previousAddress + "," + clientIP);
        }
    }

    public void initiateSession(HttpServletRequest request, String login) {
        request.getSession().setAttribute("id", login);
    }

    public void finalizeSession(HttpServletRequest request) {
        request.getSession().setAttribute("id", null);
    }

    public String getActiveUserLogin(HttpServletRequest request) {
        return (String) request.getSession().getAttribute("id");
    }

}
