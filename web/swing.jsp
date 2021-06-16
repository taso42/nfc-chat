<%-- Swing support is very minimal right now --%>

<jsp:plugin type="applet"
            code="com.lyrisoft.chat.client.ChatClientApplet.class"
            archive="chatclient_swing.jar"
            width="400"
            height="300"
            jreversion="1.2"
> 
  <jsp:params> 
    <jsp:param name="port" value="7777" /> 
    <jsp:param name="guiFactory" value="com.lyrisoft.chat.client.gui.jfc.SwingGuiFactory" /> 
  </jsp:params> 
  <jsp:fallback> 
    <p>Unable to load applet</p> 
  </jsp:fallback> 
</jsp:plugin> 
