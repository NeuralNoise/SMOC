package bg.smoc.web.servlet;

import java.io.IOException;
import java.math.BigDecimal;
import java.rmi.RemoteException;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import bg.smoc.model.Contest;
import bg.smoc.model.Person;
import bg.smoc.model.Task;
import bg.smoc.model.UserAccount;
import bg.smoc.model.UserContestData;
import bg.smoc.model.web.ResultsRow;
import bg.smoc.model.web.ResultsStyle;
import bg.smoc.model.web.ResultsTable;
import bg.smoc.web.utils.SessionUtil;

public class ResultsServlet extends HttpServlet {

    private static final long serialVersionUID = 2017784380828029991L;

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        SessionUtil sessionUtil = SessionUtil.getInstance();
        Vector<Contest> selectedContests = getSelectedContests(request, sessionUtil
                .getContestManager().getContests());
        Vector<UserAccount> userAccounts = sessionUtil.getUserAccountManager().getAllUsers();

        String contestsQueryString = "";
        Hashtable<String, Hashtable<String, UserContestData>> reportData = new Hashtable<String, Hashtable<String, UserContestData>>();
        for (Contest contest : selectedContests) {
            if ("".equals(contestsQueryString)) {
                contestsQueryString = contestsQueryString + contest.getId() + "=on";
            } else {
                contestsQueryString = contestsQueryString + "&" + contest.getId() + "=on";
            }
            reportData.put(contest.getId(), reportForContest(contest, userAccounts, sessionUtil));
        }

        ResultsTable table = loadTable(selectedContests, reportData, sessionUtil.getPersonManager()
                .getAllPersons());

