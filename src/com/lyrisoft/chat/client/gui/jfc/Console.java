package com.lyrisoft.chat.client.gui.jfc;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ListSelectionModel;

import com.lyrisoft.chat.client.gui.IChatClientInputReceiver;
import com.lyrisoft.chat.client.gui.IChatGUIFactory;
import com.lyrisoft.chat.client.gui.IConsole;
import com.lyrisoft.chat.server.local.IChatServer;

public class Console extends JPanel implements IConsole {
    private IChatServer _server;
    private IChatClientInputReceiver _receiver;
    private IChatGUIFactory _guiFactory;

    private SimpleTextPanel _text;
    private DefaultListModel _roomModel;
    private DefaultListModel _userModel;
    private Component _container;

    public Console(IChatServer server, IChatClientInputReceiver receiver, IChatGUIFactory f) {
        _server = server;
        _receiver = receiver;
        _guiFactory = f;

        _text = new SimpleTextPanel();
        _text.setMinimumSize(new Dimension(1, 50));
        JSplitPane p = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true);
        p.add(_text);
        p.add(createRoomAndUserPanel());
        p.setDividerLocation(0.5);
        setLayout(new java.awt.GridLayout());
        add(p);
    }

    public Component getContainer() {
        return _container;
    }

    public void setContainer(Component c) {
        _container = c;
    }

    Component createRoomAndUserPanel() {
        JSplitPane p = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true);
        p.add(createRoomPanel());
        p.add(createUserPanel());
        p.setDividerLocation(0.5);
        return p;
    }

    Component createRoomPanel() {
        Box b = new Box(BoxLayout.Y_AXIS);
        JLabel label = new JLabel("Rooms");
        final JList rooms = new JList((_roomModel = new DefaultListModel()));
        rooms.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        rooms.addMouseListener(
            new MouseAdapter() {
                    public void mouseClicked(MouseEvent e) {
                        if ((e.getModifiers() & MouseEvent.BUTTON2_MASK) == MouseEvent.BUTTON2_MASK ||
                            (e.getModifiers() & MouseEvent.BUTTON3_MASK) == MouseEvent.BUTTON3_MASK) 
                        {
                            Point p = new Point(e.getX(), e.getY());
                            int idx = rooms.locationToIndex(p);
                            if (idx >= 0) {
//                                String room = (String)rooms.getSelectedValue();
                                String room = (String)_roomModel.getElementAt(idx);
                                JPopupMenu menu = ((SwingGuiFactory)_guiFactory).createRoomPopupMenu(room);
                                menu.show(rooms, e.getX(), e.getY());
                            }
                        } else if (e.getClickCount() == 2) {
                            Point p = new Point(e.getX(), e.getY());
                            int idx = rooms.locationToIndex(p);
                            if (idx >= 0) {
                                String room = (String)rooms.getSelectedValue();
                                _server.joinRoom(room, null);
                            }
                        }
                    }
                }
                    );
//        _roomModel.addElement("room1");
//        _roomModel.addElement("room2");
        JButton button = new JButton("update");
        button.addActionListener(
            new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        _server.requestRoomList();
                    }
                }
        );
        b.add(label);
        b.add(new JScrollPane(rooms));
        b.add(button);
        return b;
    }
    
    Component createUserPanel() {
        Box b = new Box(BoxLayout.Y_AXIS);
        JLabel label = new JLabel("Users");
        final JList users = new JList((_userModel = new DefaultListModel()));
        users.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        users.addMouseListener(
            new MouseAdapter() {
                    public void mouseClicked(MouseEvent e) {
                        if ((e.getModifiers() & MouseEvent.BUTTON2_MASK) == MouseEvent.BUTTON2_MASK ||
                            (e.getModifiers() & MouseEvent.BUTTON3_MASK) == MouseEvent.BUTTON3_MASK) 
                        {
                            Point p = new Point(e.getX(), e.getY());
                            int idx = users.locationToIndex(p);
                            if (idx >= 0) {
//                                String user = (String)users.getSelectedValue();
                                String user = (String)_userModel.getElementAt(idx);
                                JPopupMenu menu = ((SwingGuiFactory)_guiFactory).createUserPopupMenu(user);
                                menu.show(users, e.getX(), e.getY());
                            }
                        } else {
                            if (e.getClickCount() == 2) {
                                Point p = new Point(e.getX(), e.getY());
                                int idx = users.locationToIndex(p);
                                if (idx >= 0) {
                                    String user = (String)users.getSelectedValue();
                                    _guiFactory.getMainGui().getPrivateRoom(user);
                                }
                            }
                        }
                    }
                }
        );

//        _userModel.addElement("user1");
//        _userModel.addElement("user2");
        JButton button = new JButton("update");
        button.addActionListener(
            new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        _server.requestUserList();
                    }
                }
        );
        b.add(label);
        b.add(new JScrollPane(users));
        b.add(button);
        return b;
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

    public void addRoom(String room, String userCount, boolean inform) {
        _roomModel.addElement(room);
    }

    public void addUser(String user, boolean inform) {
        _userModel.addElement(user);
    }

    public void removeRoom(String room, boolean inform) {
        _roomModel.removeElement(room);
    }
    
    public void removeUser(String user, boolean inform) {
        _userModel.removeElement(user);
    }

    public void clearRooms() {
        _roomModel.removeAllElements();
    }

    public void clearUsers() {
        _userModel.removeAllElements();
    }

    public static void main(String[] args) {
        Console c = new Console(null, null, null);
        JFrame frame = new JFrame("test");
        frame.getContentPane().add(c);
        frame.setSize(400, 300);
        frame.setVisible(true);
    }
}

