<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<html>
<head>
  <title>SMOC - Person Account Management</title>
  <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
  <link rel="stylesheet" type="text/css" href="/css/smoc.css" />
</head>
<body text="#303030" marginheight="0" marginwidth="0" topmargin="0" leftmargin="0">
  <%@ include file="header.jsp" %>
  <center>
  <form name="AddPersonForm" method="post" action="addPerson">
  <table cellspacing="0" cellpadding="0" border="0">
    <tr>
      <td>Name:</td>
      <td><input type="text" name="names"></td>
    </tr>
    <tr>
      <td>Town:</td>
      <td><input type="text" name="town"></td>
    </tr>
    <tr>
      <td>School:</td>
      <td><input type="text" name="school"></td>
    </tr>
    <tr>
      <td>School year:</td>
      <td><input type="text" name="schoolYear"></td>
    </tr>
    <tr>
      <td colspan="2">
        <input type="submit" value="Add">
      </td>
    </tr>
  </table>
  </form>

  <br/>

  <table cellspacing="0" cellpadding="0" border="0">
    <tr bgcolor="#A8C1DA">
      <td><b>Name</b></td>
      <td><b>Town</b></td>
      <td><b>School</b></td>
      <td><b>Year</b></td>
      <td><b>Logins</b></td>
      <td></td>
    </tr>
    <c:forEach items="${persons}" var="person">
      <tr bgcolor="#ECF4FB">
        <td>${person.names}</td>
        <td>${person.town}</td>
        <td>${person.school}</td>
        <td>${person.schoolYear}</td>
        <td>
          <c:forEach items="${person.logins}" var="login">
          ${login}<br/>
          </c:forEach>
        </td>
        <td>
          <a href="editPerson?personId=${person.id}">modify</a>
          <a href="deletePerson?personId=${person.id}">delete</a>
        </td>
      </tr>
    </c:forEach>
  </table>
  </center>
  <%@ include file="footer.jsp" %>
</body>
</html>