package com.lyrisoft.chat.client.gui.jfc;

import java.awt.Component;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

import com.lyrisoft.chat.client.gui.IChatClientInputReceiver;
import com.lyrisoft.chat.client.gui.IChatGUIFactory;
import com.lyrisoft.chat.client.gui.IChatRoom;
import com.lyrisoft.chat.server.local.IChatServer;

public class ChatPanel extends JPanel implements IChatRoom {
    private SimpleTextPanel _text;
    private String _name;
    private JList _list;
    private DefaultListModel _userModel;
    private Component _container;
    private IChatGUIFactory _guiFactory;
    private IChatClientInputReceiver _receiver;
    private IChatServer _server;
    private JSplitPane _splitPane;

    public ChatPanel(IChatGUIFactory factory, String room,
                     IChatClientInputReceiver inputReceiver, IChatServer server) 
    {
        _guiFactory = factory;
        _receiver = inputReceiver;
        _name = room;
        _server = server;

        _splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true);
        _splitPane.addComponentListener(
            new ComponentAdapter() {
                    public void componentResized(ComponentEvent e) {
                        _splitPane.setDividerLocation(0.8);
                    }

                    public void componentShown(ComponentEvent e) {
                        _splitPane.setDividerLocation(0.8);
                    }
                }
        );
        _text = new SimpleTextPanel();
        _splitPane.add(_text);
        _splitPane.add(createUserList());
        setLayout(new java.awt.BorderLayout());
        add(_splitPane, java.awt.BorderLayout.CENTER);
        add(createInputField(), java.awt.BorderLayout.SOUTH);
    }
    
    public void doLayout() {
        super.doLayout();
        _splitPane.setDividerLocation(0.8);
    }

    Component createUserList() {
        _list = new JList((_userModel = new DefaultListModel()));
        _list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        _list.addMouseListener(
            new MouseAdapter() {
                    public void mouseClicked(MouseEvent e) {
                        if ((e.getModifiers() & MouseEvent.BUTTON2_MASK) == MouseEvent.BUTTON2_MASK ||
                            (e.getModifiers() & MouseEvent.BUTTON3_MASK) == MouseEvent.BUTTON3_MASK) 
                        {
                            Point p = new Point(e.getX(), e.getY());
                            int idx = _list.locationToIndex(p);
                            if (idx >= 0) {
                                String user = (String)_userModel.getElementAt(idx);
                                JPopupMenu menu = ((SwingGuiFactory)_guiFactory).createUserPopupMenu(user);
                                menu.show(_list, e.getX(), e.getY());
                            }
                        } else {
                            if (e.getClickCount() == 2) {
                                Point p = new Point(e.getX(), e.getY());
                                int idx = _list.locationToIndex(p);
                                if (idx >= 0) {
                                    String user = (String)_list.getSelectedValue();
                                    _guiFactory.getMainGui().getPrivateRoom(user);
                                }
                            }
                        }
                    }
                }
        );
        return new JScrollPane(_list);
    }

    Component createInputField() {
        final JTextField tf = new JTextField();
        SwingGuiFactory.tweakTextFieldSize(tf);
        tf.addActionListener(
            new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        _receiver.inputEvent(_name, tf.getText());
                        tf.setText("");
                    }
                }
        );
        return tf;
    }

/*
    public void setBounds(int x, int y, int w, int h) {
        // try to prevent the slider bar from doing something stupid
        int oldWidth = _splitPane.getWidth();
//        int distanceFromRight = _splitPane.getWidth() - _splitPane.getLastDividerLocation();
        super.setBounds(x, y, w, h);
        
        int newLocation = _splitPane.getWidth() - distanceFromRight;
        if (newLocation > 0) {
            System.err.println("moving divider to " + newLocation);
            _splitPane.setDividerLocation(newLocation);
        } else {
            System.err.println("not moving divider because distance = " + distanceFromRight);
        }
    }
*/

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

    public void displayMessage(String user, String message) {
        _text.displayMessage(user, message);
    }

    public void displayMessage(String message) {
        _text.displayMessage(message);
    }

    public void displayError(String error) {
        _text.displayError(error);
    }

    public void setUserList(String[] users) {
        _list.setListData(users);
    }

    public void userJoinedRoom(String user) {
        _userModel.addElement(user);
        displayMessage(user + " joined the room");
    }

    public void userPartedRoom(String user, boolean signoff) {
        _userModel.removeElement(user);
        displayMessage(user + " left the room" +
                       (signoff ? " (signoff)" : "."));
    }

    public String getName() {
        return _name;
    }

    public static void main(String[] args) {
        ChatPanel p = new ChatPanel(null, "test", null, null);
        JFrame f = new JFrame("test");
        f.getContentPane().add(p);
        f.setSize(500, 300);
        f.setVisible(true);
    }
}
