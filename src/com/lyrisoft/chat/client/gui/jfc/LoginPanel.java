package com.lyrisoft.chat.client.gui.jfc;

import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import com.lyrisoft.chat.client.gui.IChatClientInputReceiver;
import com.lyrisoft.chat.client.gui.ILogin;

public class LoginPanel extends JPanel implements ILogin {
    private IChatClientInputReceiver _receiver;
    private JTextField _textField;
    private JPasswordField _passField;
    private JLabel _status;
    private Component _container;

    public LoginPanel(IChatClientInputReceiver receiver) {
        _receiver = receiver;

        Box b = new Box(BoxLayout.Y_AXIS);
        b.add(createInputAreas());
        b.add(Box.createVerticalStrut(5));
        b.add(createButtonArea()); 
        b.add(Box.createVerticalStrut(5)); 
        b.add(createStatusArea());
       
        setLayout(new GridLayout(1, 1));
        add(b);
    }

    public Component getContainer() {
        return _container;
    }

    public void setContainer(Component c) {
        _container = c;
    }

    Component createInputAreas() {
        Box b = new Box(BoxLayout.X_AXIS);
        b.add(createLabels());
        b.add(createInputFields());
        return b;
    }

    Component createButtonArea() {
        Box b = new Box(BoxLayout.X_AXIS);
        JButton loginButton = new JButton("Login");
        loginButton.addActionListener(
            new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        doLogin();
                    }
                }
        );
        
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(
            new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        _receiver.loginCancelEvent();
                    }
                }
        );
        
        b.add(loginButton);
        b.add(cancelButton); 
        return b;
    }

    Component createLabels() {
        Box b = new Box(BoxLayout.Y_AXIS);
        b.add(new JLabel("name"));
        b.add(new JLabel("password"));
        return b;
    }

    Component createInputFields() {
        Box b = new Box(BoxLayout.Y_AXIS);
        _textField = new JTextField();
        _textField.addActionListener(
            new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        _passField.requestFocus();
                    }
                }
        );
        _passField = new JPasswordField(); 
        _passField.addActionListener(
            new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        doLogin();
                    }
                }
        );

        SwingGuiFactory.tweakTextFieldSize(_textField);
        SwingGuiFactory.tweakTextFieldSize(_passField);

        b.add(_textField);
        b.add(_passField);  
        return b;
    }

    Component createStatusArea() {
        Box b = new Box(BoxLayout.X_AXIS);
        _status = new JLabel("Enter your credentials and click login", 
                             SwingConstants.LEFT);
        b.add(_status);
        b.add(Box.createHorizontalGlue());
        return b;
    }

    void doLogin() {
        String name = _textField.getText();
        char[] pass = _passField.getPassword();
        _textField.setText("");
        _passField.setText("");
        _receiver.loginEvent(name, new String(pass));
    }

    public void setStatus(String txt) {
        _status.setText(txt);
    }
}
