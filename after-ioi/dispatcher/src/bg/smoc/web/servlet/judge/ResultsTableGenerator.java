package bg.smoc.web.servlet.judge;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import bg.smoc.model.AccumulatedGrade;
import bg.smoc.model.Contest;
import bg.smoc.model.Person;
import bg.smoc.model.Task;
import bg.smoc.model.UserAccount;
import bg.smoc.model.UserContestData;
import bg.smoc.model.manager.GraderManager;
import bg.smoc.model.web.ResultsCell;
import bg.smoc.model.web.ResultsRow;
import bg.smoc.model.web.ResultsStyle;
import bg.smoc.model.web.ResultsTable;

public class ResultsTableGenerator {

    /**
     * Set externally.
     */
    private boolean includeTestGroupResults;

    /**
     * Set externally.
     */
    private List<Contest> selectedContests;

    /**
     * Set externally.
     */
    private Vector<UserAccount> userAccounts;

    /**
     * Set externally.
     */
    private GraderManager graderManager;

    /**
     * Set externally.
     */
    private Vector<Person> persons;

    /**
     * Returned to caller.
     */
    private ResultsTable resultsTable;

    /**
     * Returned to caller.
     */
    private String autoReloadLink;

    /**
     * Returned to caller.
     */
    private String detailsLink;

    /**
     * Internal usage only.
     */
    private Hashtable<String, Hashtable<String, UserContestData>> reportData;

    /**
     * Internal usage only.
     */
    private List<Person> personsInReport;

    /**
     * Internal usage only.
     */
    private ArrayList<String[]> personLogins;

    public boolean isIncludeTestGroupResults() {
        return includeTestGroupResults;
    }

    public void setIncludeTestGroupResults(boolean includeTestGroupResults) {
        this.includeTestGroupResults = includeTestGroupResults;
    }

    public List<Contest> getSelectedContests() {
        return selectedContests;
    }

    public GraderManager getGraderManager() {
        return graderManager;
    }

    public void setGraderManager(GraderManager graderManager) {
        this.graderManager = graderManager;
    }

    public void setSelectedContests(List<Contest> selectedContests) {
        this.selectedContests = selectedContests;
    }

    public Vector<UserAccount> getUserAccounts() {
        return userAccounts;
    }

    public void setUserAccounts(Vector<UserAccount> userAccounts) {
        this.userAccounts = userAccounts;
    }

    public String getAutoReloadLink() {
        return autoReloadLink;
    }

    public String getDetailsLink() {
        return detailsLink;
    }

    public Vector<Person> getPersons() {
        return persons;
    }

    public void setPersons(Vector<Person> persons) {
        this.persons = persons;
    }

    public void createReportData() {
        if (includeTestGroupResults) {
            detailsLink = getBaseLink();
            autoReloadLink = getBaseLink() + "&detail=group";
        } else {
            detailsLink = getBaseLink() + "&detail=group";
            autoReloadLink = getBaseLink();
        }

        initializeReportData();
        initializePersonMap();
        resultsTable = new ResultsTable();
        loadHeaderList();
        loadTableData();
    }

    private void loadHeaderList() {
        ResultsRow headerRow = resultsTable.addHeader();
        ResultsStyle styleHeader = new ResultsStyle();
        styleHeader.setBold(true);

        headerRow.addCell("Rank", styleHeader);
        headerRow.addCell("Names", styleHeader);
        headerRow.addCell("Team", styleHeader);
        for (Contest contest : selectedContests) {
            headerRow.addCell("Login", styleHeader);
            for (Task task : contest.getTasks()) {
                if (includeTestGroupResults) {
                    for (int i = 0; i < task.getTestGroups().size(); ++i) {
                        headerRow.addCell(Integer.toString(i + 1), styleHeader);
                    }
                }
                headerRow.addCell(task.getName(), styleHeader);
            }
            headerRow.addCell(needsGrandTotal() ? contest.getShortName() : "Total", styleHeader);
        }
        if (needsGrandTotal()) {
            headerRow.addCell("Grand total", styleHeader);
        }
    }

    private boolean needsGrandTotal() {
        return selectedContests.size() > 1;
    }

    private void initializeReportData() {
        reportData = new Hashtable<String, Hashtable<String, UserContestData>>();
        for (Contest contest : selectedContests) {
            reportData.put(contest.getId(), reportForContest(contest));
        }
    }

    private Hashtable<String, UserContestData> reportForContest(Contest contest) {
        if (contest == null || contest.getId() == null)
            return null;
        Hashtable<String, UserContestData> result = new Hashtable<String, UserContestData>();

        for (UserAccount user : userAccounts) {
            if (!user.getContestIds().contains(contest.getId()))
                continue;
            result.put(user.getLogin(), reportForUser(contest, user.getLogin()));
        }

        return result;
    }

