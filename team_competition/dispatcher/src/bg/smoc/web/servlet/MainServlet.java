package bg.smoc.web.servlet;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Hashtable;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import bg.smoc.model.Contest;
import bg.smoc.model.Task;
import bg.smoc.model.manager.ContestManager;
import bg.smoc.web.utils.SessionUtil;

public class MainServlet extends HttpServlet {

    private static final long serialVersionUID = 1518094871896762901L;

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        SessionUtil sessionUtil = SessionUtil.getInstance();
        String userLogin = sessionUtil.getLoginManager().getActiveUserLogin(request);
        ContestManager contestManager = sessionUtil.getContestManager();
        Contest contest = contestManager.getContest(request);

        request.setAttribute("userLogin", userLogin);
        request.setAttribute("contest", contest);
        request.setAttribute("time", new SimpleDateFormat("yyyy.MM.dd HH:mm:ss")
                .format(new java.util.Date()));

        Hashtable<String, String> hash = contestManager.queryStatus(contest, userLogin);
        generateTable(request, hash, contest.getTasks());
        request.setAttribute("announcement", generateAnnouncement(contest.getAnnouncement()));

        String errorMessage = getErrorMessage(request.getParameter("error"), contest.isRunning());
        request.setAttribute("errorMessage", errorMessage);

        request.setAttribute("processingSubmit", isProcessingSubmit(hash));
        request.setAttribute("submitProgress", hash.get("submitProgress"));
        request.setAttribute("outputSubmit", hash.get("outputSubmit"));
        request.setAttribute("processingTest", isProcessingTest(hash));
        request.setAttribute("testProgress", hash.get("testProgress"));
        request.setAttribute("outputTest", hash.get("outputTest"));
        request.setAttribute("tasks", contest.getTasks());
        request.setAttribute("languages", Contest.Language.values());

        request.getRequestDispatcher("main.jsp").forward(request, response);
    }

    private boolean isProcessingSubmit(Hashtable<String, String> hash) {
        return !hash.get("processingSubmit").equals("false");
    }

    private boolean isProcessingTest(Hashtable<String, String> hash) {
        return !hash.get("processingTest").equals("false");
    }

    private String getErrorMessage(String errorno, boolean contestRunning) {
        if (errorno == null || errorno.equals(""))
            errorno = "0";
        String errorMessage = null;
        switch (Integer.parseInt(errorno)) {
        case 0:
            errorMessage = null;
            break;
        case 11:
            errorMessage = "Submission failed: Already processing";
            break;
        case 12:
            errorMessage = "Submission failed: No file selected";
            break;
        case 13:
            errorMessage = "Submission failed: Contest not running";
            break;
        case 14:
            errorMessage = "Submission failed: File too big";
            break;
        case 21:
            errorMessage = "Test failed: Not allowed / Already processing";
            break;
        case 22:
            errorMessage = "Test failed: Select two files";
            break;
        case 23:
            errorMessage = "Test failed: Contest not running";
            break;
        case 24:
            errorMessage = "Submission failed: File(s) too big";
        case 31:
            errorMessage = "File not found";
            break;
        case 41:
            errorMessage = "File upload interrupted: Please retry";
            break;
        case 50:
            errorMessage = "Print Successful";
            break;
        case 51:
            errorMessage = "File upload interrupted: Please retry";
            break;
        case 52:
            errorMessage = "Print Failed";
            break;
        default:
            errorMessage = "Unknown error";
        }

        if (!contestRunning && errorMessage == null)
            errorMessage = "Contest is not running";
        return errorMessage;
    }

    private String generateAnnouncement(String announcement) {
        if (announcement == null)
            return "";
        StringBuffer sbHtmlAnnouncement = new StringBuffer();
        for (int i = 0; i < announcement.length(); i++) {
            if (announcement.charAt(i) == '\n')
                sbHtmlAnnouncement.append("<br>");
            else
                sbHtmlAnnouncement.append(announcement.charAt(i));
        }
        return sbHtmlAnnouncement.toString();
    }

    private void generateTable(HttpServletRequest request, Hashtable<String, String> hash,
            Vector<Task> tasks) {

        Vector<Vector<String>> table = new Vector<Vector<String>>();
        Vector<Vector<Integer>> tableInfo = new Vector<Vector<Integer>>();

        for (Task task : tasks) {
            Vector<String> row = new Vector<String>();
            Vector<Integer> rowInfo = new Vector<Integer>();

            table.add(row);
            tableInfo.add(rowInfo);

            row.add("Task");
            rowInfo.add(1);
            row.add(task.getName());
            rowInfo.add(2);

            if (task.getType() != Task.PROBLEM_TYPE_OUTPUT) {
                row = new Vector<String>();
                rowInfo = new Vector<Integer>();
                table.add(row);
                tableInfo.add(rowInfo);

                row.add("File");
                rowInfo.add(3);

                String taskname = task.getName();
                if (hash.containsKey(taskname + "_filename")) {
                    String filename = hash.get(taskname + "_filename");
                    row.add("<a href=\"download_submit_file/"
                            + filename
                            + "?taskname="
                            + taskname
                            + "\">"
                            + filename
                            + "</a>");
                    rowInfo.add(0);

                    row = new Vector<String>();
                    rowInfo = new Vector<Integer>();
                    table.add(row);
                    tableInfo.add(rowInfo);

                    row.add("Time");
                    rowInfo.add(3);
                    String strFullDate = hash.get(taskname + "_submit_time");
                    String submitTime = strFullDate.substring(strFullDate.length() - 8);
                    row.add(submitTime);
                    rowInfo.add(0);
                } else {
                    row.add("-");
                    rowInfo.add(0);

                    row = new Vector<String>();
                    rowInfo = new Vector<Integer>();
                    table.add(row);
                    tableInfo.add(rowInfo);

                    row.add("Time");
                    rowInfo.add(3);
                    row.add("-");
                    rowInfo.add(0);
                }
            } else {
                for (int i = 0; i < task.getNumberOfTests(); ++i) {
                    row = new Vector<String>();
                    rowInfo = new Vector<Integer>();
                    table.add(row);
                    tableInfo.add(rowInfo);

                    String internalTaskName = task.getNameAppenedTest(i);
                    if (hash.containsKey(internalTaskName)) {
                        String filename = hash.get(internalTaskName + "_filename");
                        row.add("<a href=\"download_submit_file/"
                                + filename
                                + "?taskname="
                                + internalTaskName
                                + "\">"
                                + filename
                                + "</a>");
                        rowInfo.add(0);

                        String strFullDate = hash.get(internalTaskName + "_submit_time");
                        row.add(strFullDate.substring(strFullDate.length() - 8));
                        rowInfo.add(0);
                    } else {
                        row.add("-");
                        rowInfo.add(0);
                        row.add("-");
                        rowInfo.add(0);
                    }
                }
            }
        }

        request.setAttribute("table", table);
        request.setAttribute("tableInfo", tableInfo);
    }
}
