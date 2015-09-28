<%@ page contentType="text/html" errorPage="error.jsp" pageEncoding="UTF-8"%>
<%@ page errorPage="error.jsp"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<html>
<head>
	<title>System for Managing Online Contests</title>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
	<link rel="stylesheet" type="text/css" href="/css/smoc.css" />
	
	<script type="text/javascript">
	
	/* Optional: Temporarily hide the "tabber" class so it does not "flash"
	   on the page as plain HTML. After tabber runs, the class is changed
	   to "tabberlive" and it will appear. */
	
	document.write('<style type="text/css">.tabber{display:none;}<\/style>');
	
	/*==================================================
	  Set the tabber options (must do this before including tabber.js)
	  ==================================================*/
	var tabberOptions = {
	  'onClick':function(argsObj)
	  {
		  document.getElementById('active_tab').value=argsObj.tabber.tabs[argsObj.index];			  	    
	  }
	};
	</script>

	<script type="text/javascript" src="css/tabber.js"></script>
	<link rel="stylesheet" href="css/tabber.css" TYPE="text/css" MEDIA="screen">
</head>
<body class="main">

<div id="content">

	<img src="image/mainframe_header.jpg" width="980" height="191" />

    <div id="left_side">

        <div id="contest_info">
            <table>
                <tbody>
                    <tr>
	                    <td class="description_box">Login</td>
	                    <td class="normal_box">${userLogin}</td>
                    </tr>
                    <tr>
	                    <td class="description_box">Contest</td>
	                    <td class="normal_box">${contest.name}</td>
                    </tr>
                    <tr>
	                    <td class="description_box">Time</td>
	                    <td class="normal_box">${time}</td>
                    </tr>
                    <tr>
	                    <td class="description_box">Expected contest end</td>
	                    <td class="normal_box">${contest.expectedEndTime}</td>
                    </tr>
                    <tr>
	                    <td class="description_box"/>
	                    <td class="normal_box">
	                    	<a href="main" class="reload"><img src="/image/reload-btn.gif" border="0"/></a>
	                    </td>
                    </tr>
                </tbody>
            </table>
        </div>

        <div id="announcements">
            <div class="announcements_header">Announcements</div>
            <div class="announcements_content">
                ${announcement}
            </div>
        </div>
        
        <div id="accepted_submissions">
            <table>
                <thead>
                    <tr>
                        <th style="width: 120px"></th>
                        <th style="width: 200px"></th>
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
        
        <br/>
        <!-- Backup/Restore/Print File -->  
        <br/>

		<form name="form_print" enctype="multipart/form-data" method="POST" action="print">
        <table>
            <tr>
	            <td align="left">Print File:</td>
	            <td align="right"><input type="image" src="image/print-btn.gif" border="0"></td>
	        </tr>
	        <tr>
	            <td colspan="2"><input type="file" name="file" class="input_field" size="26"></td>
            </tr>
            <tr>
            </tr>
        </table>
        </form>
        
		<form name="form_store" enctype="multipart/form-data" method="POST" action="backup">
		<table>
		    <tr>
			    <td align="left">Backup File:</td>
			    <td align="right"><input type="image" src="image/backup-btn.gif" border="0"></td>
		    </tr>
		    <tr>
		    	<td colspan="2"><input type="file" name="file" class="input_field" size="26"></td>
		    </tr>
		</table>
		</form>

		<table>
		    <tr>
                <td>Restore File:</td>
                <td align="right"><a href="backup"><img src="image/restore-btn.gif" border="0"></a></td>
		    </tr>
		</table>
        
    </div>
    <div id="right_side">

		<table width="600px" border="0" cellpadding="0" cellspacing="0">
			<tr>
			   	<td><img src="image/content_frm_top_left.png" width="5" height="5" /></td>
			   	<td><img src="image/content_frm_top.png" width="590" height="5" /></td>
			   	<td><img src="image/content_frm_top_right.png" width="5" height="5" /></td>
			</tr>
		</table>

    
        <!-- Submit Source File -->
        <div class="centered">
    		<c:choose>
                <c:when test="${isContestRunning}">
                    <form name="form_submit" enctype="multipart/form-data" method="POST" action="doSubmit">
                    <table width="600" height="52" cellspacing="0" cellpadding="0" border="0">
                        <tr>
                            <td align="right" valign="middle" width="110">Submit File : &nbsp;</td>
                            <td valign="middle" width="400"><input type="file" name="submit_file" class="input_field" size="34"></td>
                            <td valign="middle"><input type="image" src="image/submit-btn.gif" border="0"></td>
                        </tr>
                        <tr>
                            <td/>
                            <td colspan="2">
                                <select name="task">
                                    <option value="" selected="selected">Task: auto</option>
                                    <c:forEach items="${contest.tasks}" var="task">
                                        <option value="${task.name}">${task.name}</option>
                                    </c:forEach>
                                </select>
                                <select name="language">
                                    <option value="" selected="selected">Lang: auto</option>
                                    <c:forEach items="${languages}" var="language">
                                        <option value="${language.language}">${language.language}</option>
                                    </c:forEach>
                                </select>
                                &nbsp;
                                <input name="always_accept" type="checkbox" value="true"/>
                                <label for="always_accept" style="font-size: smaller; font-style: oblique">Accept for grading even if sample test case fails.</label>
                            </td>
                        </tr>
                    </table>
                    </form>
                </c:when>
                <c:otherwise>
                    <div class="error_message">
                        <div>Contest is not running.</div>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>

		<input type="hidden" id="active_tab" name="last_active_tab" />
		<div class="tabber" style="width: 580px;margin-left: auto; margin-right: auto;">					
			<c:forEach items="${outputTabs}" var="tab">
		    	<div class="${tab.tabClass}">
                    <h2>${tab.task}</h2>
                    <textarea rows="20" cols="60" class="output_field1">${tab.content}</textarea>
		    	</div>
			</c:forEach>
		</div>
                    
                
        <img class="separator" src="image/bar-520.gif" border="0"/>       
        
        <c:choose>
            <c:when test="${contest.testingOn}">
                <c:choose>
                    <c:when test="${testState.processing}">
                        <div class="processing">
                            Processing Test ${testState.progress}% ... &nbsp; 
                            Click Reload &nbsp;  
                            <a href="main"><img src="image/reload-btn.gif" border="0"></a>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <div class="centered">
                            <form name="form_test" enctype="multipart/form-data" method="POST" action="doTest">
                            <table width="600" height="52" cellspacing="0" cellpadding="0" border="0">
                                <tr>
                                    <td align="right" valign="middle" width="110">Test Source File:</td>
                                    <td valign="middle" width="400"><input type="file" name="test_src_file" class="input_field" size="34"></td>
                                    <td rowspan="2" valign="middle"><input type="image" src="image/test-btn.gif" border="0"></td>
                                </tr>
                                <tr>
                                    <td align="right" valign="middle">Test Input File:</td>
                                    <td valign="middle" width="400"><input type="file" name="test_stdin_file" class="input_field" size="34"></td>
                                </tr>
                                <tr>
                                    <td/>
                                    <td align="left">
                                        <select name="task">
                                            <option value="" selected="selected">Task: auto</option>
                                            <c:forEach items="${contest.tasks}" var="task">
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
                        &nbsp;&nbsp;Test Output
                        </div>
                        <div class="centered">
                            <table cellspacing="0" cellpadding="1" border="0"  style="margin-left: auto;margin-right: auto;">
                                <tr><td bgcolor="#C8C8C8">
                                <textarea rows="15" cols="60" class="output_field2">${testState.content}</textarea>
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
    
<!--RIGHT SIDE END -->
        
    </div>

	<img src="image/mainframe_footer.jpg" width="980" height="30" />

</div>

<!-- footer -->

</body>
</html>
