/**
 * 
 */
package bg.smoc.web.servlet.contestant;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import bg.smoc.model.Contest;
import bg.smoc.model.manager.ContestManager;
import bg.smoc.web.utils.SessionUtil;

/**
 * @author tsvetan.bogdanov@gmail.com
 * 
 */
public class RegisterForContestServlet extends HttpServlet {
    /**
     * 
     */
    private static final long serialVersionUID = -7836659092610062433L;

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        SessionUtil sessionUtil = SessionUtil.getInstance();
        ContestManager contestManager = sessionUtil.getContestManager();
        Contest contest = contestManager.getContest(request.getParameter("contestId"));
        if (contest != null) {
            sessionUtil.getUserAccountManager().registerUserForContest(sessionUtil
                    .getLoginManager().getActiveUserLogin(request),
                    contest.getId());
        }

        response.sendRedirect("/chooseContest");
    }
}
