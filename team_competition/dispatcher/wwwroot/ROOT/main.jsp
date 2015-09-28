<%@ page contentType="text/html" errorPage="error.jsp" pageEncoding="UTF-8"%>
<%@ page errorPage="error.jsp"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<html>
<head>
<title>System for Managing Online Contests</title>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<link rel="stylesheet" type="text/css" href="/css/smoc.css" />
</head>
<body class="main">

<div id="content">

    <div id="login_header">
        <div class="login_information">
            Login ID : ${userLogin}	/ Time : ${time} / Expected contest end : ${contest.expectedEndTime}
        </div>
        <div class="reload_holder">
            <a href="main" class="reload"><img src="/image/reload-btn.gif" border="0"/></a>
        </div>
    </div>

    <div class="clearer"> </div>

    <div id="left_side">
    
        <div class="centered">
            <div>
                You are competing in:
            </div>
            <div class="contest_name">
                ${contest.name}
            </div>
        </div>
    
        <div id="accepted_submissions">
            <table>
                <thead>
                    <tr>
                        <th style="width: 60px"></th>
                        <th style="width: 120px"></th>
                    </tr>
                </thead>
                <tbody>
                <c:forEach items="${table}" var="row" varStatus="rowStatus">
                    <tr>
                        <c:forEach items="${row}" var="cell" varStatus="cellStatus">
                            <c:choose>
                                <c:when test="${tableInfo[rowStatus.index][cellStatus.index] == 1}">
                                    <td class="left_header_box">${cell}</td>
                                </c:when>
                                <c:when test="${tableInfo[rowStatus.index][cellStatus.index] == 2}">
                                    <td class="right_header_box">${cell}</td>
                                </c:when>
                                <c:when test="${tableInfo[rowStatus.index][cellStatus.index] == 3}">
                                    <td class="description_box">${cell}</td>
                                </c:when>
                                <c:otherwise>
                                    <td class="normal_box">${cell}</td>
                                </c:otherwise>
                            </c:choose>
                        </c:forEach>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </div>
        
        <div id="announcements">
            <div class="announcements_header">Announcements</div>
            <div class="announcements_content">
                ${announcement}
            </div>
        </div>
        
    </div>
    <div id="right_side">
  
        <c:choose>
            <c:when test="${errorMessage != null}">
                <div class="error_message">
                    <div>${errorMessage}</div>
                    <div><a href="main"><img src="image/reload-btn.gif" border="0"></a></div>  
                 </div>
            </c:when>
            <c:otherwise>
                <c:choose>
                    <c:when test="${processingSubmit}">
                        <div class="processing">
                            Processing ${submitProgress}% ... &nbsp; 
                            Click Reload &nbsp;  
                            <a href="main"><img src="image/reload-btn.gif" border="0"></a>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <!-- Submit Source File -->
                        <div class="centered">
                            <form name="form_submit" enctype="multipart/form-data" method="POST" action="doSubmit">
                            <table width="500" height="52" cellspacing="0" cellpadding="0" border="0">              
                                <tr>
                                    <td align="right" valign="middle" width="115">Submit File : &nbsp;</td>
                                    <td valign="middle" width="260"><input type="file" name="submit_file" class="input_field"></td>
                                    <td valign="middle"><input type="image" src="image/submit-btn.gif" border="0"></td>
                                </tr>
                                <tr>
                                    <td/>
                                    <td>
                                        <select name="task">
                                            <option value="" selected="selected">Task: auto</option>
                                            <c:forEach items="${tasks}" var="task">
                                                <option value="${task.name}">${task.name}</option>
                                            </c:forEach>
                                        </select>
                                        <select name="language">
                                            <option value="" selected="selected">Lang: auto</option>
                                            <c:forEach items="${languages}" var="language">
                                                <option value="${language.language}">${language.language}</option>
                                            </c:forEach>
                                        </select>
                                        <br/>
                                        <input name="awlays_accept" type="checkbox" disabled="disabled" checked="checked"/>
                                        <label for="awlays_accept" style="font-size: smaller; font-style: oblique">Accept for grading even if it has all negative feedback.</label>
                                    </td>
                                    <td/>
                                </tr>
                            </table>
                            </form>
                        </div>           
        
                        <div>
                        &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; 
                        Submit Output
                        </div>
                        <div class="centered">
                        <table cellspacing="0" cellpadding="1" border="0">
                            <tr>
                                <td bgcolor="#C8C8C8">
                                    <textarea rows="14" cols="60" class="output_field1">${outputSubmit}</textarea>
                                </td>
                            </tr>
                        </table>
                        </div>
                    </c:otherwise>
                </c:choose>
                
                <img class="separator" src="image/bar-520.gif" border="0"/>
        
                
                <c:choose>
                    <c:when test="${contest.testingOn}">
                        <c:choose>
                            <c:when test="${processingTest}">
                                <div class="processing">
                                    Processing ${testProgress}% ... &nbsp; 
                                    Click Reload &nbsp;  
                                    <a href="main"><img src="image/reload-btn.gif" border="0"></a>
                                </div>
                            </c:when>
                            <c:otherwise>
                                <div class="centered">
                                    <form name="form_test" enctype="multipart/form-data" method="POST" action="doTest">
                                    <table width="500" height="52" cellspacing="0" cellpadding="0" border="0">
                                        <tr>
                                            <td align="right" valign="middle" width="115">Test Source File : &nbsp;</td>
                                            <td valign="middle" width="260"><input type="file" name="test_src_file" class="input_field"></td>
                                            <td rowspan="2" valign="middle"><input type="image" src="image/test-btn.gif" border="0"></td>
                                        </tr>
                                        <tr>
                                            <td align="right" valign="middle">Test Input File : &nbsp;</td>
                                            <td valign="middle"><input type="file" name="test_stdin_file"  class="input_field"></td>
                                        </tr>
                                        <tr>
                                            <td/>
                                            <td align="left">
                                                <select name="task">
                                                    <option value="" selected="selected">Task: auto</option>
                                                    <c:forEach items="${tasks}" var="task">
                                                        <option value="${task.name}">${task.name}</option>
                                                    </c:forEach>
                                                </select>
                                                <select name="language">
                                                    <option value="" selected="selected">Lang: auto</option>
                                                    <c:forEach items="${languages}" var="language">
                                                        <option value="${language.language}">${language.language}</option>
                                                    </c:forEach>
                                                </select>
                                            </td>
                                            <td/>
                                        </tr>
                                    </table>
                                    </form>
                                </div>
                            
                                <div>
                                &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; 
                                Test Output
                                </div>
                                <div class="centered">
                                    <table cellspacing="0" cellpadding="1" border="0"><tr><td bgcolor="#C8C8C8">
                                    <textarea rows="14" cols="60" class="output_field2">${outputTest}</textarea>
                                    </td></tr>
                                    </table>
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </c:when>
                    <c:otherwise>
                        <div class="processing">
                            Test Not Available
                        </div>
                    </c:otherwise>
                </c:choose>
            </c:otherwise>
        </c:choose>

        <img class="separator" src="image/bar-520.gif" border="0">
        
        <!-- Backup/Restore/Print File -->  
        
        <table cellspacing="1" cellpadding="3" border="0">
            <tr>
                <td colspan="3">
                <form name="form_print" enctype="multipart/form-data" method="POST" action="print">
                <table>
                    <tr>
                    <td align="right" width="115">Print File:</td>
                    <td width="260"><input type="file" name="file" class="input_field"></td>
                    <td><input type="image" src="image/print-btn.gif" border="0"></td>
                    </tr>
                </table>
                </form>
                </td>
            </tr>
    
            <tr>
                <td colspan="3">
                <form name="form_store" enctype="multipart/form-data" method="POST" action="backup">
                <table>
                    <tr>
                    <td align="right" width="115">Backup File:</td>
                    <td width="260"><input type="file" name="file" class="input_field"></td>
                    <td><input type="image" src="image/backup-btn.gif" border="0"></td>
                    </tr>
                </table>
                </form>
                </td>
            </tr>
            <tr>
                <td width="115"> </td>
                <td align="right" width="260">Restore File:</td>
                <td><a href="backup" ><img src="image/restore-btn.gif" border="0"></a></td>
            </tr>
        </table>
    
        <br/><br/>
<!--RIGHT SIDE END -->
        
    </div>


<!-- footer -->


</div>

</body>
</html>
