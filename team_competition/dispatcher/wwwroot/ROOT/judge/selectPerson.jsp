<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<html>
<head>
  <title>SMOC - User Account Management</title>
  <link rel="stylesheet" type="text/css" href="/css/smoc.css" />
</head>
<body text="#303030" marginheight="0" marginwidth="0" topmargin="0" leftmargin="0">
  <%@ include file="header.jsp" %>
  <center>
  <table cellspacing="0" cellpadding="0" border="0">
    <tr bgcolor="#A8C1DA">
      <td><b>Name</b></td>
      <td><b>Town</b></td>
      <td><b>School</b></td>
      <td><b>Year</b></td>
      <td></td>
    </tr>
    <c:forEach items="${persons}" var="person">
      <tr bgcolor="#ECF4FB">
        <td>${person.names}</td>
        <td>${person.town}</td>
        <td>${person.school}</td>
        <td>${person.schoolYear}</td>
        <td>
          <a href="addPersonLogin?personId=${person.id}&login=${login}&page=accounts">add</a>
        </td>
      </tr>
    </c:forEach>
  </table>
  </center>
  <%@ include file="footer.jsp" %>
</body>
</html>