    private UserContestData reportForUser(Contest contest, String login) {
        BigDecimal totalPoints = BigDecimal.ZERO;

        ResultsRow row = new ResultsRow();
        row.addCell(login);
        for (Task task : contest.getTasks()) {
            AccumulatedGrade testResults = graderManager.getResult(contest.getId(), login, task);
            if (includeTestGroupResults) {
                for (int i = 0; i < testResults.getTestGroups().size(); ++i) {
                    ResultsCell cell = new ResultsCell("-");
                    if (testResults != null) {
                        cell.setValue(testResults.getTestGroups().get(i));
                        if (task.getType() == Task.PROBLEM_TYPE_OUTPUT) {
                            cell.setHref("?contestId="
                                    + contest.getId()
                                    + "&login="
                                    + login
                                    + "&taskId="
                                    + task.getNameForTest(i + 1));
                        }
                    }
                    row.addCell(cell);
                }
            }
            if (testResults != null) {
                totalPoints = totalPoints.add(new BigDecimal(testResults.getTotal()));
            }
            ResultsCell cell = new ResultsCell(testResults != null ? testResults.getTotal()
                    : BigDecimal.ZERO.toString());
            if (task.getType() != Task.PROBLEM_TYPE_OUTPUT) {
                cell.setHref("?contestId="
                        + contest.getId()
                        + "&login="
                        + login
                        + "&taskId="
                        + task.getName());
            }
            row.addCell(cell);
        }
        row.addCell(totalPoints.toString());

        UserContestData data = new UserContestData();
        data.setData(row);
        data.setTotalPoints(totalPoints);
        return data;
    }

    private String getBaseLink() {
        StringBuffer result = new StringBuffer("");
        for (Contest contest : selectedContests) {
            if (result.length() > 0) {
                result.append("&");
            }
            result.append(contest.getId());
            result.append("=on");
        }
        return result.toString();
    }

    public ResultsTable getResultsTable() {
        return resultsTable;
    }

    private void initializePersonMap() {
        personsInReport = new ArrayList<Person>();
        personLogins = new ArrayList<String[]>();

        Map<String, Set<String>> personsInContest = new HashMap<String, Set<String>>();
        for (Contest contest : selectedContests) {
            personsInContest.put(contest.getId(), new TreeSet<String>(reportData.get(contest
                    .getId()).keySet()));
        }

        for (Person person : persons) {
            if (person.getLogins() == null || person.getLogins().size() == 0) {
                continue;
            }

            String[] logins = new String[selectedContests.size()];
            boolean include = false;
            int index = 0;
            for (Contest contest : selectedContests) {
                for (String login : person.getLogins()) {
                    if (personsInContest.get(contest.getId()).remove(login)) {
                        logins[index] = login;
                        include = true;
                        break;
                    }
                }
                index++;
            }

            if (include) {
                personsInReport.add(person);
                personLogins.add(logins);
            }
        }

        for (Contest contest : selectedContests) {
            for (String login : personsInContest.get(contest.getId())) {
                personsInReport.add(Person.getBlankPerson(login));
                String[] logins = new String[selectedContests.size()];
                int index = 0;
                for (Contest rowContest : selectedContests) {
                    logins[index] = ((rowContest == contest) ? login : null);
                    index++;
                }
                personLogins.add(logins);
            }
        }
    }

    private ResultsRow getBlankContestData(Contest contest) {
        ResultsRow row = new ResultsRow();
        row.addCell("-");
        for (Task task : contest.getTasks()) {
            if (includeTestGroupResults) {
                for (int i = 0; i < task.getTestGroups().size(); ++i) {
                    row.addCell("-");
                }
            }
            row.addCell("-");
        }
        row.addCell("0");
        return row;
    }

    private void loadTableData() {
        for (int index = 0; index < personsInReport.size(); ++index) {
            Person person = personsInReport.get(index);
            String[] logins = personLogins.get(index);

            BigDecimal grandTotalPoints = BigDecimal.ZERO;
            ResultsRow row = new ResultsRow();
            row.addCell(person.getNames());
            row.addCell(person.getTown());
            int secondary = 0;
            for (Contest contest : selectedContests) {
                String login = logins[secondary++];
                Hashtable<String, UserContestData> contestData = null;
                UserContestData userContestData = null;
                if (login != null) {
                    contestData = reportData.get(contest.getId());
                    if (contestData != null) {
                        userContestData = contestData.get(login);
                    }
                }
                if (userContestData == null) {
                    row.append(getBlankContestData(contest));
                } else {
                    row.append(userContestData.getData());
                    grandTotalPoints = grandTotalPoints.add(userContestData.getTotalPoints());
                }
            }
            if (needsGrandTotal()) {
                row.addCell(grandTotalPoints.toString());
            }
            resultsTable.appendRow(row);
        }
        sortTableRows();
    }

    private void sortTableRows() {
        Vector<ResultsRow> newRows = new Vector<ResultsRow>();
        Vector<ResultsRow> tableRows = resultsTable.getRows();
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
        resultsTable.setRows(newRows);
    }

}
