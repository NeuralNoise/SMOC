package bg.smoc.web.servlet;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import kr.or.ioi2002.RMIClientBean.HttpPostFileParser;
import bg.smoc.model.Contest;
import bg.smoc.model.manager.ContestManager;
import bg.smoc.model.manager.PrintManager;
import bg.smoc.web.utils.SessionUtil;

public class PrintServlet extends HttpServlet {

    private static final long serialVersionUID = -8780948852730239395L;

    @Override
    public void service(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String userId = (String) request.getSession().getAttribute("id");
        if (userId == null) {
            response.sendRedirect("index.jsp");
            return;
        }

        PrintManager printManager = SessionUtil.getInstance().getPrintManager();
        ContestManager contestManager = SessionUtil.getInstance().getContestManager();
        
        Contest currContest = contestManager.getContest(request);
        // do not allow printing in open contests
        if (currContest.isOpenContest()) {
            response.sendRedirect("main?error=52");
            return;
        }

        try {
            // request parsing
            HttpPostFileParser postFileParser = new HttpPostFileParser();
            // convert from KBytes to bytes
            int maxFileSize = printManager.getMaxFileSize() * 1024;
            postFileParser.init(request, maxFileSize);

            if (postFileParser.nFile == 0) {
                response.sendRedirect("main?error=12");
                return;
            }

            File fileSrcFile = postFileParser.upFile[0].GetTmpFile();

            boolean bResult = printManager.print(fileSrcFile, userId);
            if (!bResult) {
                response.sendRedirect("main?error=52");
            } else {
                response.sendRedirect("main?error=50");
            }
        } catch (java.io.IOException ex) {
            response.sendRedirect("main?error=14");
        }
    }
}
