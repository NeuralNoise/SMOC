<%@ page contentType="text/html; charset=UTF8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<html>
<head>
<title>System for Managing Online Contests</title>
<link rel="stylesheet" type="text/css" href="/css/smoc.css" />
</head>
<body text="#303030" marginheight="0" marginwidth="0" topmargin="0" leftmargin="0">
<%@ include file="header.jsp"%>
<form name="EditContestForm" method="POST" action="updateContest">
Id: ${contest.id} <input type="hidden" name="id" value="${contest.id}" /><br />
Name: <input type="text" name="name" size="30" maxlength="80" value="${contest.name}" /> <br />
Public ?: <input type="checkbox" name="isPublic" <c:if test="${contest.openContest}">checked="yes"</c:if> /> <br />
Short name: <input type="text" name="shortName" size="30" maxlength="80" value="${contest.shortName}" /> <br />
Max. upload size (KB): <input type="text" name="maxUploadSize" size="30" maxlength="80" value="${contest.maxUploadSize}" /> <br />
Expected end time (shown to users!): <input type="text" name="expectedEndTime" size="30" maxlength="80" value="${contest.expectedEndTime}" /> <br />
Last start time: ${contest.lastStartTime}<br />
Announcement: <textarea rows="8" cols="60" name="announcement" class="output_field1">${contest.announcement}</textarea> <br/>
<input type="submit" value="Update">
</form>
<br/>

<i> <c:choose>
	<c:when test="${contest.running}">
      Contest is running.
    </c:when>
	<c:otherwise>
      Contest is stopped.
    </c:otherwise>
</c:choose> <c:choose>
	<c:when test="${contest.testingOn}">
      Tests are On.
    </c:when>
	<c:otherwise>
      Tests are Off.
    </c:otherwise>
</c:choose> </i>
<br/>

<a href="deleteContest?id=${contest.id}">delete</a>
&nbsp;
<a href="startContest?id=${contest.id}">start</a>
&nbsp;
<a href="stopContest?id=${contest.id}">stop</a>
&nbsp;
<a href="setTestingAllowed?value=true&id=${contest.id}">tests-On</a>
&nbsp;
<a href="setTestingAllowed?value=false&id=${contest.id}">tests-Off</a>
&nbsp;
<c:if test="${!contest.state.inAnalysisMode}">
	<a name="enableAnalysisMode" href="switchAnalysisMode?set=on&id=${contest.id}">activate Analysis mode</a>
</c:if>
<c:if test="${contest.state.inAnalysisMode}">
	<a name="disableAnalysisMode" href="switchAnalysisMode?set=off&id=${contest.id}">deactivate Analysis mode</a>
</c:if>
<br/>

<%@ include file="footer.jsp"%>
</body>
</html>