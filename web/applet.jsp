<%-- NFC Launcher --%>

<%@ page language="java" %>

<jsp:useBean id="chatServer"
             scope="application"
             type="com.lyrisoft.chat.server.remote.ChatServer" />

<%!
    String getParameter(HttpServletRequest request, String name, String defaultVal) {
        String s = request.getParameter(name);
        return s == null ? defaultVal : s;
    }

    int getIntParameter(HttpServletRequest request, String name, int defaultVal) {
        String s = request.getParameter(name);
        try {
            return Integer.parseInt(s);
        }
        catch (Exception e) {
            return defaultVal;
        }
    }

%>

<%
    int port = chatServer.getPort();
//    int port = getIntParameter(request, "port", 8199);
    int width = getIntParameter(request, "width", 400);
    int height = getIntParameter(request, "height", 150);
%>

    <applet code="com.lyrisoft.chat.client.ChatClientApplet"
	archive="chatclient_awt.jar"
        width="<%= width %>"
        height="<%= height %>"
    >
      <param name="port" value="<%= port %>">

<%
    String guiFactory = getParameter(request, "guiFactory",
            "com.lyrisoft.chat.client.gui.awt102.AppletGUIFactory");
%>
      <param name="guiFactory" value="<%= guiFactory %>">
<%
    String authId = request.getParameter("authId");
    if (authId != null && authId.trim().length() > 0) {
%>
      <param name="autologin" value="<%= authId %>">
<%
    }
    String autojoin = request.getParameter("autojoin");
    if (autojoin != null && autojoin.length() > 0) {
%>
      <param name="autojoin" value="<%= autojoin %>">
<%
    }
    String bgColor = request.getParameter("bgColor");
    if (bgColor != null) {
%>
      <param name="bgColor" value="<%= bgColor %>">
      <param name="tunnelRead" value="<%= request.getContextPath() %>/servlet/tunnel">
      <param name="tunnelWrite" value="<%= request.getContextPath() %>/servlet/tunnel">
      <param name="tunnelOnly" value="false">
<%
    }
%>
      </applet>






