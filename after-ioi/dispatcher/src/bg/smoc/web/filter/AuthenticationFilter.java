package bg.smoc.web.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class AuthenticationFilter implements Filter {

    public void destroy() {
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String servletPath = (httpRequest).getServletPath();

        if (!needsAuthentication(servletPath)) {
            chain.doFilter(request, response);
            return;
        }

        if (servletPath.startsWith("/judge")) {
            if (httpRequest.getSession().getAttribute("login") == null) {
                ((HttpServletResponse) response).sendRedirect("/judge/index.jsp");
                return;
            }
        } else {
            if (httpRequest.getSession().getAttribute("id") == null) {
                ((HttpServletResponse) response).sendRedirect("/index.jsp");
                return;
            }
        }

        chain.doFilter(request, response);
    }

    /**
     * Returns whether the specified path should be verified to be inside a
     * logged-in session.
     * 
     * @param servletPath
     *            the path to be checked
     * @return whether the path can only be accessed after log-in
     */
    private boolean needsAuthentication(String servletPath) {
        if (servletPath.startsWith("/image")
                || servletPath.startsWith("/css")
                || servletPath.startsWith("/tasks")
                || servletPath.startsWith("/data"))
            return false;

        return !("/index.jsp".equals(servletPath)
                || "/login".equals(servletPath)
                || "/error.jsp".equals(servletPath)
                || "/judge/index.jsp".equals(servletPath)
                || "/judge/login".equals(servletPath)
                || "/register".equals(servletPath) || "/registerUser.jsp".equals(servletPath));
    }

    public void init(FilterConfig config) throws ServletException {
    }

}
