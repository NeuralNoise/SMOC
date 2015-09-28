package bg.smoc.web.servlet;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import bg.smoc.model.Contest;
import bg.smoc.model.UserAccount;
import bg.smoc.web.utils.SessionUtil;

public class AccountsServlet extends HttpServlet {

    private static final long serialVersionUID = -683637400526822595L;

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        SessionUtil sessionUtil = SessionUtil.getInstance();
        Vector<Contest> allContests = sessionUtil.getContestManager().getContests();
        request.setAttribute("contests", allContests);

        Hashtable<String, Contest> contestsHash = loadContestsIntoHashtable(allContests);
        Vector<UserAccount> users = sessionUtil.getUserAccountManager().getAllUsers();

        request.setAttribute("table", GenerateTable(users, contestsHash));

        request.getRequestDispatcher("accounts.jsp").forward(request, response);
    }

    private Vector<Vector<String>> GenerateTable(Vector<UserAccount> users,
            Hashtable<String, Contest> contestsHash) {
        Vector<Vector<String>> table = new Vector<Vector<String>>();
        for (UserAccount user : users) {
            Vector<String> row = new Vector<String>();
            table.add(row);
            row.add(user.getLogin());
            row.add(user.getPassword());
            StringBuffer contests = new StringBuffer("");
            for (String contestId : user.getContestIds()) {
                Contest contest = contestsHash.get(contestId);
                if (contest != null)
                    contests.append(contest.getName());
                else
                    contests.append("???");
                contests.append(" ");
            }
            row.add(contests.toString());
        }
        return table;
    }

    private Hashtable<String, Contest> loadContestsIntoHashtable(Vector<Contest> allContests) {
        Hashtable<String, Contest> contestsHash = new Hashtable<String, Contest>();
        for (Contest contest : allContests) {
            contestsHash.put(contest.getId(), contest);
        }
        return contestsHash;
    }
}
