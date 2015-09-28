/**
 * 
 */
package bg.smoc.web.servlet.contestant;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import kr.or.ioi2002.RMIServer.User;
import bg.smoc.web.utils.SessionUtil;

/**
 * @author tsvetan.bogdanov@gmail.com
 * 
 */
public class LogIntoContestServlet extends HttpServlet {
    /**
     * 
     */
    private static final long serialVersionUID = 4621055342566626875L;

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        SessionUtil sessionUtil = SessionUtil.getInstance();
        String userLogin = sessionUtil.getLoginManager().getActiveUserLogin(request);
        String contestId = request.getParameter("contestId");
        User user = sessionUtil.getContestManager().getUser(contestId, userLogin);
        if (user != null) {
            request.getSession().setAttribute("contestId", contestId);
            response.sendRedirect("main");
        } else {
            response.sendRedirect("chooseContest");
        }
    }
}
