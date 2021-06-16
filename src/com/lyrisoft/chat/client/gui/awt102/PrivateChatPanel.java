/*
 * Copyright (c) 2000 Lyrisoft Solutions, Inc.
 * Used by permission
 */
package com.lyrisoft.chat.client.gui.awt102;

import java.awt.Color;
import java.awt.Event;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Panel;
import java.awt.TextField;

import com.lyrisoft.awt.HyperlinkReceiver;
import com.lyrisoft.chat.ICommands;
import com.lyrisoft.chat.client.gui.ChatGUI;
import com.lyrisoft.chat.client.gui.IChatClientInputReceiver;
import com.lyrisoft.chat.client.gui.IChatGUIFactory;
import com.lyrisoft.chat.client.gui.IPrivateChatRoom;
import com.lyrisoft.chat.server.local.IChatServer;

public class PrivateChatPanel 
    extends Panel 
    implements IPrivateChatRoom, HyperlinkReceiver, ICommands 
{
    private MessageView _msgView;
    private GridBagLayout _gridBag;

    private String _name;
    private IChatGUIFactory _guiFactory;
    private ChatGUI _chatGui;
    private IChatClientInputReceiver _inputReceiver;
    private IChatServer _server;
    
    private TextField _inputField = new TextField();

    public PrivateChatPanel(String name, IChatGUIFactory factory, ChatGUI mainGui,
                           IChatClientInputReceiver inputReceiver, IChatServer server) 
    {
        _name = name;
        _guiFactory = factory;
        _chatGui = mainGui;
        _inputReceiver = inputReceiver;
        _server = server;

        initGUI();
    }

    public void handleHyperlink(String link) {
        _inputReceiver.inputEvent(_name, HYPERLINK + " " + link);
    }

    public void requestFocus() {
        _inputField.requestFocus();
    }

    public void initGUI() {
        _gridBag = new GridBagLayout();
        setLayout(_gridBag);

        _msgView = new MessageView(this);
        Color userColor = (Color)_guiFactory.getAttribute("userColor");
        if (userColor != null) {
            _msgView.getUserStyle().setColor(userColor);
        }
        Color errorColor = (Color)_guiFactory.getAttribute("errorColor");
        if (errorColor != null) {
            _msgView.getErrorStyle().setColor(errorColor);
        }

        _inputField = new TextField();

        _gridBag.setConstraints(_msgView,
                                Constraints.create(0, 0, 1, 1, 1.0, 1.0,
                                                   GridBagConstraints.CENTER,
                                                   GridBagConstraints.BOTH,
                                                   new Insets(0, 0, 0, 0), 0, 0));
        _gridBag.setConstraints(_inputField,
                                Constraints.create(0, 1, 1, 1, 1.0, 0.0,
                                                   GridBagConstraints.CENTER,
                                                   GridBagConstraints.HORIZONTAL,
                                                   new Insets(0, 0, 0, 0), 0, 4));
        add(_msgView);
        add(_inputField);
    }


    public boolean action(Event e, Object o) {
        if (e.id == Event.ACTION_EVENT) {
            if (e.target == _inputField) {
                String text = (String)o;
                if (text.length() > 0) {
                    if (text.charAt(0) == '/') {
                        _inputReceiver.inputEvent("PRIVATE__" + _name, text);
                    } else {
                        _inputReceiver.inputEvent(null, ICommands.SAY_TO_USER + " " + _name + " " + o);
                    }
                }
                _inputField.setText("");
                return true;
            }
        }
        return super.action(e, o);
    }

    public void displayPrivateMessage(String user, String message) {
        _msgView.displayMessage(user, message);
    }

    public void displayPrivateEmote(String user, String message) {
        _msgView.displayMessage(user + " " + message);
    }
}
