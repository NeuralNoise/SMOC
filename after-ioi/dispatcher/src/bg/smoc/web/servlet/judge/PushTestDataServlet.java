package bg.smoc.web.servlet.judge;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import bg.smoc.model.Contest;
import bg.smoc.web.utils.SessionUtil;

public class PushTestDataServlet extends HttpServlet {

    /**
     * 
     */
    private static final long serialVersionUID = -6442086501877459778L;

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String contestId = request.getParameter("contestId");

        SessionUtil sessionUtil = SessionUtil.getInstance();

        Contest contest = sessionUtil.getContestManager().getContest(contestId);
        if (contest != null) {
            sessionUtil.getGraderManager().pushTestData(contest);
        }
    }

}
