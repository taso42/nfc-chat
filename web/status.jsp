<%@ page language="java"
         import="com.lyrisoft.chat.server.remote.Formatter"
         session="false"
%>

<%!
    public static final String JAVA_VERSION = System.getProperty("java.version");
    public static final String JAVA_VENDOR = System.getProperty("java.vendor");
    public static final String OS_NAME = System.getProperty("os.name");
%>

<jsp:useBean id="chatServer"
             scope="application"
             type="com.lyrisoft.chat.server.remote.ChatServer" />


<table cellpadding=3>
  <tr>
    <td>Server name:</td>
    <td> <b><jsp:getProperty name="chatServer" property="name" /></b> </td>
  </tr>
  <tr>
    <td>Uptime:</td>
    <td> <%= Formatter.millisToString(chatServer.getUptime()) %> </td>
  </tr>
  <tr>
    <td>Cumulative logins:</td>
    <td> <jsp:getProperty name="chatServer" property="cumulativeLogins" /> </td>
  </tr>	
  <tr>
    <td>Number of users:</td>
    <td> <jsp:getProperty name="chatServer" property="userCount" /> </td>
  </tr>
  <tr>
    <td>Number of rooms:</td>
    <td> <jsp:getProperty name="chatServer" property="roomCount" /> </td>
  </tr>
  <tr>
    <td>Java Version:</td>
    <td> <%= JAVA_VERSION %> (<%= JAVA_VENDOR %>)</td>
  </tr>
  <tr>
    <td>Operating System:</td>
    <td> <%= OS_NAME %> </td>
  </tr>
  <tr>
    <td colspan=2>Memory Usage:</td>
  </tr>	
<%
    int totalMem = (int)(Runtime.getRuntime().totalMemory() / 1024);
    int freeMem = (int)(Runtime.getRuntime().freeMemory() / 1024);
%>
  <tr>
    <td align="right">total</td>
    <td> <%= totalMem %> KB</td>
  </tr>	
  <tr>
    <td align="right">free</td>
    <td> <%= freeMem %> KB</td>
  </tr>	
</table>

<p>
<font size="+1">Other servers on this network</font>
<p>
<%
    String[] servers = chatServer.getServerNames();
    int scount = 0;
    for (int i=0; i < servers.length; i++) {
        if (servers[i].equalsIgnoreCase(chatServer.getName())) {
            continue;
        }
        scount++;
        int cnt = chatServer.getUserCountOnServer(servers[i]);
        out.println("<li>" + servers[i] + " (" + cnt + " " + (cnt == 1 ? "user)" : "users)"));
    }
    if (scount == 0) {
	out.println("<i>none</i>");
    }
%>
