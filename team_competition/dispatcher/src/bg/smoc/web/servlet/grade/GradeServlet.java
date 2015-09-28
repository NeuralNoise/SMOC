/**
 * 
 */
package bg.smoc.web.servlet.grade;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import bg.smoc.model.Contest;
import bg.smoc.web.utils.SessionUtil;

/**
 * @author tsvetan.bogdanov@gmail.com
 * 
 */
public class GradeServlet extends HttpServlet {

    private static final long serialVersionUID = 4483284038604869383L;

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        SessionUtil sessionUtil = SessionUtil.getInstance();
        Contest contest = sessionUtil.getContestManager().getContest(request
                .getParameter("contestId").trim());
        String userId = request.getParameter("userId").trim();
        String task = request.getParameter("task").trim();

        if (contest != null
                && userId != null
                && task != null
                && sessionUtil.getGraderManager().grade(contest, userId, task)) {
            response.sendRedirect("main");
        } else {
            response.setContentType("text/html");
            response.getOutputStream().println("<script language=\"javascript\">"
                    + "alert(\"Failed: contest/user/task combination does not exist\");"
                    + "history.go(-1);"
                    + "</script>");
        }
    }
}
