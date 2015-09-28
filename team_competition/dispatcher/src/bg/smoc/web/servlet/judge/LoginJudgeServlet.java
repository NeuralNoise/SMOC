/**
 * 
 */
package bg.smoc.web.servlet.judge;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import bg.smoc.web.utils.ServletUtil;

/**
 * @author tsvetan.bogdanov@gmail.com
 * 
 */
public class LoginJudgeServlet extends HttpServlet {

    private static final long serialVersionUID = -5850990089964240195L;

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String passwd = request.getParameter("pw");
        if (passwd != null && isValidAdminPassword(passwd)) {
            request.getSession().setAttribute("login", "yes");
            response.sendRedirect("main");
        } else {
            request.getSession().removeAttribute("login");
            response.setContentType("text/html");
            response.getOutputStream().println("<script language=\"javascript\">"
                    + "alert(\"Login failed\");"
                    + "history.go(-1);"
                    + "</script>");
        }
    }

    private boolean isValidAdminPassword(String passwd) {
        return ServletUtil.encryptPassword(passwd.trim()).equals(getServletContext()
                .getInitParameter("adminPassword"));
    }
}
