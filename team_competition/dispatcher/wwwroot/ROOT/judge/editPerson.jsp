<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<html>
<head>
  <title>SMOC - Person Account Management</title>
  <link rel="stylesheet" type="text/css" href="/css/smoc.css" />
</head>
<body text="#303030" marginheight="0" marginwidth="0" topmargin="0" leftmargin="0">
  <%@ include file="header.jsp" %>
  <center>
  <table cellspacing="0" cellpadding="0" border="0">
    <form name="UpdatePersonForm" method="post" action="updatePerson">
      <tr>
        <td>Id:</td>
        <td><input type="hidden" name="id" value="${person.id}">${person.id}</td>
      </tr>
      <tr>
        <td>Name:</td>
        <td><input type="text" name="names" value="${person.names}"></td>
      </tr>
      <tr>
        <td>Town:</td>
        <td><input type="text" name="town" value="${person.town}"></td>
      </tr>
      <tr>
        <td>School:</td>
        <td><input type="text" name="school" value="${person.school}"></td>
      </tr>
      <tr>
        <td>School year:</td>
        <td><input type="text" name="schoolYear" value="${person.schoolYear}"></td>
      </tr>
      <tr>
        <td colspan="2">Logins:</td>
      </tr>
      <c:forEach items="${person.logins}" var="login">
        <tr>
          <td>${login}</td>
          <td><a href="removePersonLogin?personId=${person.id}&login=${login}">remove</a></td>
        </tr>
      </c:forEach>
      <tr>
        <td colspan="2">
          <input type="submit" value="Update">
        </td>
      </tr>
    </form>
    <tr>
      <form name="AddPersonLoginForm" method="post" action="addPersonLogin">
        <td>
          <input type="hidden" name="personId" value="${person.id}">
          <input type="text" name="login">
        </td>
        <td>
          <input type="submit" value="Add">
        </td>
      </form>
    </tr>
  </table>
  </center>
  <%@ include file="footer.jsp" %>
</body>
</html>