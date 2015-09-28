<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
  <title>SMOC - User Account Management</title>
  <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
  <link rel="stylesheet" type="text/css" href="/css/smoc.css" />
</head>
<body text="#303030" marginheight="0" marginwidth="0" topmargin="0" leftmargin="0">
  <%@ include file="header.jsp" %>
  <center>
  <!-- Add Users -->
  <form name="AddUsersForm" method="post" action="addUserAccounts">
  <table cellspacing="0" cellpadding="0" border="0">
    <tr>
      <td valign="top">
        <table>
          <tr>
            <td>Add users with the following logins:</td>
          </tr>
          <tr>
            <td><textarea cols="50" rows="10" name="users"></textarea></td>
          </tr>
          <tr>
            <td>
              Contest to which they should be added:
              <select name="selectedContest">
                <option value="">[None]</option>
                <c:forEach items="${contests}" var="contest">
                  <option value="${contest.id}">${contest.name}</option>
                </c:forEach>
              </select>
            </td>
          </tr>
          <tr>
            <td><input type="submit" value="Add users and generate passwords"></td>
          </tr>
        </table>
      </td>
    </tr>
  </table>
  </form>

  <br/>

  <!-- Available users -->
  <form name="ChangeContest" method="post" action="changeUsersContest">
  <table cellspacing="0" cellpadding="0" border="0">
    <tr bgcolor="#A8C1DA">
      <td width="100" align="center"><b>Login</b></td>
      <td width="100" align="center"><b>Password</b></td>
      <td align="center"><b>Contest</b></td>
      <td></td>
      <td></td>
      <td width="100" align="center"><b>Login</b></td>
    </tr>
    <c:forEach items="${table}" var="row">
      <tr bgcolor="#ECF4FB">
        <c:forEach items="${row}" var="cell">
          <td>${cell}</td>
        </c:forEach>
        <td><input type="checkbox" name="changeForUser${row[0]}"></td>
        <td>
          <a href="editAccount?login=${row[0]}">modify</a>
          <a href="deleteAccount?login=${row[0]}">delete</a>
          <a href="selectPerson?login=${row[0]}">Add to person</a>
        </td>
        <td>${row[0]}</td>
      </tr>
    </c:forEach>
  </table>
  <br/>
  Change the contest for all selected users to
  <select name="selectedContest">
    <option value="">[None]</option>
    <c:forEach items="${contests}" var="contest">
      <option value="${contest.id}">${contest.name}</option>
    </c:forEach>
  </select><br/>
  
  <input type="submit" value="Change">
  
  </form>
  
  </center>
  <%@ include file="footer.jsp" %>
</body>
</html>