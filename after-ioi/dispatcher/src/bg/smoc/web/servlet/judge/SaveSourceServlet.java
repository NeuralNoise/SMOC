package bg.smoc.web.servlet.judge;

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

public class SaveSourceServlet extends HttpServlet {

    /**
     * 
     */
    private static final long serialVersionUID = 6026508040585507334L;

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String userLogin = request.getParameter("login");
        ContestManager contestManager = SessionUtil.getInstance().getContestManager();
        Contest contest = contestManager.getContest(request.getParameter("contestId"));
        if (contest == null) {
            response.sendRedirect("");
            return;
        }
        String taskname = request.getParameter("taskId");

        byte[] fileAsBytes = getInputStream(contestManager, contest, userLogin, taskname);
        InputStream inputStream = fileAsBytes != null ? new ByteArrayInputStream(fileAsBytes)
                : null;
        if (inputStream == null) {
            if (contest == null) {
                response.sendRedirect("");
                return;
            }
            response.sendRedirect("");
            return;
        }
        int length = fileAsBytes.length;

        BufferedInputStream bufferInputStream = new BufferedInputStream(inputStream);

        response.setContentType("plain/text");
        response.setContentLength(length);

        response.setHeader("Content-disposition",
                "attachment; filename=\"" + taskname + "_" + userLogin + ".txt" + "\"");

        ServletOutputStream servletOutputStream = response.getOutputStream();
        FileUtils.copyStreams(bufferInputStream, servletOutputStream);
        bufferInputStream.close();
    }

    public byte[] getInputStream(ContestManager contestManager, Contest contest, String userLogin,
            String taskname) throws IOException {
        if (taskname == null || contest == null)
            return null;

        byte[] bytearray = contestManager.getSourceCodeAsByteArray(contest, userLogin, taskname);
        if (bytearray == null)
            return null;

        return bytearray;
    }
}
