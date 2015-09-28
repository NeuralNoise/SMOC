<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<html>
<head>
  <title>System for Managing Online Contests</title>
  <link rel="stylesheet" type="text/css" href="/css/smoc.css" />
</head>
<body text="#303030" marginheight="0" marginwidth="0" topmargin="0" leftmargin="0">
  <%@ include file="header.jsp" %>
  <center>
  <table>
    <tr>
      <td>Id</td>
      <td>Name</td>
      <td>Type</td>
      <td>Tests count</td>
      <td>Max submit size</td>
      <td></td>
    </tr>
    <c:forEach items="${tasks}" var="task">
      <tr>
        <td>${task.id}</td>
        <td>${task.name}</td>
        <td>${task.type} - ${taskTypes[task.type-1].string}</td>
        <td>${task.numberOfTests}</td>
        <td>${task.maxSubmitSize}</td>
        <td>
          <a href="editTask?contestId=${contestId}&taskId=${task.id}">modify</a>
          <a href="deleteTask?contestId=${contestId}&taskId=${task.id}">delete</a>
        </td>
      </tr>
    </c:forEach>
  </table>
  <br/>
  <a href="addContestTask?contestId=${contestId}">Create a new task</a>
  </center>
  <%@ include file="footer.jsp" %>
</body>
</html>