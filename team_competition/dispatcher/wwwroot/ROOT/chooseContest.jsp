<%@ page contentType="text/html" errorPage="error.jsp" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<html>
<head>
<title>SMOC - User Account Management</title>
<link rel="stylesheet" type="text/css" href="/css/smoc.css" />
</head>
<body>
<table border="1">
	<tr>
		<td width="50%">
		<table>
			<tr>
				<td colspan="2">You can login in any of these contests, that you are already registered for:</td>
			</tr>
			<tr>
				<td>Contest name</td>
				<td>Status</td>
				<td></td>
			</tr>
			<c:forEach items="${availableForLogin}" var="contest">
				<tr>
					<td>${contest.name}</td>
					<td><c:choose>
						<c:when test="${contest.running}">
							<font color="green">Running</font>
						</c:when>
						<c:otherwise>
							<font color="red">Stopped</font>
						</c:otherwise>
					</c:choose></td>
					<td><a href="logIntoContest?contestId=${contest.id}">Login</a></td>
				</tr>
			</c:forEach>
		</table>
		</td>
		<td width="50%">
		<table>
			<tr>
				<td colspan="2">You can register to compete in any of these contests:</td>
			</tr>
			<tr>
				<td>Contest name</td>
				<td>Status</td>
				<td></td>
			</tr>
			<c:forEach items="${availableForRegistering}" var="contest">
				<tr>
					<td>${contest.name}</td>
					<td><c:choose>
						<c:when test="${contest.running}">
							<font color="green">Running</font>
						</c:when>
						<c:otherwise>
							<font color="red">Stopped</font>
						</c:otherwise>
					</c:choose></td>
					<td><a href="registerForContest?contestId=${contest.id}">Register</a></td>
				</tr>
			</c:forEach>
		</table>
		</td>
	</tr>
</table>
</body>
</html>