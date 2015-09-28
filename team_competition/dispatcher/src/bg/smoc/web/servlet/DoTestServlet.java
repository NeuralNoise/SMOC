package bg.smoc.web.servlet;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
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

public class DoTestServlet extends HttpServlet {

    private static final long serialVersionUID = 6223813170493844342L;

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String userLogin = (String) request.getSession().getAttribute("id");

        ContestManager contestManager = SessionUtil.getInstance().getContestManager();
        Contest contest = contestManager.getContest(request);
        if (contest == null || !contest.isRunning()) {
            response.sendRedirect("main?error=23");
            return;
        }

        try {
            HttpPostFileParser postFileParser = new HttpPostFileParser();
            // convert from KBytes to bytes
            int maxUploadSize = contest.getMaxUploadSize() * 1024;
            postFileParser.init(request, maxUploadSize);

            if (postFileParser.nFile <= 1) {
                response.sendRedirect("main?error=22");
                return;
            }
            String srcFile = null;
            byte[] abSrcFile = null;

            srcFile = postFileParser.upFile[0].pc_file_name;
            if (srcFile != null)
                srcFile = FilenameUtils.getName(srcFile);
            
            String task = contestManager.getTaskFromFilename(contest, srcFile, postFileParser.getParameter("task"));
            String language = contestManager.getLanguageFromFilename(contest, srcFile, postFileParser.getParameter("language"));
            
            // TODO: export these as error messages or sth
            if (task == null) {
            	User user = contestManager.getUser(contest.getId(), userLogin);
            	user.setStrLastTaskTest("Invalid task name!\nThe task you are trying to submit is not part of the contest you are registered in.");
            	user.setOutputTest(null);
            	response.sendRedirect("main");
            	return;
            }
            if (contest.getTaskByName(task).getType() == Task.PROBLEM_TYPE_OUTPUT) {
                User user = contestManager.getUser(contest.getId(), userLogin);
                user.setStrLastTaskTest("Output only tasks can not be tested.");
                user.setOutputTest(null);
                response.sendRedirect("main");
                return;
            }
            if (language == null) {
            	User user = contestManager.getUser(contest.getId(), userLogin);
            	user.setStrLastTaskTest("Invalid language!");
            	user.setOutputTest(null);
            	response.sendRedirect("main");
            	return;
            }
            
            File fileSrcFile = postFileParser.upFile[0].GetTmpFile();
            abSrcFile = new byte[postFileParser.upFile[0].size];
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(fileSrcFile));
            int index = 0;
            int iRead = -1;
            while ((iRead = bis.read(abSrcFile, index, postFileParser.upFile[0].size - index)) > 0) {
                index += iRead;
            }
            bis.close();

            // for debug only
            postFileParser.upFile[0].save("./ioidebugtest", userLogin
                    + "_"
                    + String.valueOf(postFileParser.upFile[0].hashCode()));

            String stdinFile = null;
            byte[] abStdinFile = null;
            stdinFile = postFileParser.upFile[1].pc_file_name;
            java.io.File fileStdinFile = postFileParser.upFile[1].GetTmpFile();
            abStdinFile = new byte[postFileParser.upFile[1].size];
            bis = new BufferedInputStream(new FileInputStream(fileStdinFile));
            index = 0;
            iRead = -1;
            while ((iRead = bis.read(abStdinFile, index, postFileParser.upFile[1].size - index)) > 0) {
                index += iRead;
            }
            bis.close();
            
            boolean bResult = contestManager.test(contest,
                    userLogin,
                    task,
                    language,
                    srcFile,
                    abSrcFile, stdinFile, abStdinFile);

            if (!bResult) {
                response.sendRedirect("main?error=21");
            } else {
                response.sendRedirect("main");
            }
        } catch (java.io.IOException ex) {
            response.sendRedirect("main?error=24");
        }
    }
}
