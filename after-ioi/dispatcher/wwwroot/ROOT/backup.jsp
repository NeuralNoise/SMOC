<%@ page contentType="text/html" errorPage="error.jsp" pageEncoding="UTF-8"%>
<%@ page errorPage="error.jsp"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!-- header -->
<html>
<head>
<title>System for Managing Online Contests</title>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
<link rel="stylesheet" type="text/css" href="/css/smoc.css" />
<SCRIPT language="JavaScript">
<!--
function deletefile (fileid, filename)
{
 var confirmdelete = confirm("Do you really want to delete ["+filename+"] ?");
 if (confirmdelete== true)
 {
   window.location="backup?delete_file="+fileid;
 }
}

function deletefileall ()
{
 var confirmdeleteall1 = confirm("WARNING: All the backup files will be deleted");
 if (confirmdeleteall1== true)
 {
   var confirmdeleteall2 = confirm("Do you really want to delete all the files ?");
   if (confirmdeleteall2== true)
   {
	   window.location="backup?delete_all=true";
   }
 }
}

//-->
</SCRIPT>
</head>
<body class="main">
<div id="content">

<img src="image/mainframe_header.jpg" width="980" height="191" />

<table width="760" height="100%" cellspacing="0" cellpadding="0" border="0" align="center"><tr><td bgcolor="#FFFFFF" align="center" valign="top">

<!-- login information -->
<table width="760" height="93" cellspacing="0" cellpadding="0" border="0">
	<tr height="27">
		<td width="300"> 
			Login ID : ${userLogin}	/ Time : ${time} / Expected contest end : ${contest.expectedEndTime}
		</td>
		<td width="83">&nbsp;<a href="main"><img src="image/back-btn.gif" border="0"></a>
		</td>
	</tr>
</table>

<!-- Restore List -->	

<table width="760" cellspacing="0" cellpadding="0" border="0"><tr><td valign="top">

	<br/><br/><br/><br/>
	<center>

	<table cellspacing="0" cellpadding="0" border="0"><tr><td bgcolor="#000000">
	<table cellspacing="1" cellpadding="2" border="0">
		<tr>
			<td width="430" align="center" colspan="3"><font color="#FFFFFF"><B>Stored Files</B></font></td>
		</tr>


	<c:forEach items="${table}" var="row" varStatus="rowStatus">
		<tr>
			<td width="220" align="center" bgcolor="#ECF4FB"><a href="backup?file=${row[0]}">${row[1]}</a></td>
			<td width="150" align="center" bgcolor="#ECF4FB">${row[2]}</td>
			<td width="60" align="center" bgcolor="#ECF4FB"><a href="javascript:deletefile('${row[0]}', '${row[1]}')">delete</a></td>
		</tr>	
	</c:forEach>
 
		<tr>
			<td align="center" colspan="3" bgcolor="#A8C1DA"><a href="javascript:deletefileall()"><B>delete all</B></a></td>
		</tr>

	</table>
	</td></tr></table>
	</center>

	<br/><br/><br/><br/><br/>

</td></tr></table>

<!-- footer -->

<br/><br/>

<!-- <img src="image/bottom.gif" border=0>  -->

</td></tr></table>
</div>
</body>
</html>
