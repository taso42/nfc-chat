/*
 * Copyright (c) 2000 Lyrisoft Solutions, Inc.
 * Used by permission
 */
package com.lyrisoft.chat.client.gui.awt102;

import java.awt.Color;
import java.awt.Component;

import com.lyrisoft.awt.HyperlinkReceiver;
import com.lyrisoft.chat.ICommands;
import com.lyrisoft.chat.Translator;
import com.lyrisoft.chat.client.gui.IChatClientInputReceiver;
import com.lyrisoft.chat.client.gui.IChatGUIFactory;

/**
 * JDK1.0.2 compliant ChatPanel subclass.  This one uses a TextView instead of the crappy
 * TextArea AWT class.  This allows for word-wrapping and pretty colors.
 *
 * @see com.lyrisoft.awt.TextView
 */
public class EnhancedChatPanel extends ChatPanel implements HyperlinkReceiver {
    protected MessageView _txtMessages;

    public EnhancedChatPanel(IChatGUIFactory factory, 
                             String room, String title, 
                             IChatClientInputReceiver inputReceiver) 
    {
        super(factory, room, title, inputReceiver);
    }

    public EnhancedChatPanel(IChatGUIFactory factory, 
                             String room,
                             IChatClientInputReceiver inputReceiver) 
    {
        this(factory, room, room, inputReceiver);
    }

    public void setFont(String name) {
        _txtMessages.setFont(name);
        displayMessage(Translator.getMessage("newfont"));
    }

    public void setFontSize(int size) {
        _txtMessages.setFontSize(size);
        displayMessage(Translator.getMessage("newfont"));
    }

    public void handleHyperlink(String link) {
        _inputReceiver.inputEvent(_room, ICommands.HYPERLINK + " " + link);
    }

    /**
     * This is the "enhanced" bit..   We use java.awt.TextView
     * instead of the traditional java.awt.TextArea
     */
    protected Component createTextWidget() {
        _txtMessages = new MessageView(this);
        
        Color userColor = (Color)_factory.getAttribute("userColor");
        if (userColor != null) {
            _txtMessages.getUserStyle().setColor(userColor);
        }
        Color errorColor = (Color)_factory.getAttribute("errorColor");
        if (errorColor != null) {
            _txtMessages.getErrorStyle().setColor(errorColor);
        }
        return _txtMessages;
    }

    public void displayPrivateMessage(String user, String message) {
        _factory.playAudioClip("private.au");
        _txtMessages.displayPrivateMessage(user, message);
    }

    public void displayMessage(String user, String message) {
        _txtMessages.displayMessage(user, message);
    }

    public void displayMessage(String message) {
        _txtMessages.displayMessage(message);
    }

    public void displayError(String error) {
        _txtMessages.displayError(error);
    }

    public void displayPrivateEmote(String user, String message) {
        _factory.playAudioClip("private.au");
        _txtMessages.displayPrivateEmote(user, message);
    }

    public void showBigButtons(boolean b) {
        super.showBigButtons(b);
    }
}
