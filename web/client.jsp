<%@ page language="java" session="false" %>

<html>
<head><title>NFC Chat</title></head>
<body bgcolor="#ffffff" text="#000000">

This is a demo of NFC in action.  If you are looking for the regular
web site (for info, downloads, etc), go <a
href="http://nfcchat.sourceforge.net/">here</a>. 
<hr>


<table border="0">
  <tr>
    <td align="center"><font size="+2"><b>NFC Chat Demo</b></font></td>
  </tr>	
  <tr>
    <td>
      <jsp:include page="applet.jsp" flush="true" />
    </td>
  </tr>
  <tr>
    <td align="center">
      <small>Powered by <a href="http://nfcchat.sourceforge.net/">NFC</a></small><br>
      <small>Copyright (c) 2000-2002 Lyrisoft Solutions, Inc.</small>
    </td>
  </tr>	
</table>  

<hr>
<h2>Status</h2>
<P>
<small>as of <%= (new java.util.Date()) %></small>
<p>
<jsp:include page="status.jsp" flush="true" />

</body>
</html>
