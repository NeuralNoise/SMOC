package bg.smoc.web.servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import kr.or.ioi2002.RMIServer.User;
import kr.or.ioi2002.RMIServer.Util;
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
        if (contest == null) {
            response.sendRedirect("");
            return;
        }

        request.setAttribute("userLogin", userLogin);
        request.setAttribute("contest", contest);
        request.setAttribute("time", Util.DATETIME_FORMAT.format(new java.util.Date()));

        User user = contestManager.getUser(contest.getId(), userLogin);
        Hashtable<String, String> hash = user.getStatus();
        generateTable(request, hash, contest.getTasks());

        request.setAttribute("announcement", generateAnnouncement(contest.getAnnouncement()));
        request.setAttribute("languages", Contest.Language.values());

        setOutputTabs(request, contest, user);

        request.setAttribute("isContestRunning", contest.isRunning() ? true : null);

        request.setAttribute("testState", user.getTestState());

        request.getRequestDispatcher("main.jsp").forward(request, response);
    }

    private void setOutputTabs(HttpServletRequest request, Contest contest, User user) {
        String activeTab = (String) request.getSession().getAttribute("tab");
        if (activeTab == null)
            activeTab = "General";
        List<Tab> tabs = new ArrayList<Tab>();
        tabs.add(new Tab("General", user.getGeneralState().getContent(), activeTab
                .equals("General")));
        for (Task task : contest.getTasks()) {
            tabs.add(new Tab(task.getName(), user.getSubmitState(task.getName()).getContent(),
                    activeTab.equals(task.getName())));
        }

        request.setAttribute("outputTabs", tabs);
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

    public class Tab {
        private String task;
        private String content;
        private boolean active;

        public Tab(String task, String content, boolean active) {
            this.setTask(task);
            this.setContent(content);
            this.setActive(active);
        }

        public void setTask(String task) {
            this.task = task;
        }

        public String getTask() {
            return task;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public String getContent() {
            return content;
        }

        public void setActive(boolean active) {
            this.active = active;
        }

        public boolean isActive() {
            return active;
        }

        public String getTabClass() {
            return isActive() ? "tabbertab tabbertabdefault" : "tabbertab";
        }
    }

}
