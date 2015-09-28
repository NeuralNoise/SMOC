package bg.smoc.web.servlet;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import kr.or.ioi2002.RMIClientBean.HttpPostFileParser;
import kr.or.ioi2002.RMIServer.User;

import org.apache.commons.io.FilenameUtils;

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
        if (contest == null) {
            response.sendRedirect("");
            return;
        }

        User user = contestManager.getUser(contest.getId(), userLogin);
        
        if (!contest.isTestingOn()) {
            user.getTestState().setOutputNow("Test failed: Testing now allowed");
            response.sendRedirect("main");
            return;
        }

        try {
            HttpPostFileParser postFileParser = new HttpPostFileParser();
            // convert from KBytes to bytes
            int maxUploadSize = contest.getMaxUploadSize() * 1024;
            postFileParser.init(request, maxUploadSize);

            if (postFileParser.nFile <= 1) {
                user.getTestState().setOutputNow("Test failed: Select two files");
                response.sendRedirect("main");
                return;
            }
            String srcFile = null;
            byte[] abSrcFile = null;

            srcFile = postFileParser.upFile[0].pc_file_name;
            if (srcFile != null)
                srcFile = FilenameUtils.getName(srcFile);

            String task = contestManager.getTaskFromFilename(contest, srcFile, postFileParser
                    .getParameter("task"));
            String language = contestManager.getLanguageFromFilename(contest,
                    srcFile,
                    postFileParser.getParameter("language"));

            if (task == null) {
                user
                        .getTestState()
                        .setOutputNow("Invalid task name!\nThe task you are trying to submit is not part of the contest you are registered in.");
                response.sendRedirect("main");
                return;
            }
            if (contest.getTaskByName(task).getType() == Task.PROBLEM_TYPE_OUTPUT) {
                user.getTestState().setOutputNow("Output only tasks can not be tested.");
                response.sendRedirect("main");
                return;
            }
            if (language == null) {
                user.getTestState().setOutputNow("Invalid language!");
                response.sendRedirect("main");
                return;
            }

            File fileSrcFile = postFileParser.upFile[0].GetTmpFile();
            abSrcFile = new byte[postFileParser.upFile[0].size];
            BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(
                    fileSrcFile));
            int index = 0;
            int iRead = -1;
            while ((iRead = bufferedInputStream.read(abSrcFile,
                    index,
                    postFileParser.upFile[0].size - index)) > 0) {
                index += iRead;
            }
            bufferedInputStream.close();

            // for debug only
            postFileParser.upFile[0].save("./ioidebugtest", userLogin
                    + "_"
                    + String.valueOf(postFileParser.upFile[0].hashCode()));

            String stdinFile = null;
            byte[] stdinFileData = null;
            stdinFile = postFileParser.upFile[1].pc_file_name;
            File fileStdinFile = postFileParser.upFile[1].GetTmpFile();
            FileInputStream testStream = new FileInputStream(fileStdinFile);

            if (stdinFile.endsWith(".zip")) {
                ZipInputStream zipStream = new ZipInputStream(testStream);
                ZipEntry zipEntry = null;
                try {
                    if ((zipEntry = zipStream.getNextEntry()) == null || zipEntry.isDirectory())
                        throw new java.io.IOException();

                    if (zipEntry.getSize() > 5 * contest.getMaxUploadSize() * 1024) {
                        user.getTestState().setOutputNow("Test failed: stdin file too big");
                        response.sendRedirect("main");
                        return;
                    }

                    stdinFileData = new byte[(int) zipEntry.getSize()];
                    bufferedInputStream = new BufferedInputStream(zipStream);

                    index = 0;
                    iRead = -1;
                    // TODO: isn't there an easier/cleaner way to do this?
                    while ((iRead = bufferedInputStream.read(stdinFileData, index, (int) zipEntry
                            .getSize()
                            - index)) > 0) {
                        index += iRead;
                    }

                    if (zipStream.getNextEntry() != null)
                        throw new java.io.IOException();

                    bufferedInputStream.close();
                } catch (java.io.IOException e) {
                    user
                            .getTestState()
                            .setOutputNow("Test failed: Bad zip archive - please include only single file");
                    response.sendRedirect("main");
                    return;
                }
            } else {
                int uncompressedSize = postFileParser.upFile[1].size;
                if (stdinFile.endsWith(".gz") || stdinFile.endsWith(".gzip")) {
                    File uncompressedStdinFile = new File(postFileParser.upFile[1].GetTmpFile()
                            .getAbsolutePath()
                            + "_ungzipped");
                    uncompressedSize = 0;
                    try {
                        InputStream inputStream = new GZIPInputStream(testStream);
                        OutputStream outputStream = new FileOutputStream(uncompressedStdinFile);
                        byte[] readBuffer = new byte[1024];
                        int read = -1;
                        while ((read = inputStream.read(readBuffer)) > 0) {
                            outputStream.write(readBuffer, 0, read);
                            uncompressedSize += read;
                        }
                        outputStream.close();
                        inputStream.close();
                        testStream = new FileInputStream(uncompressedStdinFile);
                    } catch (IOException e) {
                        user
                                .getTestState()
                                .setOutputNow("Test failed: Bad gzip archive - please include only single file");
                        response.sendRedirect("main");
                        return;
                    }
                }
            

                stdinFileData = new byte[uncompressedSize];
                bufferedInputStream = new BufferedInputStream(testStream);

                index = 0;
                iRead = -1;
                while ((iRead = bufferedInputStream.read(stdinFileData,
                        index,
                        uncompressedSize - index)) > 0) {
                    index += iRead;
                }
                bufferedInputStream.close();
            }

            boolean bResult = contestManager.test(contest,
                    userLogin,
                    task,
                    language,
                    srcFile,
                    abSrcFile,
                    stdinFile,
                    stdinFileData);

            if (!bResult) {
                user.getTestState()
                        .setOutputNow("Test failed: Not allowed / Already processing");
                response.sendRedirect("main");
                return;
            } else {
                response.sendRedirect("main");
            }
        } catch (java.io.IOException ex) {
            user.getTestState()
                    .setOutputNow("Test failed: File upload failed (possibly file(s) too big)");
            response.sendRedirect("main");
            return;
        }
    }
}
