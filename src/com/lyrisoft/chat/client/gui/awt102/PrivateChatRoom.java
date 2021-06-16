/*
 * Copyright (c) 2000 Lyrisoft Solutions, Inc.
 * Used by permission
 */
package com.lyrisoft.chat.client.gui.awt102;

import java.awt.Event;
import java.awt.Frame;
import java.awt.GridLayout;

import com.lyrisoft.chat.Translator;
import com.lyrisoft.chat.client.gui.ChatGUI;
import com.lyrisoft.chat.client.gui.IChatClientInputReceiver;
import com.lyrisoft.chat.client.gui.IChatGUIFactory;
import com.lyrisoft.chat.client.gui.IPrivateChatRoom;
import com.lyrisoft.chat.server.local.IChatServer;

public class PrivateChatRoom extends Frame implements IPrivateChatRoom {
    private PrivateChatPanel _panel;
    private String _name;
    private ChatGUI _chatGui;
    
    public PrivateChatRoom(String name, IChatGUIFactory factory, ChatGUI mainGui,
                           IChatClientInputReceiver inputReceiver, IChatServer server)
    {
        _name = name;
        _chatGui = mainGui;
        _panel = new PrivateChatPanel(name, factory, mainGui, inputReceiver, server);
        setTitle(Translator.getMessage("label.private.chat", name));
        setLayout(new GridLayout(1, 1));
        add(_panel);
        resize(400, 200);
    }

    public void displayPrivateMessage(String user, String message) {
        _panel.displayPrivateMessage(user, message);
    }

    public void displayPrivateEmote(String user, String message) {
        _panel.displayPrivateEmote(user, message);
    }

    public boolean handleEvent(Event e) {
        if (e.target == this && e.id == Event.WINDOW_DESTROY) {
            _chatGui.closePrivateChatRoom(_name);
            return true;
        }
        return super.handleEvent(e);
    }
}


