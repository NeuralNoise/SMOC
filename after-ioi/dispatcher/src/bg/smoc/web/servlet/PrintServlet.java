package bg.smoc.web.servlet;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import kr.or.ioi2002.RMIClientBean.HttpPostFileParser;
import kr.or.ioi2002.RMIServer.User;
import bg.smoc.model.Contest;
import bg.smoc.model.manager.ContestManager;
import bg.smoc.model.manager.PrintManager;
import bg.smoc.web.utils.SessionUtil;

public class PrintServlet extends HttpServlet {

    private static final long serialVersionUID = -8780948852730239395L;

    @Override
    public void service(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        SessionUtil sessionUtil = SessionUtil.getInstance();
        String userLogin = sessionUtil.getLoginManager().getActiveUserLogin(request);
        ContestManager contestManager = sessionUtil.getContestManager();
        PrintManager printManager = sessionUtil.getPrintManager();
        Contest contest = contestManager.getContest(request);
        if (contest == null) {
            response.sendRedirect("");
            return;
        }
        User user = contestManager.getUser(contest.getId(), userLogin);
        if (!contest.isRunning()) {
            user.getGeneralState().setOutputNow("Print failed: Contest not running");
            request.getSession().setAttribute("tab", "General");
            response.sendRedirect("main");
            return;
        }
        
        // do not allow printing in open contests
        if (contest.isOpenContest()) {
            user.getGeneralState().setOutputNow("Print failed: No printing in open contests");
            request.getSession().setAttribute("tab", "General");
            response.sendRedirect("main");
            return;
        }

        try {
            // request parsing
            HttpPostFileParser postFileParser = new HttpPostFileParser();
            // convert from KBytes to bytes
            int maxFileSize = printManager.getMaxFileSize() * 1024;
            postFileParser.init(request, maxFileSize);

            if (postFileParser.nFile == 0) {
                user.getGeneralState().setOutputNow("Print failed: No file selected");
                request.getSession().setAttribute("tab", "General");
                response.sendRedirect("main");
                return;
            }

            File fileSrcFile = postFileParser.upFile[0].GetTmpFile();

            boolean bResult = printManager.print(fileSrcFile, userLogin);
            if (!bResult) {
                user.getGeneralState().setOutputNow("Print failed");
                request.getSession().setAttribute("tab", "General");
                response.sendRedirect("main");
                return;
            } else {
                user.getGeneralState().setOutputNow("Print successful");
                request.getSession().setAttribute("tab", "General");
                response.sendRedirect("main");
                return;
            }
        } catch (java.io.IOException ex) {
            user.getGeneralState().setOutputNow("Print failed: File upload failed (possibly file(s) too big)");
            request.getSession().setAttribute("tab", "General");
            response.sendRedirect("main");
            return;
        }
    }
}
