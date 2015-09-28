/**
 * 
 */
package bg.smoc.web.servlet.contestant;

import java.io.IOException;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import bg.smoc.model.Contest;
import bg.smoc.model.UserAccount;
import bg.smoc.web.utils.SessionUtil;

/**
 * @author zbogi
 * 
 */
public class ChooseContestServlet extends HttpServlet {

    /**
     * 
     */
    private static final long serialVersionUID = 1664279090517565207L;

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        SessionUtil sessionUtil = SessionUtil.getInstance();
        String userLogin = sessionUtil.getLoginManager().getActiveUserLogin(request);
        UserAccount userAccount = sessionUtil.getUserAccountManager().getUserAccount(userLogin);
        Vector<Contest> allContests = sessionUtil.getContestManager().getContests();

        Vector<Contest> availableForLogin = new Vector<Contest>();
        Vector<Contest> availableForRegistering = new Vector<Contest>();
        for (Contest contest : allContests) {
            if (userAccount.getContestIds().contains(contest.getId())) {
                availableForLogin.add(contest);
            } else {
                if (contest.isOpenContest())
                    availableForRegistering.add(contest);
            }
        }

        if (availableForLogin.size() == 1 && availableForRegistering.size() == 0) {
            request.getSession().setAttribute("contestId", availableForLogin.get(0).getId());
            response.sendRedirect("main");
            return;
        }

        request.setAttribute("availableForLogin", availableForLogin);
        request.setAttribute("availableForRegistering", availableForRegistering);
        request.getRequestDispatcher("chooseContest.jsp").forward(request, response);
    }

}
