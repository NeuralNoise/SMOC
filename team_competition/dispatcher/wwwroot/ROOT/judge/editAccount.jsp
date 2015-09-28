<%@ page contentType="text/html charset=UTF8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>

<html>
<head>
<title>System for Managing Online Contests</title>
<link rel="stylesheet" type="text/css" href="/css/smoc.css" />
</head>
<body text="#303030" marginheight="0" marginwidth="0" topmargin="0"
	leftmargin="0">
<%@ include file="header.jsp"%>
<center>
<form name="EditUserAccount" method="post" action="updateAccount">
<table>
	<tr>
		<td>Login:</td>
		<td><input type="hidden" name="login" value="${user.login}">
		${user.login}</td>
	</tr>
	<tr>
		<td>Password:</td>
		<td><input type="text" name="password" value="${user.password}"></td>
	</tr>
	<tr>
		<td>Contest: <select name="contests" multiple="yes">
			<c:forEach items="${contests}" var="contest">
				<option value="${contest.id}"
					<c:forEach items="${user.contestIds}" var="id">
            		<c:if test="${contest.id eq id}">selected</c:if>
            	</c:forEach>>${contest.name}</option>
			</c:forEach>
		</select></td>
	</tr>
</table>
<input type="submit" value="Update"></form>
</center>
<%@ include file="footer.jsp"%>
</body>
</html>