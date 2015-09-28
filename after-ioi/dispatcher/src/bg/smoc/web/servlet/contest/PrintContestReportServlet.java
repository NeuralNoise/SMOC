package bg.smoc.web.servlet.contest;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import bg.smoc.model.Contest;
import bg.smoc.model.manager.ContestManager;
import bg.smoc.web.utils.SessionUtil;

public class PrintContestReportServlet extends HttpServlet {

    /**
     * 
     */
    private static final long serialVersionUID = 1149265231551622157L;

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String contestId = request.getParameter("contestId");
        SessionUtil sessionUtil = SessionUtil.getInstance();
        ContestManager contestManager = sessionUtil.getContestManager();
        Contest contest = contestManager.getContest(contestId);
        if (contest == null) {
            response.sendRedirect("");
        }
        
        ContestReportGenerator generator = new ContestReportGenerator();
        generator.setContest(contest);
        generator.setAllUserAccounts(sessionUtil.getUserAccountManager().getAllUsers());
        generator.setAllPersons(sessionUtil.getPersonManager().getAllPersons());
        generator.setGraderManager(sessionUtil.getGraderManager());
        generator.setPrintManager(sessionUtil.getPrintManager());

        generator.generateContestReport();
        
        response.sendRedirect("contestSetup");        
    }

}
