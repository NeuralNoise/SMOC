<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<!-- header -->
<html>
<head>
  <title>System for Managing Online Contests</title>
  <c:if test="${autoreload}">
    <meta http-equiv="REFRESH" content="2"/>
  </c:if>
  <link rel="stylesheet" type="text/css" href="/css/smoc.css" />
</head>
<body text="#303030" marginheight="0" marginwidth="0" topmargin="0" leftmargin="0">
  <%@ include file="header.jsp" %>
  <table width="1000" height="100%" cellspacing="0" cellpadding="0" border="0">
    <tr height="27">
      <td colspan="3">
      <c:choose>
        <c:when test="${autoreload}">
          <a href="main?autoreload=false">Disable Auto Reload</a>
        </c:when>
        <c:otherwise>
          <a href="main?autoreload=true">Enable Auto Reload</a>
        </c:otherwise>
      </c:choose> Time : ${dateTimeNow}
      </td>
    </tr>
    <tr>
      <td width="1" bgcolor="#C8C8C8"></td>
      <td valign="TOP"><!--Grade tasks-->
        <table width="100%">
          <tr>
            <td align="center"><!-- Grade single user/task -->
              <form name="GradeForm" action="grade" method="POST">
                <table height="52" cellspacing="0" cellpadding="0" border="0">
                  <tr>
                    <td align="right" valign="middle">contest</td>
                    <td align="center" valign="middle">
                      <input type="text" name="contestId" size="12" maxlength="32" tabindex="2" class="input_field">
                    </td>
                    <td rowspan="3" valign="middle"><input type="image" src="/image/grade-btn.gif" border="0"></td>
                  </tr>
                  <tr>
                    <td align="right" valign="middle">user</td>
                    <td align="center" valign="middle">
                      <input type="text" name="userId" size="12" maxlength="32" tabindex="2" class="input_field">
                    </td>
                  </tr>
                  <tr>
                    <td align="right" valign="middle">task</td>
                    <td align="center" valign="middle">
                      <input type="text" name="task" size="12" maxlength="32" tabindex="2" class="input_field">
                    </td>
                  </tr>
                </table>
              </form>
            </td>
            <td align="center">
              <form name="GradeCsv" enctype="multipart/form-data" method="POST"  action="gradeCsv">
                <table height="52" cellspacing="0" cellpadding="0" border="0">
                  <tr>
                    <td>CSV file</td>
                    <td rowspan="2">
                      <input type="image" src="/image/grade-btn.gif" border="0">
                    </td>
                  </tr>
                  <tr>
                    <td><input type="file" name="csv_file" class="input_field"></td>
                  </tr>
                </table>
              </form>
            </td>
          </tr>
        </table>

        <center><!-- Common grading options --> <br/>
          <br/>
          <b><a href="complexContestReport">Get a cross-contest report</a></b> <br/>
          <br/>

          <!-- Pending (contest) submits and tests --> <br/>
          Submission[${fn:length(submitQueue)}], &nbsp;&nbsp;&nbsp;
          Test[${fn:length(testQueue)}], &nbsp;&nbsp;&nbsp;
          Grading[${fn:length(gradeQueue)} ] <br/>

          <table cellspacing="1" cellpadding="2" border="0" width="700">
            <tr bgcolor="#A8C1DA" align="center">
              <td width="25%"><B>Submission Queue</B></td>
              <td width="25%"><B>Test Queue</B></td>
              <td><B>Grading Queue</B></td>
            </tr>
            <tr bgcolor="#ECF4FB" align="center">
              <td><c:forEach items="${submitQueue}" var="item">
                ${item}<br/>
              </c:forEach></td>
              <td><c:forEach items="${testQueue}" var="item">
                ${item}<br/>
              </c:forEach></td>
              <td><c:forEach items="${gradeQueue}" var="item">
                ${item}<br/>
              </c:forEach></td>
            </tr>
          </table>

          <!-- Pending agents --> <br/>
          Agents [${fn:length(busyQueue) + fn:length(idleQueue)}] <br/>

          <table cellspacing="1" cellpadding="2" border="0" width="700">
            <tr bgcolor="#A8C1DA" align="center">
              <td><b>IP address</b></td>
              <td><b>Version</B></td>
              <td><b>Registered Time</b></td>
              <td><b>Processing</b></td>
            </tr>
            <tr bgcolor="#A8C1DA" align="center">
              <td colspan="4"><B>Busy Agent Queue [${fn:length(busyQueue)}]</B></td>
            </tr>
            <c:forEach items="${busyQueue}" var="agent">
              <tr bgcolor="#ECF4FB" align="center">
                <td>${agent[0]}</td>
                <td>${agent[1]}</td>
                <td>${agent[3]} ${agent[4]}</td>
                <td>${agent[5]}</td>
              </tr>
            </c:forEach>
            <tr bgcolor="#A8C1DA" align="center">
              <td colspan="4"><B>Idle Contest Agent Queue
              [${fn:length(idleQueue)}]</B></td>
            </tr>
            <c:forEach items="${idleQueue}" var="agent">
              <tr bgcolor="#ECF4FB" align="center">
                <td>${agent[0]}</td>
                <td>${agent[1]}</td>
                <td>${agent[3]} ${agent[4]}</td>
                <td>${agent[5]}</td>
              </tr>
            </c:forEach>

          </table>
        </center>
      <!--Right side end -->
      </td>
    </tr>
  </table>

  <%@ include file="footer.jsp" %>
</body>
</html>
