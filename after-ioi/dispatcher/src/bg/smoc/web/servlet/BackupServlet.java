package bg.smoc.web.servlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import kr.or.ioi2002.RMIClientBean.HttpPostFileParser;
import kr.or.ioi2002.RMIServer.User;
import kr.or.ioi2002.RMIServer.Util;
import bg.smoc.model.Contest;
import bg.smoc.model.manager.ContestManager;
import bg.smoc.web.utils.SessionUtil;

public class BackupServlet extends HttpServlet {

    private static final String FILENAME_PROPERTY_BACKUP_RESTORE = "backup_properties";

    private static final long serialVersionUID = -6805679739585478055L;

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
            user.getGeneralState().setOutputNow("Backup failed: Contest not running");
            request.getSession().setAttribute("tab", "General");
            response.sendRedirect("main");
            return;
        }

        File backupDirectory = new File("./backups/" + contest.getId() + "/" + userLogin + "/");
        backupDirectory.mkdirs();

        File propertyFile = new File(backupDirectory, FILENAME_PROPERTY_BACKUP_RESTORE);

        Properties propertyBackup = loadProperties(propertyFile);

        if (request.getMethod().equalsIgnoreCase("POST")) {
            doBackup(request,
                    response,
                    contest,
                    user,
                    propertyBackup,
                    propertyFile,
                    backupDirectory);
            return;
        } else {
            if (request.getParameter("file") != null) {
                serve(request,
                        response,
                        request.getParameter("file"),
                        user,
                        propertyBackup,
                        backupDirectory);
                return;
            }
            if (request.getParameter("delete_all") != null) {
                deleteAllFiles(request, response, propertyBackup, propertyFile, backupDirectory);
                return;
            }
            if (request.getParameter("delete_file") != null) {
                deleteFile(request.getParameter("delete_file"),
                        propertyBackup,
                        propertyFile,
                        backupDirectory);
            }

            display(request, response, propertyBackup);
        }
    }

    private Properties loadProperties(File propertyFile) throws IOException {
        Properties propertyBackup = new Properties();

        if (propertyFile.exists()) {
            FileInputStream fis = new FileInputStream(propertyFile);
            propertyBackup.load(fis);
            fis.close();
        }

        return propertyBackup;
    }

    private void storeProperties(Properties propertyBackup, File propertyFile) throws IOException {
        FileOutputStream fos = new FileOutputStream(propertyFile);
        propertyBackup.store(fos, "Filename and timestamp for Backup/restore facility");
        fos.close();
    }

    private void display(HttpServletRequest request, HttpServletResponse response,
            Properties propertyBackup) throws ServletException, IOException {
        SessionUtil sessionUtil = SessionUtil.getInstance();
        String userLogin = sessionUtil.getLoginManager().getActiveUserLogin(request);
        ContestManager contestManager = sessionUtil.getContestManager();
        Contest contest = contestManager.getContest(request);
        if (contest == null) {
            response.sendRedirect("");
            return;
        }

        request.setAttribute("userLogin", userLogin);
        request.setAttribute("contest", contest);
        request.setAttribute("time", Util.DATETIME_FORMAT.format(new java.util.Date()));

        Vector<String> vectorFileid = new Vector<String>();

        for (Object key : propertyBackup.keySet().toArray()) {
            String strKey = (String) key;
            if (!strKey.startsWith("datetime")) {
                vectorFileid.add(strKey);
            }
        }

        String[] astrFileid = new String[vectorFileid.size()];
        astrFileid = (String[]) vectorFileid.toArray(astrFileid);

        class FileidComparator implements java.util.Comparator<String> {
            private java.util.Properties propertyBackup = null;
            private java.text.DateFormat dateFormat = null;

            private FileidComparator(java.util.Properties propertyBackup,
                    java.text.DateFormat date_format) {
                this.propertyBackup = propertyBackup;
                this.dateFormat = date_format;
            }

            public int compare(String arg0, String arg1) {
                java.util.Date d1, d2 = null;
                try {
                    d1 = dateFormat.parse(propertyBackup.getProperty("datetime" + (String) arg0));
                    d2 = dateFormat.parse(propertyBackup.getProperty("datetime" + (String) arg1));
                } catch (java.text.ParseException e) {
                    System.out.println("restore: FileidComparator: " + e.toString());
                    return 0;
                }
                return d1.compareTo(d2);
            }
        }

        java.util.Arrays.sort(astrFileid,
                new FileidComparator(propertyBackup, Util.DATETIME_FORMAT));

        Vector<Vector<String>> table = new Vector<Vector<String>>();

        for (String fileid : astrFileid) {
            Vector<String> row = new Vector<String>();
            row.add(fileid);
            row.add(propertyBackup.getProperty(fileid));
            row.add(propertyBackup.getProperty("datetime" + fileid));
            table.add(row);
        }

        request.setAttribute("table", table);

        request.getRequestDispatcher("backup.jsp").forward(request, response);
    }

    private void doBackup(HttpServletRequest request, HttpServletResponse response,
            Contest contest, User user, Properties propertyBackup, File propertyFile,
            File backupDirectory) throws IOException, ServletException {
        try {

            HttpPostFileParser postFileParser = new HttpPostFileParser();
            // no more than the maximum test should be allowed
            // convert from KBytes to bytes
            int maxUploadSize = contest.getMaxUploadSize() * 1024;
            postFileParser.init(request, maxUploadSize);

            if (postFileParser.nFile < 1) {
                user.getGeneralState().setOutputNow("Please select file.");
                request.getSession().setAttribute("tab", "General");
                response.sendRedirect("main");
                return;
            }

            String backupFile = postFileParser.upFile[0].pc_file_name;

            // add new backup file information
            int iFilename = 1000000;
            while (propertyBackup.containsKey(String.valueOf(iFilename)) == true)
                iFilename++;
            String strFilename = String.valueOf(iFilename);
            propertyBackup.setProperty(strFilename, backupFile);
            propertyBackup.setProperty("datetime" + strFilename, Util.DATETIME_FORMAT
                    .format(new java.util.Date()));

            storeProperties(propertyBackup, propertyFile);

            postFileParser.upFile[0].save(backupDirectory.getPath(), strFilename);
        } catch (java.io.IOException ex) {
            user.getGeneralState().setOutputNow("File upload interrupted: Please retry");
            request.getSession().setAttribute("tab", "General");
            response.sendRedirect("main");
            return;
        }

        display(request, response, propertyBackup);
    }

    private void serve(HttpServletRequest request, HttpServletResponse response, String fileId,
            User user, Properties propertyBackup, File backupDirectory) throws IOException {
        File file = new File(backupDirectory, fileId.replace("/", ""));
        if (!file.exists()) {
            user.getGeneralState().setOutputNow("File not found");
            request.getSession().setAttribute("tab", "General");
            response.sendRedirect("main");
            return;
        }

        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; filename=\""
                + propertyBackup.getProperty(fileId)
                + "\"");

        byte b[] = new byte[4096];
        int iRead = -1;
        FileInputStream fis = new FileInputStream(file);
        ServletOutputStream out = response.getOutputStream();
        while ((iRead = fis.read(b, 0, b.length)) > 0) {
            out.write(b, 0, iRead);
        }

        fis.close();
        out.flush();
        out.close();

    }

    private void deleteFile(String fileId, Properties propertyBackup, File propertyFile,
            File backupDirectory) throws IOException {
        // delete file entry
        propertyBackup.remove(fileId);
        propertyBackup.remove("datetime" + fileId);

        // save property file for backup
        storeProperties(propertyBackup, propertyFile);

        // delete actual file
        java.io.File file = new java.io.File(backupDirectory, fileId.replace("/", ""));
        file.delete();
    }

    private void deleteAllFiles(HttpServletRequest request, HttpServletResponse response,
            Properties propertyBackup, File propertyFile, File backupDirectory) throws IOException,
            ServletException {

        for (Object key : propertyBackup.keySet().toArray()) {
            String strKey = (String) key;
            if (!strKey.startsWith("datetime")) {
                new File(backupDirectory, strKey).delete();
            }
            propertyBackup.remove(key);
        }

        storeProperties(propertyBackup, propertyFile);

        response.sendRedirect("main");
    }

}
