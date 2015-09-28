package bg.smoc.web.servlet.judge;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Radoslav Gerganov
 *
 */
public class LogoutJudgeServlet extends HttpServlet {

    private static final long serialVersionUID = 8840036756618851843L;

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getSession().invalidate();
        response.sendRedirect("/judge/index.jsp");
    }


}
