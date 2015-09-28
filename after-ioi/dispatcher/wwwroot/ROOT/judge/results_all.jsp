<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<html>
  <head>
    <title>System for Managing Online Contests</title>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <link rel="stylesheet" type="text/css" href="/css/smoc.css" />
    <c:if test="${autoreload}">
        <meta http-equiv="refresh" content="2">
    </c:if>
  </head>
  <body text="#303030" marginheight="0" marginwidth="0" topmargin="0" leftmargin="0">
    <%@ include file="header.jsp" %>
    <table width="1000" height="93" cellspacing="0" cellpadding="0" border="0">
      <tr height="27">
        <td width="10"> &nbsp;</td>
        <td width="750" valign="middle"> 
          <c:choose>
            <c:when test="${autoreload}">
              <a href="results_all?${queryString}&autoreload=false">Disable Auto Reload</a>
            </c:when>
            <c:otherwise>
              <a href="results_all?${queryString}&autoreload=true">Enable Auto Reload</a>
            </c:otherwise>
          </c:choose>
          &nbsp;
          <a href="results_all?${alternativeQuery}">Switch detail level</a>
          Server: ${grader.RMIServerURL}
        </td>
        <td width="330"><img src="/image/main-04.jpg" border="0"></td>
      </tr>
    </table>
    
    <center>
    <br/><br/>

    Available results:<br/>
    <a href="results_all?${queryString}&xls=true">(download as xls)</a>

    <table cellpadding="0" border="0">
      <tr bgcolor="#ECF4FB">
        <c:forEach items="${table.header.cells}" var="title">
          <td><span style="${title.style.styleHTML}">${title.value}</span></td>
        </c:forEach>
      </tr>
      <c:forEach items="${table.rows}" var="row">
        <tr bgcolor="#ECF4FB" align="center">
          <c:forEach items="${row.cells}" var="cell">
            <td>
            	<span style="${cell.style.styleHTML}">
            	<c:if test="${cell.href ne null}"><a href="download_source${cell.href}"></c:if>
            	${cell.value}
            	<c:if test="${cell.href ne null}"></a></c:if>
            	</span>
            </td>
          </c:forEach>
        </tr>
      </c:forEach>
    </table>
    </center>
    <%@ include file="footer.jsp" %>
  </body>
</html>