package bg.smoc.web.servlet.report;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import bg.smoc.web.utils.SessionUtil;

public class ComplexContestReportServlet extends HttpServlet {

    private static final long serialVersionUID = -3671791324265197756L;

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("contests", (SessionUtil.getInstance().getContestManager()
                .getContests()));

        request.getRequestDispatcher("complexContestReport.jsp").forward(request, response);
    }
}
