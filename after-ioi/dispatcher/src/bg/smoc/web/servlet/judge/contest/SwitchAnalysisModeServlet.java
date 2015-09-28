/**
 * 
 */
package bg.smoc.web.servlet.judge.contest;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import bg.smoc.model.Contest;
import bg.smoc.web.utils.SessionUtil;

/**
 * @author tsvetan.bogdanov@gmail.com
 * 
 *         This class takes care of enabling and disabling analysis mode.
 */
public class SwitchAnalysisModeServlet extends HttpServlet {

    /**
     * 
     */
    private static final long serialVersionUID = 6868986616295988559L;

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String contestId = request.getParameter("id");
        Contest contest = SessionUtil.getInstance().getContestManager().getContest(contestId);

        String action = request.getParameter("set");

        if ("on".equals(action)) {
            contest.getState().setInAnalysisMode(true);
        } else {
            contest.getState().setInAnalysisMode(false);
        }

        response.sendRedirect("contestSetup");
    }

}
