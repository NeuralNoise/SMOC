<%-- Disable browser caching --%> 
<%
  response.setHeader("Pragma", "No-cache");
  response.setDateHeader("Expires", 0);
  response.setHeader("Cache-Control", "no-cache");
%>
<table cellspacing="0" cellpadding="0" border=0>
  <style type="text/css">
    <% //TODO: export elsewhere
    %>
    a {
        text-decoration: none;
        color: blue;
        background-color: #C8C8FF;
    }
    a:hover {
        color: black;
        background-color: #E0E0FF;
    }
  </style>
  <tr height=66>
    <td colspan=3>
    <!-- <img src="/image/main-01-1024.jpg" border=0> -->
    </td>
  </tr>
  <tr height=27>
    <td>
      <!-- <img src="/image/main-03.jpg" border=0> -->
      <a href="main">[Results]</a>
      <a href="contestSetup">[Contests]</a>
      <a href="accounts">[User Accounts]</a>
      <a href="persons">[Person Accounts]</a>
      <a href="logout">[Logout]</a>
    </td>
  </tr>
  <tr height="15" />
</table>
