package bg.smoc.web.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FilenameUtils;

import kr.or.ioi2002.RMIClientBean.HttpPostFileParser;
import kr.or.ioi2002.RMIServer.User;
import bg.smoc.model.Contest;
import bg.smoc.model.Task;
import bg.smoc.model.manager.ContestManager;
import bg.smoc.web.utils.SessionUtil;

public class DoSubmitServlet extends HttpServlet {

    private static final long serialVersionUID = -8582188100238050943L;

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        SessionUtil sessionUtil = SessionUtil.getInstance();
        String userLogin = sessionUtil.getLoginManager().getActiveUserLogin(request);
        ContestManager contestManager = sessionUtil.getContestManager();
        Contest contest = contestManager.getContest(request);
        if (contest == null) {
            response.sendRedirect("");
            return;
        }
        User user = contestManager.getUser(contest.getId(), userLogin);
        if (!contest.isRunning()) {
            user.getGeneralState().setOutputNow("Submission failed: Contest not running");
            request.getSession().setAttribute("tab", "General");
            response.sendRedirect("main");
            return;
        }

        try {
            // request parsing
            HttpPostFileParser postFileParser = new HttpPostFileParser();
            // convert from KBytes to bytes
            int maxUploadSize = contest.getMaxUploadSize() * 1024;
            postFileParser.init(request, maxUploadSize);

            if (postFileParser.nFile == 0) {
                user.getGeneralState().setOutputNow("Submission failed: No file selected");
                request.getSession().setAttribute("tab", "General");
                response.sendRedirect("main");
                return;
            }

            String srcFile = postFileParser.upFile[0].pc_file_name;
            if (srcFile != null)
                srcFile = FilenameUtils.getName(srcFile);

            String taskName = contestManager.getTaskFromFilename(contest, srcFile, postFileParser
                    .getParameter("task"));
            String language = contestManager.getLanguageFromFilename(contest,
                    srcFile,
                    postFileParser.getParameter("language"));

            Task task = contest.getTaskByName(taskName);

            if (task == null || taskName == null) {
                user
                        .getGeneralState()
                        .setOutputNow("Invalid task name!\nThe task you are trying to submit is not part of the contest you are registered in.");
                request.getSession().setAttribute("tab", "General");
                response.sendRedirect("main");
                return;
            }
            if (postFileParser.upFile[0].size > contest.getTaskByName(taskName).getMaxSubmitSize()) {
                user.getGeneralState().setOutputNow("Submission failed: File too big");
                request.getSession().setAttribute("tab", "General");
                response.sendRedirect("main");
                return;
            }
            if (contest.getTaskByName(taskName).getType() == Task.PROBLEM_TYPE_OUTPUT) {
                String index = null;
                int indexNumber = 0;
                if (srcFile != null && srcFile.split("\\.").length >= 2) {
                    index = srcFile.split("\\.")[1];
                    if ("txt".equals(index) || "out".equals(index)) {
                        indexNumber = 1;
                    } else {
                        try {
                            indexNumber = Integer.parseInt(index);
                            if (indexNumber <= 0
                                    || indexNumber > contest.getTaskByName(taskName)
                                            .getNumberOfTests()) {
                                index = null;
                            }
                        } catch (NumberFormatException e) {
                            index = null;
                        }
                    }
                }
                if (index == null) {
                    user
                            .getGeneralState()
                            .setOutputNow("Invalid file name!\nFormat is [taskname].[case_number].txt (e.g. output.10.txt).");
                    request.getSession().setAttribute("tab", "General");
                    response.sendRedirect("main");
                    return;
                }
                // TODO: remove HACK
                language = String.format("%0" + Task.TESTCASE_PADDING_LENGTH + "d", indexNumber);
            }
            if (language == null) {
                user.getGeneralState().setOutputNow("Invalid language!");
                request.getSession().setAttribute("tab", "General");
                response.sendRedirect("main");
                return;
            }

            java.io.File fileSrcFile = postFileParser.upFile[0].GetTmpFile();

            byte[] absoluteSrcFile = new byte[postFileParser.upFile[0].size];
            java.io.BufferedInputStream bis = new java.io.BufferedInputStream(
                    new java.io.FileInputStream(fileSrcFile));
            int index = 0;
            int iRead;
            while ((iRead = bis.read(absoluteSrcFile, index, postFileParser.upFile[0].size - index)) > 0) {
                index += iRead;
            }
            bis.close();

            // for debug only
            postFileParser.upFile[0].save("./ioidebugsubmit", userLogin
                    + "_"
                    + String.valueOf(postFileParser.upFile[0].hashCode()));

            boolean isAlwaysAccept = Boolean.valueOf(postFileParser.getParameter("always_accept"));

            boolean bResult = contestManager.submit(contest,
                    userLogin,
                    task,
                    language,
                    srcFile,
                    absoluteSrcFile,
                    isAlwaysAccept);

            if (!bResult) {
                user.getGeneralState().setOutputNow("Submission failed: Already processing");
                request.getSession().setAttribute("tab", "General");
                response.sendRedirect("main");
                return;
            } else {
                request.getSession().setAttribute("tab", task.getName());
                response.sendRedirect("main");
            }
        } catch (java.io.IOException ex) {
            user.getGeneralState().setOutputNow("Submission failed: File upload failed (possibly file too big)");
            request.getSession().setAttribute("tab", "General");
            response.sendRedirect("main");
            return;
        }
    }
}
