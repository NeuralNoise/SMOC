<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<html>
<head>
  <title>SMOC - User Account Management</title>
  <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
  <link rel="stylesheet" type="text/css" href="/css/smoc.css" />
</head>
<body text="#303030" marginheight="0" marginwidth="0" topmargin="0" leftmargin="0">
  <%@ include file="/judge/header.jsp" %>
  <center>
  <form name="ContestReportForm" method="get" action="contestReport">
  <table cellspacing="0" cellpadding="0" border="0">
    <tr>
      <td>Contest Id:</td>
      <td>Name:</td>
      <td>Include?</td>
    </tr>
    <c:forEach items="${contests}" var="contest">
    <tr>
      <td>${contest.id}</td>
      <td>${contest.name}</td>
      <td><input type="checkbox" name="${contest.id}"></td>
    </tr>
    </c:forEach>
    <tr>
      <td colspan="2">
        <input type="submit" value="Generate">
      </td>
    </tr>
  </table>
  </form>
  </center>
  <%@ include file="/judge/footer.jsp" %>
</body>
</html>