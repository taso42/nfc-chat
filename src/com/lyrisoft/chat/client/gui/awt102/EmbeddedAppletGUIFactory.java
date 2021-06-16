/*
 * Copyright (c) 2000 Lyrisoft Solutions, Inc.
 * Used by permission
 */
package com.lyrisoft.chat.client.gui.awt102;

import java.awt.Component;
import java.awt.Container;
import java.awt.Frame;

import com.lyrisoft.chat.client.ChatClientApplet;
import com.lyrisoft.chat.client.gui.IChatRoom;
import com.lyrisoft.chat.client.gui.IConsole;
import com.lyrisoft.chat.client.gui.ILogin;

/**
 * Everything is embedded in the browser. 
 * Joining a room implies leaving the room you're in.
 */
public class EmbeddedAppletGUIFactory extends AppletGUIFactory  {
    Frame _frame;
//    IChatRoom _currentRoom;
    IConsole _console;

    public void setApplet(ChatClientApplet a) {
        super.setApplet(a);
        Container c = a;
        do {
            c = c.getParent();
            if (c instanceof Frame) {
                System.err.println("applet frame bounds = " + c.bounds());
                _frame = (Frame)c;
                return;
            }
        } while (c != null);
        _frame = new Frame();
    }

    public IConsole createConsole() 
    {
        if (_console == null) {
            _console = new Console(_server, _inputReceiver, this);
        }
        return _console;
    }

    public IChatRoom createChatRoom(String name)
    {
        ChatPanel p = new EnhancedChatPanel(this, name, _inputReceiver);
        return p;
    }

    public com.lyrisoft.chat.client.gui.IQuery createQuery(String title, 
                              String choiceLabel, String[] choices, 
                              boolean showTextField, String textFieldLabel) 
    {
        return new Query(_frame, title, choiceLabel, choices, showTextField, textFieldLabel);
    }

    public void hide(ILogin login) {
        _applet.removeView((Component)login);
    }

    public void show(ILogin login) {
        _applet.setView((Component)login);
    }
    
    public void show(IConsole c) {
        _applet.setView((Component)c);
        _mainGui.setStatusGui(c);
    }
    
    public void hide(IConsole c) {
        if (c instanceof Component) {
            _applet.removeView((Component)c);
        }
    }
    
    public synchronized void hide(IChatRoom room) {
        _applet.removeView((ChatPanel)room);
/*        if (((Component)room).isVisible()) {
            _applet.removeView((ChatPanel)room);
            _currentRoom = null;
            }*/
    }

    public synchronized void show(IChatRoom room) {
/*        if (_currentRoom != null) {
            String name = _currentRoom.getName();
            _server.partRoom(name);
//            hide(_currentRoom);
} */
        _applet.setView((ChatPanel)room);
        _mainGui.setStatusGui(room);
//        _currentRoom = room;
    }
}

