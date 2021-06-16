package com.ruchat.gui;

import java.awt.GridLayout;
import java.awt.Panel;
import java.awt.TextArea;

import com.lyrisoft.chat.client.gui.IConsole;
import com.lyrisoft.chat.client.gui.ILogin;

public class StatusView extends Panel implements ILogin, IConsole {
    private TextArea _textArea;

    public StatusView() {
        _textArea = new TextArea();
        _textArea.setBackground(getBackground());
        _textArea.setEditable(false);
        setLayout(new GridLayout(1, 1));
        add(_textArea);
    }
            
    public void setStatus(String txt) {
        _textArea.setText(txt);
        _textArea.repaint();
    }

    public void show() {}
    public void hide() {}
    public void addRoom(String room, String userCount, boolean inform) {}
    public void addUser(String user, boolean inform) {}
    public void removeRoom(String room, boolean inform) {}
    public void removeUser(String user, boolean inform) {}
    public void clearRooms() {}
    public void clearUsers() {}

    public void displayPrivateMessage(String user, String message) {}
    public void displayPrivateEmote(String user, String message) {}
    public void displayMessage(String user, String message) {}
    public void displayMessage(String message) { setStatus(message); }
    public void displayError(String error) { setStatus(error); }
}
