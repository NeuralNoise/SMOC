package bg.smoc.web.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import bg.smoc.web.utils.SessionUtil;

public class AddUserAccountsServlet extends HttpServlet {

    private static final long serialVersionUID = -8711480281204254083L;

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String users = request.getParameter("users").trim();
        String contestId = request.getParameter("selectedContest");
        SessionUtil.getInstance().getUserAccountManager().generateUsers(users, contestId);

        response.sendRedirect("accounts");
    }
}
