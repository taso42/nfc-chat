<%@ page language="java" session="false" %>

<script language="JavaScript">
<!--

    function setDimensions(guiSelector) {
       if (guiSelector.options[0].selected) {
           document.theForm.width.value=400;
           document.theForm.height.value=150;
       } else {
           document.theForm.width.value=600;
           document.theForm.height.value=400;
       }
    }

    function setColor(colorSelector) {
        for (var i=0; i < colorSelector.options.length; i++) {
            if (colorSelector.options[i].selected) {
                document.theForm.bgColor.value = colorSelector.options[i].value;
            }
        }
    }

    function setRoom(roomSelector) {
        for (var i=0; i < roomSelector.options.length; i++) {
            if (roomSelector.options[i].selected) {
                document.theForm.autojoin.value = roomSelector.options[i].value;
            }
        }
    }

//-->
</script>

This is a demo of NFC in action.  If you are looking for the regular
web site (for info, downloads, etc), go <a
href="http://nfcchat.sourceforge.net/">here</a>. 
<hr>

If you want, just leave everything blank and click submit.
<form method="post" action="client.jsp" name="theForm">
<table border=0>
  <tr>
    <td>Screen name</td>
    <td><input name="authId"></td>
  </tr>
  <tr>
    <td>Initial room</td>
    <td>
	<select name="rooms" onChange="setRoom(this)">
	  <option value="">None</option>
	  <option value="Dev">Dev</option>
	  <option value="Lobby">Lobby</option>
	  <option value="Test">Test</option>
        </select>	      
        <input name="autojoin" value="">
    </td>	
  </tr>
  <tr>
    <td>Mode</td>
    <td>
	<select name="guiFactory" onChange="setDimensions(this)">
	<option value="com.lyrisoft.chat.client.gui.awt102.AppletGUIFactory">
	    Multi-window
        </option>	    
	<option value="com.lyrisoft.chat.client.gui.awt102.EmbeddedAppletGUIFactory">
	    Embedded in browser
        </option>	    
	</select>
    </td>	
  </tr>
  <tr>
    <td>Applet width</td>
    <td><input name="width" value="400"></td>
  </tr>
  <tr>
    <td>Applet height</td>
    <td><input name="height" value="150"></td>	
  </tr>
  <tr>
    <td>Background</td>
    <td>
	<select name="colors" onChange="setColor(this)">
	  <option value="6299CD">default</option>
	  <option value="0000FF">blue</option>
	  <option value="FF0000">red</option>
	  <option value="00FF00">green</option>
	  <option value="000000">black</option>
	  <option value="FFFFFF">white</option>
        </select>	      
        <input name="bgColor" value="6299CD">
    </td>	
  </tr>
  <tr>
    <td colspan="2">
        <input type="submit">
    </td>
  </tr>    
</table>

</form>
