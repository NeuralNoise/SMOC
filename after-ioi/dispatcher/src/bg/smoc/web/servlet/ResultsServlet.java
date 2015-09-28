package bg.smoc.web.servlet;

import java.io.IOException;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import bg.smoc.model.Contest;
import bg.smoc.model.web.ResultsStyle;
import bg.smoc.model.web.ResultsTable;
import bg.smoc.web.servlet.judge.ResultsTableGenerator;
import bg.smoc.web.utils.SessionUtil;

public class ResultsServlet extends HttpServlet {

    private static final long serialVersionUID = 2017784380828029991L;

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        SessionUtil sessionUtil = SessionUtil.getInstance();
        ResultsTableGenerator tableGenerator = new ResultsTableGenerator();
        tableGenerator.setSelectedContests(getSelectedContests(request, sessionUtil
                .getContestManager().getContests()));
        tableGenerator.setUserAccounts(sessionUtil.getUserAccountManager().getAllUsers());
        tableGenerator.setIncludeTestGroupResults("group".equalsIgnoreCase(request
                .getParameter("detail")));
        tableGenerator.setGraderManager(sessionUtil.getGraderManager());
        tableGenerator.setPersons(sessionUtil.getPersonManager().getAllPersons());

        tableGenerator.createReportData();

        if (Boolean.parseBoolean(request.getParameter("xls"))) {
            response.setContentType("application/ms-excel");
            response.setHeader("Content-Disposition", "attachment; filename=\"results.xls\"");

            ServletOutputStream out = response.getOutputStream();

            HSSFWorkbook resultsxls = getTableAsXLS(tableGenerator.getResultsTable());

            resultsxls.write(out);
        } else {
            request.setAttribute("table", tableGenerator.getResultsTable());
            forwardAutoReload(request);

            request.setAttribute("queryString", tableGenerator.getAutoReloadLink());
            request.setAttribute("alternativeQuery", tableGenerator.getDetailsLink());

            request.getRequestDispatcher("results_all.jsp").forward(request, response);
        }
    }

    private List<Contest> getSelectedContests(HttpServletRequest request, Vector<Contest> contests) {
        List<Contest> selectedContests = new LinkedList<Contest>();
        for (Contest contest : contests) {
            if (isCheckboxSelected(request.getParameter(contest.getId()))) {
                selectedContests.add(contest);
            }
        }
        return selectedContests;
    }

    private void forwardAutoReload(HttpServletRequest request) {
        String autoReload = request.getParameter("autoreload");
        request.setAttribute("autoreload", Boolean.parseBoolean(autoReload));
    }

    private boolean isCheckboxSelected(String parameter) {
        if (parameter == null)
            return false;
        String value = parameter.toLowerCase();
        return ("on".equals(value) || "yes".equals(value) || "true".equals(value));
    }

    private HSSFWorkbook getTableAsXLS(ResultsTable table) {

        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet = wb.createSheet("Results");

        final int MAGICWIDTH = 30;
        final int MAGICPADDING = 15;

        int[] columnWidth = new int[table.getHeader().getCells().size()];

        Hashtable<ResultsStyle, HSSFCellStyle> cellStyleHash = new Hashtable<ResultsStyle, HSSFCellStyle>();

        HSSFRow row = sheet.createRow((short) 0);
        for (int i = 0; i < table.getHeader().getCells().size(); i++) {
            HSSFCell cell = row.createCell((short) i);

            ResultsStyle style = table.getHeader().getCells().get(i).getStyle();

            if (!cellStyleHash.containsKey(style))
                cellStyleHash.put(style, style.getStyleHSSF(wb));

            cell.setCellStyle(cellStyleHash.get(style));

            String title = table.getHeader().getCells().get(i).getValue();

            cell.setCellValue(new HSSFRichTextString(title));

            sheet.setColumnWidth((short) i,
                    (short) ((title.length() * MAGICWIDTH + MAGICPADDING) * style.getFontSize()));
        }

        for (int r = 0; r < table.getRows().size(); r++) {

            row = sheet.createRow((short) r + 1);

            for (int i = 0; i < table.getRows().get(r).getCells().size(); i++) {
                HSSFCell cell = row.createCell((short) i);

                ResultsStyle style = table.getRows().get(r).getCells().get(i).getStyle();

                if (!cellStyleHash.containsKey(style))
                    cellStyleHash.put(style, style.getStyleHSSF(wb));

                cell.setCellStyle(cellStyleHash.get(style));

                String value = table.getRows().get(r).getCells().get(i).getValue();
                if (value != null) {
                    try {
                        cell.setCellValue(Double.parseDouble(value));
                    } catch (NumberFormatException e) {
                        cell.setCellValue(new HSSFRichTextString(value));
                    }

                    sheet.setColumnWidth((short) i, (short) Math.max(sheet
                            .getColumnWidth((short) i),
                            (short) ((value.length() * MAGICWIDTH + MAGICPADDING) * style
                                    .getFontSize())));
                    columnWidth[i] = Math.max(columnWidth[i], value.length());
                }
            }
        }

        return wb;
    }
}
