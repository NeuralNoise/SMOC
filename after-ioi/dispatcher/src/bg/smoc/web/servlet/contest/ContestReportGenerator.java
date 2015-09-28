package bg.smoc.web.servlet.contest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Vector;

import bg.smoc.model.AccumulatedGrade;
import bg.smoc.model.Contest;
import bg.smoc.model.Person;
import bg.smoc.model.Task;
import bg.smoc.model.TestGroup;
import bg.smoc.model.UserAccount;
import bg.smoc.model.manager.GraderManager;
import bg.smoc.model.manager.PrintManager;

public class ContestReportGenerator {

    private Contest contest;

    private Vector<UserAccount> allUserAccounts;

    private Vector<Person> allPersons;

    private GraderManager graderManager;

    private PrintManager printManager;

    public Contest getContest() {
        return contest;
    }

    public void setContest(Contest contest) {
        this.contest = contest;
    }

    public Vector<UserAccount> getAllUserAccounts() {
        return allUserAccounts;
    }

    public void setAllUserAccounts(Vector<UserAccount> allUserAccounts) {
        this.allUserAccounts = allUserAccounts;
    }

    public Vector<Person> getAllPersons() {
        return allPersons;
    }

    public void setAllPersons(Vector<Person> allPersons) {
        this.allPersons = allPersons;
    }

    public void generateContestReport() {
        for (UserAccount account : allUserAccounts) {
            if (account.getContestIds().contains(contest.getId())) {
                generateReportFor(account);
                boolean success = printManager.print(graderManager.getContestReportFile(contest
                        .getId(), account.getLogin()), account.getLogin(), false);
                if (!success) {
                    System.out.println("Could not print contest report for :" + account.getLogin());
                }
            }
        }
    }

    public GraderManager getGraderManager() {
        return graderManager;
    }

    public void setGraderManager(GraderManager graderManager) {
        this.graderManager = graderManager;
    }

    public PrintManager getPrintManager() {
        return printManager;
    }

    public void setPrintManager(PrintManager printManager) {
        this.printManager = printManager;
    }

    private void generateReportFor(UserAccount account) {
        StringBuffer report = new StringBuffer();
        Person person = loadPerson(account);
        printImportantPair(report, "Country", (person != null) ? person.getTown() : null);
        printName(report, person);

        printProperty(report, "Login", account.getLogin());
        report.append("\n\n");
        printDelimiter(report);

        for (Task task : contest.getTasks()) {
            AccumulatedGrade testResults = graderManager.getResult(contest.getId(), account
                    .getLogin(), task);
            report.append(String.format("Task %-10s %#3s out of 100\n",
                    task.getName() + ":",
                    testResults.getTotal()));
        }
        report.append("-------------------------------\n");
        BigDecimal totalScore = calculateTotalScore(account);
        report.append(String.format("Total score:    %#3s out of %#3s\n", totalScore, contest
                .getTasks().size() * 100));
        printDelimiter(report);

        report.append("\n\n\nLegend:\n"
                + "TL - Time Limit Exceeded\n"
                + "RE - Run-time Error (including limit violation)\n"
                + "WA - Wrong Answer\n"
                + "PC - Partial Credit\n"
                + "OK - Correct\n");

        for (Task task : contest.getTasks()) {
            AccumulatedGrade testResults = graderManager.getResult(contest.getId(), account
                    .getLogin(), task);

            report.append("\n\n\n");
            printDelimiter(report);
            printName(report, person);
            printProperty(report, "Task", task.getName());
            report.append("\n");
            if (task.getType() != Task.PROBLEM_TYPE_OUTPUT
                    && "-".equals(testResults.getTestCases().get(0))) {
                report.append("No valid submission.\n");
                printDelimiter(report);
                continue;
            }

            report.append("Test group | Score | Max Score | Individual test results\n");
            report.append("-----------+-------+-----------+-------------------------\n");

            List<TestGroup> testGroups = task.getTestGroups();
            for (int index = 0; index < testGroups.size(); index++) {
                report.append(String.format("%#6s     |", index + 1));
                TestGroup group = testGroups.get(index);
                List<String> testCases = testResults.getTestCases();
                report.append(String.format(" %#3s   |", getNumericValue(testResults
                        .getTestGroups().get(index))));
                report.append(String.format("    %#3s    |", group.getPoints()));
                for (int testIndex : group.getTestCases()) {
                    report.append(" ");
                    report.append(toTwoLetterCode(testCases.get(testIndex - 1), group.getPoints()));
                    report.append(" ");
                }
                report.append("\n");
            }
            report.append("-----------+-------+-----------+-------------------------\n");
            report.append(String.format("   TOTAL   | %#3s   |    100    |\n", testResults
                    .getTotal()));
            printDelimiter(report);
        }

        report.append("\n\n");
        printName(report, person);
        report.append("TOTAL SCORE : "
                + totalScore.toString()
                + " out of "
                + (contest.getTasks().size() * 100)
                + "\n");

        graderManager.storeContestReport(contest.getId(), account.getLogin(), report.toString());
    }

    private void printName(StringBuffer report, Person person) {
        printImportantPair(report, "Name", (person != null) ? person.getNames() : null);
    }

    private void printImportantPair(StringBuffer report, String key, String value) {
        printProperty(report, key, (value != null) ? value.toUpperCase() : "? ? ?");
    }

    private String getNumericValue(String string) {
        try {
            new BigDecimal(string);
            return string;
        } catch (NumberFormatException e) {
            return "0";
        }
    }

    private String toTwoLetterCode(String execResult, BigDecimal maxScore) {
        try {
            BigDecimal pointsGained = new BigDecimal(execResult);
            if (pointsGained.equals(maxScore)) {
                return "OK";
            } else {
                return "PC";
            }
        } catch (NumberFormatException e) {
            if ("x".equals(execResult)) {
                return "WA";
            } else if ("e".equals(execResult)) {
                return "RE";
            } else if ("t".equals(execResult)) {
                return "TL";
            } else if ("c".equals(execResult)) {
                return "CE";
            } else if ("-".equals(execResult)) {
                return "Not submitted";
            } else {
                System.out.print("Unknown output result:" + execResult);
                return "SE";
            }
        }
    }

    private BigDecimal calculateTotalScore(UserAccount account) {
        BigDecimal totalScore = BigDecimal.ZERO;
        for (Task task : contest.getTasks()) {
            AccumulatedGrade testResults = graderManager.getResult(contest.getId(), account
                    .getLogin(), task);
            try {
                BigDecimal taskScore = new BigDecimal(testResults.getTotal());
                totalScore = totalScore.add(taskScore);
            } catch (NumberFormatException e) {
            }
        }
        return totalScore;
    }

    private void printDelimiter(StringBuffer report) {
        report.append("---------------------------------------------------------\n");
    }

    private void printProperty(StringBuffer report, String key, String value) {
        report.append(key);
        report.append(" : ");
        report.append(value);
        report.append("\n");
    }

    private Person loadPerson(UserAccount account) {
        for (Person person : allPersons) {
            if (person.getLogins().contains(account.getLogin()))
                return person;
        }

        System.out.println("Could not find person for account:" + account.getLogin());
        return null;
    }
}
