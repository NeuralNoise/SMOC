<%@ page contentType="text/html" errorPage="error.jsp" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<html>
<head>
	<title>SMOC - User Account Management</title>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
	<link rel="stylesheet" type="text/css" href="/css/smoc.css" />
</head>
<body class="registration">
<div id="content">
    <form name="RegisterForm" action="/register" method="POST">
	<table>
        <c:if test="${errorMessage != null}">
        <tr>    
            <td align="right"><font color="#FF0000">Error:</font></td>
            <td><font color="#FF0000">${errorMessage}</font></td>
        </tr>
        </c:if>
	    <tr>
		    <td>Login:</td>
		    <td><input type="text" name="username" value="${username}"></td>
		</tr>
        <tr>
            <td>Password:</td>
            <td><input type="password" name="password" value="${password}"></td>
        </tr>
        <tr>
            <td>Re-type password:</td>
            <td><input type="password" name="repassword" value="${repassword}"></td>
        </tr>
        <tr>
        <!-- Имена: --> 
            <td>Names:</td>
            <td><input type="text" name="names" value="${names}"></td>
        </tr>
        <tr>
            <td><!--  Град: --> Country, Town</td>
            <td><input type="text" name="town" value="${town}"></td>
        </tr>
        <tr>
            <td>Email:</td>
            <td><input type="text" name="email" value="${email}"></td>
        </tr>
        <tr>
            <td colspan="2" align="center"><input type="submit" name="submitted" value="Register me"></td>
        </tr>
	</table>
	</form>
</div>
</body>
</html>