        if (Boolean.parseBoolean(request.getParameter("xls"))) {
            response.setContentType("application/ms-excel");
            response.setHeader("Content-Disposition", "attachment; filename=\"results.xls\"");

            ServletOutputStream out = response.getOutputStream();

            HSSFWorkbook resultsxls = getTableAsXLS(table);

            resultsxls.write(out);
        } else {
            table.exportToRequest(request);
            forwardAutoReload(request);

            request.setAttribute("queryString", contestsQueryString);

            request.getRequestDispatcher("results_all.jsp").forward(request, response);
        }
    }

    private Hashtable<String, UserContestData> reportForContest(Contest contest,
            Vector<UserAccount> userAccounts, SessionUtil sessionUtil) throws RemoteException {
        if (contest == null || contest.getId() == null)
            return null;
        Hashtable<String, UserContestData> result = new Hashtable<String, UserContestData>();

        for (UserAccount user : userAccounts) {
            if (!user.getContestIds().contains(contest.getId()))
                continue;
            result.put(user.getLogin(), reportForUser(contest, user.getLogin(), sessionUtil));
        }

        return result;
    }

    private UserContestData reportForUser(Contest contest, String login, SessionUtil sessionUtil)
            throws RemoteException {
        UserContestData data = new UserContestData();
        data.setData(new ResultsRow());
        data.setTotalPoints(createResultsRowForUser(contest, login, sessionUtil, data.getData()));

        return data;
    }

    private Vector<Contest> getSelectedContests(HttpServletRequest request, Vector<Contest> contests) {
        Vector<Contest> selectedContests = new Vector<Contest>();
        for (Contest contest : contests) {
            if (isCheckboxSelected(request.getParameter(contest.getId()))) {
                selectedContests.add(contest);
            }
        }
        return selectedContests;
    }

    private ResultsTable loadTable(Vector<Contest> selectedContests,
            Hashtable<String, Hashtable<String, UserContestData>> reportData, Vector<Person> persons)
            throws RemoteException {
        ResultsTable resultsTable = new ResultsTable();
        loadHeaderList(resultsTable.addHeader(), selectedContests);
        loadTableData(resultsTable, selectedContests, reportData, persons);
        return resultsTable;
    }

    private void loadTableData(ResultsTable table, Vector<Contest> selectedContests,
            Hashtable<String, Hashtable<String, UserContestData>> reportData, Vector<Person> persons)
            throws RemoteException {

        for (Person person : persons) {
            BigDecimal grandTotalPoints = BigDecimal.ZERO;
            boolean include = false;
            ResultsRow row = new ResultsRow();
            row.addCell(person.getNames());
            row.addCell(person.getTown());
            row.addCell(person.getSchool());
            row.addCell(Integer.toString(person.getSchoolYear()));
            for (Contest nextContest : selectedContests) {
                boolean foundLoginForContest = false;
                if (person.getLogins() != null) {
                    for (String login : person.getLogins()) {
                        Hashtable<String, UserContestData> contestData = reportData.get(nextContest
                                .getId());
                        if (contestData.containsKey(login)) {
                            row.append(contestData.get(login).getData());
                            grandTotalPoints = grandTotalPoints.add(contestData.get(login)
                                    .getTotalPoints());
                            contestData.remove(login);
                            foundLoginForContest = true;
                            break;
                        }
                    }
                }
                if (foundLoginForContest) {
                    include = true;
                } else {
                    row.addCell("-");
                    for (Task task : nextContest.getTasks()) {
                        for (int i = 0; i < task.getNumberOfTests(); ++i) {
                            row.addCell("-");
                        }
                        row.addCell("-");
                    }
                    row.addCell("0");
                }
            }
            row.addCell(grandTotalPoints.toString());
            if (include)
                table.appendRow(row);
        }
        for (Contest contest : selectedContests) {
            Hashtable<String, UserContestData> contestData = reportData.get(contest.getId());
            for (String login : contestData.keySet()) {
                BigDecimal total = BigDecimal.ZERO;
                ResultsRow row = table.addRow();
                row.addCell("-");
                row.addCell("-");
                row.addCell("-");
                row.addCell("-");
                for (Contest rowContest : selectedContests) {
                    if (rowContest == contest) {
                        row.append(contestData.get(login).getData());
                        total = contestData.get(login).getTotalPoints();
                    } else {
                        row.addCell("");
                        for (Task task : rowContest.getTasks()) {
                            for (int i = 0; i < task.getNumberOfTests(); ++i)
                                row.addCell("");
                            row.addCell("");
                        }
                        row.addCell("-");
                    }
                }
                row.addCell(total.toString());
            }
        }

        Vector<ResultsRow> newRows = new Vector<ResultsRow>();
        Vector<ResultsRow> tableRows = table.getRows();
        boolean[] used = new boolean[tableRows.size()];
        for (int i = 0; i < tableRows.size(); ++i) {
            used[i] = false;
        }
        for (int i = 0; i < tableRows.size(); ++i) {
            int index = -1;
            BigDecimal max = BigDecimal.ZERO;
            for (int j = 0; j < tableRows.size(); ++j) {
                BigDecimal points = new BigDecimal(tableRows.get(j).getCells().lastElement()
                        .getValue());
                if (!used[j] && max.compareTo(points) <= 0) {
                    max = points;
                    index = j;
                }
            }
            used[index] = true;
            newRows.add(tableRows.get(index));
        }

        tableRows = newRows;
        newRows = new Vector<ResultsRow>();

        for (int i = 0; i < tableRows.size(); ++i) {
            BigDecimal points = new BigDecimal(tableRows.get(i).getCells().lastElement().getValue());

            int count = 1;
            for (; i + count < tableRows.size(); ++count) {
                if (points.compareTo(new BigDecimal(tableRows.get(i + count).getCells()
                        .lastElement().getValue())) != 0)
                    break;
            }

            for (int j = 0; j < count; ++j) {
                ResultsRow row = new ResultsRow();
                if (count > 1) {
                    row.addCell(Integer.toString(i + 1) + "-" + Integer.toString(i + count));
                } else {
                    row.addCell(Integer.toString(i + 1));
                }
                row.append(tableRows.get(i + j));
                newRows.add(row);
            }

            i += count - 1;
        }
        table.setRows(newRows);
    }

    private BigDecimal createResultsRowForUser(Contest contest, String login,
            SessionUtil sessionUtil, ResultsRow resultsRow) throws RemoteException {
        resultsRow.addCell(login);

        BigDecimal totalPoints = BigDecimal.ZERO;
        Vector<Task> tasks = contest.getTasks();
        for (Task task : tasks) {
            boolean[] hasSubmit = sessionUtil.getGraderManager()
                    .hasSubmission(contest, login, task);
            List<String> byTestResults = sessionUtil.getGraderManager().getResult(contest.getId(),
                    login,
                    task);
            for (int i = 0; i < task.getNumberOfTests(); ++i) {
                if (hasSubmit[i]) {
                    if (i < byTestResults.size()) {
                        resultsRow.addCell(byTestResults.get(i));
                    }
                } else {
                    resultsRow.addCell("-");
                }
            }
            BigDecimal points = getPointsTotal(byTestResults);
            totalPoints = totalPoints.add(points);
            resultsRow.addCell(points.toString());
        }

        resultsRow.addCell(totalPoints.toString());
        return totalPoints;
    }

    private BigDecimal getPointsTotal(List<String> byTestResults) {
        BigDecimal total = BigDecimal.ZERO;
        for (String testResult : byTestResults) {
            try {
                total = total.add(new BigDecimal(testResult));
            } catch (NumberFormatException e) {
            }
        }
        return total;
    }

    private void forwardAutoReload(HttpServletRequest request) {
        String autoReload = request.getParameter("autoreload");
        request.setAttribute("autoreload", Boolean.parseBoolean(autoReload));
    }

    private void loadHeaderList(ResultsRow headerRow, Vector<Contest> selectedContests)
            throws RemoteException {
        ResultsStyle styleHeader = new ResultsStyle();
        styleHeader.setBold(true);

        headerRow.addCell("Rank", styleHeader);
        headerRow.addCell("Names", styleHeader);
        headerRow.addCell("Town", styleHeader);
        headerRow.addCell("School", styleHeader);
        headerRow.addCell("Year", styleHeader);
        for (Contest contest : selectedContests) {
            headerRow.addCell("Login", styleHeader);
            for (Task task : contest.getTasks()) {
                for (int i = 0; i < task.getNumberOfTests(); ++i) {
                    headerRow.addCell(Integer.toString(i + 1), styleHeader);
                }
                headerRow.addCell(task.getName(), styleHeader);
            }
            headerRow.addCell(contest.getShortName(), styleHeader);
        }
        headerRow.addCell("Grand total", styleHeader);
    }

    private boolean isCheckboxSelected(String parameter) {
        if (parameter == null)
            return false;
        String value = parameter.toLowerCase();
        return ("on".equals(value) || "yes".equals(value) || "true".equals(value));
    }

    private HSSFWorkbook getTableAsXLS(ResultsTable table) {

        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet = wb.createSheet("Results");

        final int MAGICWIDTH = 30;
        final int MAGICPADDING = 15;

        int[] columnWidth = new int[table.getHeader().getCells().size()];

        HSSFRow row = sheet.createRow((short) 0);
        for (int i = 0; i < table.getHeader().getCells().size(); i++) {
            HSSFCell cell = row.createCell((short) i);

            ResultsStyle style = table.getHeader().getCells().get(i).getStyle();

            cell.setCellStyle(style.getStyleHSSF(wb));

            String title = table.getHeader().getCells().get(i).getValue();

            cell.setCellValue(new HSSFRichTextString(title));

            sheet.setColumnWidth((short) i,
                    (short) ((title.length() * MAGICWIDTH + MAGICPADDING) * style.getFontSize()));
        }

        for (int r = 0; r < table.getRows().size(); r++) {

            row = sheet.createRow((short) r + 1);

            for (int i = 0; i < table.getRows().get(r).getCells().size(); i++) {
                HSSFCell cell = row.createCell((short) i);

                ResultsStyle style = table.getRows().get(r).getCells().get(i).getStyle();

                cell.setCellStyle(style.getStyleHSSF(wb));

                String value = table.getRows().get(r).getCells().get(i).getValue();
                if (value != null) {
                    try {
                        cell.setCellValue(Double.parseDouble(value));
                    } catch (NumberFormatException e) {
                        cell.setCellValue(new HSSFRichTextString(value));
                    }

                    sheet.setColumnWidth((short) i, (short) Math.max(sheet
                            .getColumnWidth((short) i),
                            (short) ((value.length() * MAGICWIDTH + MAGICPADDING) * style
                                    .getFontSize())));
                    columnWidth[i] = Math.max(columnWidth[i], value.length());
                }
            }
        }

        return wb;
    }
}
