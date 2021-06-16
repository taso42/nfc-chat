package com.lyrisoft.chat.client.gui.jfc;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JTextField;

import com.lyrisoft.chat.client.gui.ChatGUI;
import com.lyrisoft.chat.client.gui.IChatClientInputReceiver;
import com.lyrisoft.chat.client.gui.IChatGUIFactory;
import com.lyrisoft.chat.client.gui.IPrivateChatRoom;
import com.lyrisoft.chat.server.local.IChatServer;

public class PrivateChatPanel extends Box implements IPrivateChatRoom {
    private SimpleTextPanel _text;
    private JTextField _inputField;
    private Component _container;
    private IChatServer _server;
    private IChatClientInputReceiver _receiver;
    private IChatGUIFactory _guiFactory;
    private String _name;

    public PrivateChatPanel(String name, IChatGUIFactory factory, ChatGUI mainGui,
                            IChatClientInputReceiver inputReceiver, IChatServer server)
    {
        super(BoxLayout.Y_AXIS);
        _name = "PRIVATE__" + name;
        _server = server;
        _receiver = inputReceiver;
        _guiFactory = factory;
        
        _text = new SimpleTextPanel();
        _inputField = new JTextField();
        SwingGuiFactory.tweakTextFieldSize(_inputField);
        _inputField.addActionListener(
            new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        _receiver.inputEvent(_name, _inputField.getText());
                        _inputField.setText("");
                    }
                }
        );
        add(_text);
        add(_inputField);
    }

    public String getName() {
        return _name;
    }

    public Component getContainer() {
        return _container;
    }

    public void setContainer(Component c) {
        _container = c;
    }

    public void displayPrivateMessage(String user, String message) {
        _text.displayPrivateMessage(user, message);
    }

    public void displayPrivateEmote(String user, String message) {
        _text.displayPrivateEmote(user, message);
    }
}

