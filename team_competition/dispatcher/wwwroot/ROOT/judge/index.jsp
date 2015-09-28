<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<html>
<head>
  <title>System for Managing Online Contests</title>
  <link rel="stylesheet" type="text/css" href="/css/smoc.css" />
</head>
<body bgcolor="#ffffff" text="#303030" marginheight="0" marginwidth="0" topmargin="0" leftmargin="0">

<table width="100%" height="100%" cellspacing="50" cellpadding="0" border="0">
  <tr>
    <td align="center" valign="middle">

    <table width="500" height="127" cellspacing="0" cellpadding="0" border="0">
      <tr>
        <td width="363" align="center" valign="middle">

		<form name="PasswordForm" action="login" method="POST">
        <table width="340" height="52" cellspacing="0" cellpadding="0" border="0">
          <tr>
            <td align="right" valign="middle">Administration Password</td>
            <td align="center" valign="middle">
              <input type="password" name="pw" size="12" maxlength="32" tabindex="2" class="input_field">
            </td>
            <td valign="middle">
              <input type="image" name="connectButton" src="/image/connect-btn.gif" border="0"></td>
          </tr>
        </table>
        </form>

        </td>
      </tr>
    </table>

    </td>
  </tr>
</table>

<script language="javascript">PasswordForm.pw.focus();</script>

</body>
</html>
