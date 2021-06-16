package com.ruchat.gui;

import com.lyrisoft.chat.client.ChatClientApplet;
import com.lyrisoft.chat.client.gui.ChatGUI;
import com.lyrisoft.chat.client.gui.IConsole;
import com.lyrisoft.chat.client.gui.ILogin;
import com.lyrisoft.chat.client.gui.awt102.EmbeddedAppletGUIFactory;

/**
 * RUChat's own GUIFactory
 */
public class GUIFactory extends EmbeddedAppletGUIFactory {
    StatusView _statusView = new StatusView();

    public void setApplet(ChatClientApplet a) {
        super.setApplet(a);
        _statusView.setStatus("Loading, please wait.");
        _applet.setView(_statusView);
    }

    public void setMainGui(ChatGUI mainGui) {
        super.setMainGui(mainGui);
        _mainGui.setStatusGui(_statusView);
    }

    public IConsole createConsole() {
        return (IConsole)_statusView;
    }

    public void show(IConsole c) {}

    public void hide(IConsole c) {}

    public ILogin createLoginDialog() {
        return (ILogin)_statusView;
    }

    public void show(ILogin login) {
        // kinda kludgy....  in the context of RUChat.com, this will happen
        // ONLY when the connection has been closed and the gui resets.
        // hence, we are printing the message "chat exitted" here.
System.err.println("setting the view to \"chat exitted\"");
        _statusView.setStatus("Chat exitted.");
        _applet.setView(_statusView);
    }

    public void hide(ILogin login) {}

/*
    public void hide(IChatRoom room) {
        super.hide(room);
        _statusView.setStatus("Chat exitted.");
        _applet.setView(_statusView);
    }
*/
}
