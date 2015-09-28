package bg.smoc.web.servlet.contestant;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import bg.smoc.model.Contest;
import bg.smoc.model.manager.ContestManager;
import bg.smoc.web.utils.FileUtils;
import bg.smoc.web.utils.SessionUtil;

public class SubmitSaveAs extends HttpServlet {
    /**
     * 
     */
    private static final long serialVersionUID = 7256621425178207471L;

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse res)
            throws ServletException, IOException {
        SessionUtil sessionUtil = SessionUtil.getInstance();
        String userLogin = sessionUtil.getLoginManager().getActiveUserLogin(request);

        byte[] fileAsBytes = getInputStream(request, userLogin);
        InputStream is = new ByteArrayInputStream(fileAsBytes); 
        if (is == null) {
            res.sendRedirect("/main?error=31");
            return;
        }
        int length = fileAsBytes.length;
        
        BufferedInputStream bis = new BufferedInputStream(is);

        res.setContentType("plain/text");
        res.setContentLength(length);

        ServletOutputStream sos = res.getOutputStream();
        FileUtils.copyStreams(bis, sos);
        bis.close();
    }

    public byte[] getInputStream(HttpServletRequest request, String userLogin) throws IOException {
        ContestManager contestManager = SessionUtil.getInstance().getContestManager();
        Contest contest = contestManager.getContest(request);

        String taskname = request.getParameter("taskname");
        if (taskname == null || contest == null)
            return null;

        byte[] bytearray = contestManager.getSourceCodeAsByteArray(contest, userLogin, taskname);
        if (bytearray == null)
            return null;

        return bytearray;
    }

}