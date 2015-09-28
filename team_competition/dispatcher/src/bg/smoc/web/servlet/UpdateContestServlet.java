package bg.smoc.web.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import bg.smoc.model.Contest;
import bg.smoc.model.manager.ContestManager;
import bg.smoc.web.utils.ServletUtil;
import bg.smoc.web.utils.SessionUtil;

public class UpdateContestServlet extends HttpServlet {

    private static final long serialVersionUID = -1858685600763585161L;

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String contestId = request.getParameter("id");
        if (contestId != null) {
            ContestManager contestManager = SessionUtil.getInstance().getContestManager();
            Contest contest = contestManager.getContest(contestId);
            contest.setName(request.getParameter("name"));
            contest.setOpenContest(ServletUtil
                            .isCheckboxSelected(request.getParameter("isPublic")));
            contest.setShortName(request.getParameter("shortName"));
            contest.setExpectedEndTime(request.getParameter("expectedEndTime"));
            contest.setAnnouncement(request.getParameter("announcement").trim());
            contest.setMaxUploadSize(Integer.parseInt(request.getParameter("maxUploadSize")));
            contestManager.updateContest(contest);
        }

        response.sendRedirect("contestSetup");
    }
}
