package bg.smoc.web.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import bg.smoc.model.UserAccount;
import bg.smoc.model.manager.ContestManager;
import bg.smoc.web.utils.ServletUtil;
import bg.smoc.web.utils.SessionUtil;

public class UpdateAccountServlet extends HttpServlet {

    private static final long serialVersionUID = 2531861980083573904L;

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String userLogin = request.getParameter("login");
        UserAccount userAccount = SessionUtil.getInstance().getUserAccountManager()
                .getUserAccount(userLogin);
        ContestManager contestManager = SessionUtil.getInstance().getContestManager();
        if (userAccount != null) {
            if (request.getParameter("password") != null && request.getParameter("password") != "") {
                userAccount.setPassword(request.getParameter("password"));
                userAccount.setPasswordHash(ServletUtil.encryptPassword(userAccount.getPassword()));
            }
            if (request.getParameter("contest") != null) {
                userAccount.getContestIds().add(request.getParameter("contest"));
                contestManager.registerUserForContest(request.getParameter("contest"), userLogin);
            }
            else if (request.getParameterValues("contests") != null) {
                userAccount.getContestIds().clear();
                for (String contest : request.getParameterValues("contests")) {
                    userAccount.getContestIds().add(contest);
                    contestManager.registerUserForContest(contest, userLogin);
                }
            } else {
                userAccount.setContestIds(null);
            }
            SessionUtil.getInstance().getUserAccountManager().update(userAccount);
        }

        response.sendRedirect("accounts");
    }
}
