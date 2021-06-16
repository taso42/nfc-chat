/*
 * Copyright (c) 2000 Lyrisoft Solutions, Inc.
 * Used by permission
 */
package com.lyrisoft.chat.client.gui.awt102;

import java.awt.Event;
import java.awt.Frame;
import java.awt.GridLayout;

import com.lyrisoft.chat.Translator;
import com.lyrisoft.chat.client.gui.IChatClientInputReceiver;
import com.lyrisoft.chat.client.gui.IChatGUIFactory;
import com.lyrisoft.chat.client.gui.ILogin;

public class LoginDialog extends Frame implements ILogin {
    private LoginPanel _loginPanel;
    private IChatClientInputReceiver _client;

    public LoginDialog(IChatGUIFactory factory, IChatClientInputReceiver client) {
        this(factory, client, 
             Translator.getMessage("label.login"),
             Translator.getMessage("label.loginwindow"), 
             Translator.getMessage("label.userid"),
             Translator.getMessage("label.password"), 
             Translator.getMessage("label.login"), 
             Translator.getMessage("label.cancel"), true);
    }

    public LoginDialog(IChatGUIFactory factory, 
                       IChatClientInputReceiver client, 
                       String title,
                       String label, 
                       String loginLabel,
                       String passwordLabel,
                       String loginButtonLabel,
                       String cancelButtonLabel,
                       boolean showPassword) 
    {
        _client = client;
        setTitle("Login");
        setLayout(new GridLayout(1, 1));
        _loginPanel = createLoginPanel(factory, client, label, loginLabel, passwordLabel,
                                       loginButtonLabel, cancelButtonLabel, showPassword);
        add(_loginPanel);
        pack();
    }

    public LoginPanel createLoginPanel(IChatGUIFactory factory, IChatClientInputReceiver client, 
                            String label, 
                            String loginLabel,
                            String passwordLabel,
                            String loginButtonLabel,
                            String cancelButtonLabel,
                            boolean showPassword)
    {
        return new LoginPanel(factory, client, label, loginLabel, passwordLabel,
                              loginButtonLabel, cancelButtonLabel, showPassword);
    }

    public void show() {
        super.show();
        _loginPanel.requestFocus();
    }

    public void setStatus(String txt) {
        _loginPanel.setStatus(txt);
    }

    public boolean handleEvent(Event e) {
        if (e.target == this && e.id == Event.WINDOW_DESTROY) {
            _client.loginCancelEvent();
        }
        return super.handleEvent(e);
    }
}
