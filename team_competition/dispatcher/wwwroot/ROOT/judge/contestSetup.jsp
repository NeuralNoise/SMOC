<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<html>
<head>
  <title>System for Managing Online Contests</title>
  <link rel="stylesheet" type="text/css" href="/css/smoc.css" />
  
	<script type="text/javascript">
	<!--
	function confirmer(question, forwardTo) {
		var answer = confirm(question)
		if (answer) {
			window.location = forwardTo;
		}
	}
	function confirmGraderLoad(forwardTo) {
		confirmer("The operation you are about to perform will add a serious load to the graders. "
				+ "Are you sure you want to continue?", forwardTo)
	}
	//-->
	</script>
  
</head>
<body text="#303030" marginheight="0" marginwidth="0" topmargin="0" leftmargin="0">
  <%@ include file="header.jsp" %>
  <center>
  <form name="AddNewContestForm" method="POST" action="addNewContest">
    <h2>Add new contest</h2><br/>
    Name:
    <input type="text" name="name" size="30" maxlength="80"> <br/>
    Public ?:
    <input type="checkbox" name="isPublic"> <br/>
    Short name:
    <input type="text" name="shortName" size="30" maxlength="80"> <br/>
    Max. upload size (KB):
    <input type="text" name="maxUploadSize" size="10" value="10240" maxlength="10"> <br/>
    <input type="submit" value="Add contest">
  </form>
  <br/>
  <br/>
  <table border="1">
    <tr>
      <td>Id</td>
      <td>Name</td>
      <td>Visibility</td>
      <td>Short name</td>
      <td>Expected end time</td>
      <td>Last start time</td>
      <td>UploadSize</td>
      <td>Running?</td>
      <td>Start/stop</td>
      <td>Modification</td>
      <td>Grade</td>
    </tr>
    <c:forEach items="${contests}" var="contest">
      <tr>
        <td>${contest.id}</td>
        <td>${contest.name}</td>
        <td>
        	<c:choose>
	        	<c:when test="${contest.openContest}">
	        		Public
	        	</c:when>
	        	<c:otherwise>
	        		Invite only
	        	</c:otherwise>
	        </c:choose>
        </td>
        <td>${contest.shortName}</td>
        <td>${contest.expectedEndTime}</td>
        <td>${contest.lastStartTime}</td>
        <td>${contest.maxUploadSize}</td>
        <td>
          <i>
          <c:choose>
            <c:when test="${contest.running}">
            	<font color="green">running</font>
            </c:when>
            <c:otherwise>
            	<font color="red">stopped</font>
            </c:otherwise>
          </c:choose>
          <br/>
          <c:choose>
            <c:when test="${contest.testingOn}">
              <font color="green">tests-On</font>
            </c:when>
            <c:otherwise>
              <font color="red">tests-Off</font>
            </c:otherwise>
          </c:choose>
          </i>
        </td>
        <td>
          <c:if test="${!contest.running}">
              <a href="startContest?id=${contest.id}">start</a>
          </c:if>
          <c:if test="${contest.running}">
              <a href="stopContest?id=${contest.id}">stop</a>
          </c:if>
          <br/>
          <c:if test="${!contest.testingOn}">
              <a href="setTestingAllowed?value=true&id=${contest.id}">tests-On</a>
          </c:if>
          <c:if test="${contest.testingOn}">
              <a href="setTestingAllowed?value=false&id=${contest.id}">tests-Off</a>
          </c:if>
        </td>
        <td>
            <center>
            <a name="editContest" href="editContest?id=${contest.id}">modify</a> &nbsp;
            <a href="updateTaskList?contestId=${contest.id}">tasks</a>
            <br/>
            <a href="javascript:confirmer('Are you sure you want to delete the contest?', 'deleteContest?id=${contest.id}')">delete</a>
            </center>
        </td>
        <td>
        	<a href="javascript:confirmGraderLoad('pushTests?contestId=${contest.id}')">Push tests to graders</a><br/>
        	<a href="results_all?${contest.id}=on">View results</a><br/>
        	<a href="javascript:confirmGraderLoad('gradeContest?contestId=${contest.id}')">Grade all submits</a>
        </td>
      </tr>
    </c:forEach>
  </table>
  </center>
  <%@ include file="footer.jsp" %>
</body>
</html>