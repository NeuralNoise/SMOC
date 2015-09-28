package bg.smoc.web.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import kr.or.ioi2002.RMIServer.Util;
import bg.smoc.model.Contest;
import bg.smoc.model.manager.ContestManager;
import bg.smoc.web.utils.SessionUtil;

public class StartContestServlet extends HttpServlet {

    private static final long serialVersionUID = 8193997486491663924L;

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String contestId = request.getParameter("id");
        ContestManager contestManager = SessionUtil.getInstance().getContestManager();
        Contest contest = contestManager.getContest(contestId);
        contest.setRunning(true);
        contest.setTestingOn(true);
        contest.setFeedbackOn(true);
        contest.setLastStartTime(Util.DATETIME_FORMAT.format(new java.util.Date()));
        contestManager.updateContest(contest);
        contestManager.scheduleContest(contest);
        response.sendRedirect("contestSetup");
    }

}
