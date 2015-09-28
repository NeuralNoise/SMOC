<%@ page contentType="text/html" errorPage="error.jsp" pageEncoding="UTF-8"%>
<jsp:useBean id="printer" scope="application" class="kr.or.ioi2002.RMIClientBean.printer.PrinterDaemon" />
<%
// not login
String userid = (String)session.getAttribute("id");
if (userid == null) response.sendRedirect("index.jsp"); else {

// Upload file size limit
final int MAX_UPLOAD_SIZE = 1*1024*1024;	// 1M byte

// request parsing
kr.or.ioi2002.RMIClientBean.HttpPostFileParser ur = new kr.or.ioi2002.RMIClientBean.HttpPostFileParser();
ur.init(request, MAX_UPLOAD_SIZE);
	    
if (ur.nFile > 0)
{
	java.io.File file = ur.upFile[0].GetTmpFile();
	boolean bRet = printer.print(file, request.getRemoteAddr(), userid);
	if (bRet)
	{
		response.sendRedirect("main?error=50");
	} else
	{
		response.sendRedirect("main?error=52");
	}
}
else	// upfile number = 0
{
	response.sendRedirect("main?error=51");
}

}	// if ~ sendRedirect		
%>
