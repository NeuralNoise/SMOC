<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
  <title>System for Managing Online Contests</title>
  <link rel="stylesheet" type="text/css" href="/css/smoc.css" />
</head>
<body text="#303030" marginheight="0" marginwidth="0" topmargin="0" leftmargin="0">
  <%@ include file="header.jsp" %>
  <table>
	<form name="UpdateTask" method="post" action="updateTask">
		<input type="hidden" name="contestId" value="${contestId}">
    <tr>
      <td>Id:</td>
      <td>
        <input type="hidden" name="id" value="${task.id}">
        ${task.id}
      </td>
    </tr>
    <tr>
      <td>Name:</td>
      <td><input name="name" value="${task.name}"></td>
    </tr>
    <tr>
      <td>Type:</td>
      <td>
        <select name="type">
          <c:forEach items="${taskTypes}" var="type">
            <option value="${type.numeric}" <c:if test="${type.numeric == task.type}">selected</c:if>>${type.string}</option>
          </c:forEach>
        </select>
      </td>
    </tr>
    <tr>
      <td>Tests count:</td>
      <td><input name="numberOfTests" value="${task.numberOfTests}"></td>
    </tr>
    <tr>
      <td>Max. submit size (B):</td>
      <td><input type="text" name="maxSubmitSize" size="30" maxlength="80" value="${task.maxSubmitSize}" /></td>
    </tr>
    <tr>
    	<td> 
    		<input type="submit" value="Update">
    	</td>
    </tr>
  	</form>
  	<tr>
  		<td><br/></td>
  	</tr>
  	<form name="UploadTests" method="post" enctype="multipart/form-data" action="uploadTestData">
  		<input type="hidden" name="contestId" value="${contestId}">
  		<input type="hidden" name="taskId" value="${task.id}">
    <tr>
        <td>Upload tests:</td>
        <td><input type="file" name="submit_file" class="input_field"> <input type="submit" value="Upload tests"></td>
    </tr>
    <tr>
    	<td colspan="2">
    		<font color="red">
    		<c:forEach items="${messages}" var="message">
    			${message}<br/>
    		</c:forEach>
    		</font>
    	</td>
    </tr>
    </form>
  </table>
  <br/>
  <br/>
  <table>
  	<tr>
  		<td>
  			Test cases for ${task.name}.
  		</td>
  		<td>
  			Test groups setup for ${task.name}.
  		</td>
  	</tr>
  	<tr>
  		<td>
		  <table cellpadding="0" border="1" cellspacing="0">
		  	<tr bgcolor="#ECF4FB" align="center" >
	  			<td width="20%">Index</td>
		  		<td width="40%">Input</td>
		  		<td>Solution</td>
		  	</tr>
		  	<c:forEach items="${taskTests}" var="testInfo" varStatus="loop">
		  	<tr bgcolor="#ECF4FB" align="center">
	  			<td width="20%">
	  				${loop.index + 1}
	  			</td>
		  		<c:forEach items="${testInfo}" var="dataLink">
		  			<td width="40%">
		  				<c:if test="${dataLink != ''}">
		  					<a href="${dataLink}"/>view</a>
		  				</c:if>
		  				<c:if  test="${dataLink == ''}">
		  					missing!
		  				</c:if>
		  			</td>
		  		</c:forEach>
		  	</tr>
		  	</c:forEach>
		</table>
		</td>
		<td>
			<table cellpadding="0" border="1" cellspacing="0">
			  	<tr bgcolor="#ECF4FB" align="center" >
			  		<td>Test group</td>
			  		<td>Test cases</td>
			  		<td>Points awared</td>
			  		<td>Feedback</td>
			  	</tr>
			  	<c:forEach items="${testGroups}" var="testGroup" varStatus="loop">
				  	<tr bgcolor="#ECF4FB" align="center">
				  		<td>${loop.index + 1}</td>
				  		<td>
					  		<c:forEach items="${testGroup.testCases}" var="testNumber" varStatus="innerLoop">
					  			${testNumber}<c:if test="${not innerLoop.last}">,</c:if>
					  		</c:forEach>
					  	</td>
					  	<td>${testGroup.points}</td>
					  	<td>
					  		<c:if test="${testGroup.feedbackEnabled}">yes</c:if>
					  		<c:if test="${!testGroup.feedbackEnabled}">no</c:if>
					  	</td>
				  	</tr>
			  	</c:forEach>
			</table>
		</td>
	</tr>
</table>
  <%@ include file="footer.jsp" %>
</body>
</html>