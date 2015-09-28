package bg.smoc.web.servlet;

import java.io.IOException;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import bg.smoc.model.Contest;
import bg.smoc.model.Task;
import bg.smoc.model.UserAccount;
import bg.smoc.web.utils.SessionUtil;

public class GradeContestServlet extends HttpServlet {

    private static final long serialVersionUID = -1151575636284932381L;

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String contestId = request.getParameter("contestId");
        SessionUtil sessionUtil = SessionUtil.getInstance();
        Contest contest = sessionUtil.getContestManager().getContest(contestId);
        if (contest != null) {
            Vector<UserAccount> users = sessionUtil.getUserAccountManager().getAllUsers();
            for (UserAccount userAccount : users) {
                if (!userAccount.getContestIds().contains(contest.getId()))
                    continue;

                for (Task task : contest.getTasks()) {
                    if (task.getType() == null || task.getType() != Task.PROBLEM_TYPE_OUTPUT) {
                        sessionUtil.getGraderManager().grade(contest,
                                userAccount.getLogin(),
                                task.getName());
                    } else {
                        for (int i = 0; i < task.getNumberOfTests(); ++i) {
                            sessionUtil.getGraderManager().grade(contest,
                                    userAccount.getLogin(),
                                    task.getNameAppenedTest(i).toLowerCase());
                        }
                    }
                }
            }
        }
        response.sendRedirect("main");
    }
}
