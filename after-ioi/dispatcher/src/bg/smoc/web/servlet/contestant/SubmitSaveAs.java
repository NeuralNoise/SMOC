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

import kr.or.ioi2002.RMIServer.User;

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
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        SessionUtil sessionUtil = SessionUtil.getInstance();
        String userLogin = sessionUtil.getLoginManager().getActiveUserLogin(request);

        byte[] fileAsBytes = getInputStream(request, userLogin);
        InputStream inputStream = new ByteArrayInputStream(fileAsBytes);
        if (inputStream == null) {
            ContestManager contestManager = sessionUtil.getContestManager();
            Contest contest = contestManager.getContest(request);
            if (contest == null) {
                response.sendRedirect("");
                return;
            }
            User user = contestManager.getUser(contest.getId(), userLogin);
            user.getGeneralState().setOutputNow("File not found");
            request.getSession().setAttribute("tab", "General");
            response.sendRedirect("main");
            return;
        }
        int length = fileAsBytes.length;

        BufferedInputStream bufferInputStream = new BufferedInputStream(inputStream);

        response.setContentType("plain/text");
        response.setContentLength(length);

        ServletOutputStream servletOutputStream = response.getOutputStream();
        FileUtils.copyStreams(bufferInputStream, servletOutputStream);
        bufferInputStream.close();
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