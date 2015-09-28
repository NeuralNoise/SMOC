<%@ page contentType="text/html" isErrorPage="true" import="java.io.*" pageEncoding="UTF-8"%>
<% 
StringWriter sw = new StringWriter();
PrintWriter pw = new PrintWriter(sw);
exception.printStackTrace(pw);
//Syslog.log(sw.toString());

// File too big
if (exception instanceof kr.or.ioi2002.RMIClientBean.HttpPostFileParser.UploadTooBigException)
{ %>
<script language="javascript">
 alert("Upload file size limit exceeded");
 history.go(-1);
</script>
<% } else { %>
<html>
<head>
<title>System Error</title>
</head>
<body>
<h1>System error</h1>
<br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/>
error<br/>
<pre><%= sw.toString() %></pre>
</body>
</html>
<% } %>
