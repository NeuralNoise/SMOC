/**
 * 
 */
package bg.smoc.web.servlet.grade;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.ListIterator;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import bg.smoc.model.Contest;
import bg.smoc.web.utils.SessionUtil;

import kr.or.ioi2002.RMIClientBean.HttpPostFileParser;

/**
 * @author zbogi
 * 
 * CSV file in format: contestId, userId, task
 * 
 */
public class GradeCsvServlet extends HttpServlet {

    /**
     * 
     */
    private static final long serialVersionUID = -4753159772794540849L;

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpPostFileParser ur = new HttpPostFileParser();
        ur.init(request);

        LinkedList<String[]> llist = new LinkedList<String[]>();

        if (ur.nFile > 0) {
            File fileSrcFile = ur.upFile[0].GetTmpFile();

            BufferedReader br = new BufferedReader(new FileReader(fileSrcFile));
            String line = null;
            while ((line = br.readLine()) != null) {
                java.util.StringTokenizer st = new java.util.StringTokenizer(line, ",");
                if (st.countTokens() < 3)
                    continue;

                String[] tuple = new String[3];
                tuple[0] = st.nextToken().trim();
                tuple[1] = st.nextToken().trim();
                tuple[2] = st.nextToken().trim();
                llist.add(tuple);
            }
            br.close();
        } else {
            response.getOutputStream().println("File Upload Failed");
        }
        response.getOutputStream().println("<body>" + "<table border=1>");

        ListIterator<String[]> it = llist.listIterator();
        while (it.hasNext()) {
            String[] tuple = it.next();
            Contest contest = SessionUtil.getInstance().getContestManager().getContest(tuple[0]);
            response.getOutputStream().print("		 <tr>"
                    + "<td>"
                    + tuple[0]
                    + "</td>"
                    + "<td>"
                    + tuple[1]
                    + "</td>"
                    + "<td>"
                    + tuple[2]
                    + "</td>"
                    + "<td>"
                    + (contest == null ? false : SessionUtil.getInstance().getGraderManager()
                            .grade(contest, tuple[1], tuple[2]))
                    + "</td>"
                    + "</tr>");
        }
        response.getOutputStream().print("</table>" + "<a href=\"main\">OK</a>" + "</body>");
        response.setContentType("text/html");
    }
}
