/*
 * Copyright (c) 2000 Lyrisoft Solutions, Inc.
 * Used by permission
 */
package com.lyrisoft.chat.client.gui.awt102;

import java.awt.Button;
import java.awt.Color;
import java.awt.Event;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Label;
import java.awt.Panel;
import java.awt.TextField;

import com.lyrisoft.chat.Translator;
import com.lyrisoft.chat.client.gui.IChatClientInputReceiver;
import com.lyrisoft.chat.client.gui.IChatGUIFactory;
import com.lyrisoft.chat.client.gui.ILogin;

public class LoginPanel extends Panel implements ILogin {
    TextField txtLogin = new TextField(25);
    TextField txtPassword = new TextField(25);
    GridBagLayout gridBag = new GridBagLayout();
    Label lblLogin = null;
    Label lblPassword = null;
    Label lblStatus = null;
    Button btnLogin = null;
    Button btnCancel = null;
    private IChatClientInputReceiver _inputReceiver;
    boolean showPassword;

    public LoginPanel(IChatGUIFactory factory, IChatClientInputReceiver client) {
        this(factory, client, 
             Translator.getMessage("label.loginwindow"), 
             Translator.getMessage("label.userid"),
             Translator.getMessage("label.password"), 
             Translator.getMessage("label.login"), 
             Translator.getMessage("label.cancel"), 
             true);
    }

    public LoginPanel(IChatGUIFactory factory,
                      IChatClientInputReceiver client, 
                      String label, 
                      String loginLabel,
                      String passwordLabel,
                      String loginButtonLabel,
                      String cancelButtonLabel,
                      boolean showPassword) 
    {
        _inputReceiver = client;

        lblStatus = new Label(label);
        lblStatus.setForeground(Color.white);

        lblLogin = new Label(loginLabel);
        lblLogin.setForeground(Color.white);

        lblPassword = new Label(passwordLabel);
        lblPassword.setForeground(Color.white);

        btnLogin = new Button(loginButtonLabel);
        btnLogin.setForeground(Color.yellow);

        btnCancel = new Button(cancelButtonLabel);
        btnCancel.setForeground(Color.yellow);

        this.showPassword = showPassword;

        Color bgColor = (Color)factory.getAttribute("bgColor");
        if (bgColor == null) {
            bgColor = new Color(0x384CC7);
        }
        setBackground(bgColor);
        txtLogin.setBackground(Color.white);
        txtPassword.setBackground(Color.white);
        try {
            jbInit();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void setStatus(String txt) {
        lblStatus.setText(txt);
    }

    public boolean action(Event e, Object arg) {
        if (e.id == Event.ACTION_EVENT) {
            if (e.target == btnCancel) {
                _inputReceiver.loginCancelEvent();
                return true;
            } 
            if (e.target == btnLogin || 
                (showPassword && e.target == txtPassword) ||
                (!showPassword && e.target == txtLogin)) 
            {
                _inputReceiver.loginEvent(txtLogin.getText(), txtPassword.getText());
                return true;
            } else if (e.target == txtLogin) {
                txtPassword.requestFocus();
                return true;
            }
        }
        return super.action(e, arg);
    }

    public void requestFocus() {
        txtLogin.requestFocus();
    }

    private void jbInit() throws Exception {
        setLayout(gridBag);
        txtPassword.setEchoCharacter('*');
        gridBag.setConstraints(lblLogin, Constraints.create(0, 0, 1, 1, 0.0, 0.0,
                                                           GridBagConstraints.WEST, 
                                                           GridBagConstraints.NONE, 
                                                           new Insets(5, 5, 0, 0), 0, 0));
        gridBag.setConstraints(txtLogin, Constraints.create(1, 0, 1, 1, 1.0, 0.0,
                                                           GridBagConstraints.WEST, 
                                                           GridBagConstraints.HORIZONTAL, 
                                                           new Insets(5, 5, 0, 0), 0, 5));

        gridBag.setConstraints(lblPassword, Constraints.create(0, 1, 1, 1, 0.0, 0.0,
                                                              GridBagConstraints.NORTHWEST, 
                                                              GridBagConstraints.NONE, 
                                                              new Insets(5, 5, 0, 0), 0, 0));
        gridBag.setConstraints(txtPassword, Constraints.create(1, 1, 1, 1, 0.0, 0.0,
                                                              GridBagConstraints.WEST, 
                                                              GridBagConstraints.HORIZONTAL, 
                                                              new Insets(5, 5, 0, 0), 0, 5));

        gridBag.setConstraints(btnLogin, Constraints.create(0, 2, 1, 1, 0.0, 0.0,
                                                           GridBagConstraints.WEST, 
                                                           GridBagConstraints.NONE, 
                                                           new Insets(5, 5, 0, 0), 0, 0));
        gridBag.setConstraints(btnCancel, Constraints.create(1, 2, 1, 1, 0.0, 0.0,
                                                           GridBagConstraints.WEST, 
                                                           GridBagConstraints.NONE, 
                                                           new Insets(5, 5, 0, 0), 0, 0));

        gridBag.setConstraints(lblStatus, Constraints.create(0, 3, 2, 1, 0.0, 0.0,
                                                            GridBagConstraints.WEST, 
                                                            GridBagConstraints.HORIZONTAL, 
                                                            new Insets(5, 5, 0, 0), 0, 0));
        add(txtLogin);
        add(lblLogin);
        if (showPassword) {
            add(txtPassword);
            add(lblPassword);
        }
        add(btnLogin);
        add(btnCancel);
        add(lblStatus);
    }
}

