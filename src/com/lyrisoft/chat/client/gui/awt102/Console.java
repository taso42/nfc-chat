/*
 * Copyright (c) 2000 Lyrisoft Solutions, Inc.
 * Used by permission
 */
package com.lyrisoft.chat.client.gui.awt102;

import java.awt.Button;
import java.awt.Color;
import java.awt.Event;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Label;
import java.awt.List;
import java.awt.Panel;

import com.lyrisoft.awt.HyperlinkReceiver;
import com.lyrisoft.chat.ICommands;
import com.lyrisoft.chat.Translator;
import com.lyrisoft.chat.client.gui.IChatClientInputReceiver;
import com.lyrisoft.chat.client.gui.IChatGUIFactory;
import com.lyrisoft.chat.client.gui.IConsole;
import com.lyrisoft.chat.client.gui.IQuery;
import com.lyrisoft.chat.client.gui.IQueryCallback;
import com.lyrisoft.chat.server.local.IChatServer;

public class Console 
    extends Panel 
    implements IConsole, HyperlinkReceiver, IQueryCallback
{
    protected int roomCnt = 0;
    protected int userCnt = 0;
    protected Color bgColor;
    protected IChatClientInputReceiver _inputReceiver;
    private IChatServer _server;
    private List lstRooms;
    private List lstUsers;
    private IChatGUIFactory _factory;

    private Label lblUserCnt;
    private Label lblRoomCnt;
    private Button btnCreate;
    private Button btnUpdate;
    private Button btnJoin;
    private Button btnExit;

    private MessageView _txtMessages;
    private boolean _keepGoing = true;

    private static final int CREATE = 0;
    private static final int JOIN = 1;
    private static final int QUIT = 2;

    public Console(IChatServer server, IChatClientInputReceiver receiver, IChatGUIFactory f) 
    {
        _factory = f;
        _server = server;
        _inputReceiver = receiver;

        bgColor = (Color)f.getAttribute("bgColor");
        if (bgColor == null) {
            bgColor = new Color(0x384CC7);
        }

        setupGUI();
    }

    public void handleHyperlink(String link) {
        _inputReceiver.inputEvent("console", ICommands.HYPERLINK + " " + link);
    }

    public void setupGUI() {
        GridBagLayout gridBag = new GridBagLayout();
        setLayout(gridBag);
        setBackground(bgColor);

        int row = 0;
        
        // title label
        Label l = createLabel(Translator.getMessage("label.console"));
        gridBag.setConstraints(l,
                               Constraints.create(0, row, 2, 1, 1.0, 0.0,
                                                  GridBagConstraints.WEST,
                                                  GridBagConstraints.HORIZONTAL,
                                                  new Insets(0, 0, 0, 0),
                                                  0, 0));
        add(l);


        // update button & exit buttons
        btnUpdate = new Button(Translator.getMessage("label.update"));
        btnUpdate.setBackground(bgColor);
        btnUpdate.setForeground(Color.yellow);
        gridBag.setConstraints(btnUpdate,
                               Constraints.create(2, row, 1, 1, 0.0, 0.0,
                                                  GridBagConstraints.WEST,
                                                  GridBagConstraints.NONE,
                                                  new Insets(0, 0, 0, 0),
                                                  0, 0));
        add(btnUpdate);

        btnExit = new Button(Translator.getMessage("label.quit"));
        btnExit.setBackground(bgColor);
        btnExit.setForeground(Color.yellow);
        gridBag.setConstraints(btnExit,
                               Constraints.create(3, row, 1, 1, 0.0, 0.0,
                                                  GridBagConstraints.WEST,
                                                  GridBagConstraints.NONE,
                                                  new Insets(0, 0, 0, 0),
                                                  0, 0));
        add(btnExit);
        
        // ----- // ----- // ----- // ----- // ----- // ----- // ----- 
        row++;
        _txtMessages = new MessageView(this);
//        FixedPanel p = new FixedPanel(new ScrollView(_txtMessages), 400, 75);
        FixedPanel p = new FixedPanel(_txtMessages, 400, 75);
        gridBag.setConstraints(p, Constraints.create(0, row, 4, 1, 0.0, 0.0,
                                                                GridBagConstraints.CENTER,
                                                                GridBagConstraints.BOTH,
                                                                new Insets(5, 5, 5, 5),
                                                                0, 0));
        add(p);

        // ----- // ----- // ----- // ----- // ----- // ----- // ----- 
        row++;
        l = createLabel(Translator.getMessage("label.rooms"));
        gridBag.setConstraints(l,
                               Constraints.create(0, row, 1, 1, 1.0, 0.0,
                                                  GridBagConstraints.WEST,
                                                  GridBagConstraints.HORIZONTAL,
                                                  new Insets(0, 0, 0, 0),
                                                  0, 0));
        add(l);

        lblRoomCnt = createLabel("(0)");
        gridBag.setConstraints(lblRoomCnt,
                               Constraints.create(1, row, 1, 1, 1.0, 0.0,
                                                  GridBagConstraints.WEST,
                                                  GridBagConstraints.HORIZONTAL,
                                                  new Insets(0, 0, 0, 0),
                                                  0, 0));
        add(lblRoomCnt);

        l = createLabel(Translator.getMessage("label.users"));
        gridBag.setConstraints(l,
                               Constraints.create(2, row, 1, 1, 1.0, 0.0,
                                                 GridBagConstraints.WEST,
                                                  GridBagConstraints.HORIZONTAL,
                                                  new Insets(0, 0, 0, 0),
                                                  0, 0));
        add(l);

        lblUserCnt = createLabel("(0)");
        gridBag.setConstraints(lblUserCnt,
                               Constraints.create(3, row, 1, 1, 1.0, 0.0,
                                                  GridBagConstraints.WEST,
                                                  GridBagConstraints.HORIZONTAL,
                                                  new Insets(0, 0, 0, 0),
                                                  0, 0));
        add(lblUserCnt);

        // ----- // ----- // ----- // ----- // ----- // ----- // ----- 
        row++;
        // lists
        lstRooms = new List();
        lstRooms.setBackground(Color.lightGray);
        gridBag.setConstraints(lstRooms,
                               Constraints.create(0, row, 2, 1, 1.0, 1.0,
                                                  GridBagConstraints.CENTER,
                                                  GridBagConstraints.BOTH,
                                                  new Insets(0, 0, 0, 0),
                                                  0, 0));
        add(lstRooms);

        lstUsers = new List();
        lstUsers.setBackground(Color.lightGray);
        gridBag.setConstraints(lstUsers,
                               Constraints.create(2, row, 2, 1, 1.0, 1.0,
                                                  GridBagConstraints.CENTER,
                                                  GridBagConstraints.BOTH,
                                                  new Insets(0, 0, 0, 0),
                                                  0, 0));
        add(lstUsers);


        // ----- // ----- // ----- // ----- // ----- // ----- // ----- 
        row++;
        btnJoin = new Button(Translator.getMessage("label.join.room"));
        btnJoin.setForeground(Color.yellow);
        btnJoin.setBackground(bgColor);
        gridBag.setConstraints(btnJoin, Constraints.create(0, row, 1, 1, 0.0, 0.0,
                                                           GridBagConstraints.WEST,
                                                           GridBagConstraints.NONE,
                                                           new Insets(0, 0, 0, 0),
                                                           0, 0));
        add(btnJoin);

        btnCreate = new Button(Translator.getMessage("label.create.room"));
        btnCreate.setForeground(Color.yellow);
        btnCreate.setBackground(bgColor);
        gridBag.setConstraints(btnCreate, Constraints.create(1, row, 1, 1, 0.0, 0.0,
                                                             GridBagConstraints.WEST,
                                                             GridBagConstraints.NONE,
                                                             new Insets(0, 0, 0, 0),
                                                             0, 0));
        add(btnCreate);


    }

    public Label createLabel(String label) {
        Label l = new Label(label);
        l.setBackground(bgColor);
        l.setForeground(Color.white);
        l.setFont(new Font("Dialog", Font.BOLD, 14));
        return l;
    }

    public void addUser(String user, boolean inform) {
        synchronized (lstUsers) {
            lstUsers.addItem(user);
        }
        userCnt++;
        lblUserCnt.setText("(" + userCnt + ")");
        if (inform) {
            displayMessage(Translator.getMessage("signon", user));
        }
    }

    public void addRoom(String room, String count, boolean inform) {
        synchronized (lstRooms) {
            String users = null;
            if (count.equals("1")) {
                users = Translator.getMessage("users1", "1");
            } else {
                users = Translator.getMessage("users2", count);
            }
            lstRooms.addItem(room + " (" + users + ")");

        }
        roomCnt++;
        lblRoomCnt.setText("(" + roomCnt + ")");
        if (inform) {
            displayMessage(Translator.getMessage("room.created", "\"" + room + "\""));
        }
    }

    public void removeRoom(String room, boolean inform) {
        String upperCaseRoom = room.toUpperCase();
        boolean found = false;
        synchronized (lstRooms) {
            int cnt = lstRooms.countItems();
            for (int i=0; i < cnt; i++) {
                if (lstRooms.getItem(i).toUpperCase().startsWith(upperCaseRoom)) {
                    lstRooms.delItem(i);
                    found = true;
                    break;
                }
            }
        }
        if (found) {
            roomCnt--;
            lblRoomCnt.setText("(" + roomCnt + ")");
            if (inform) {
                displayMessage(Translator.getMessage("room.destroyed", "\"" + room + "\""));
            }
        }
    }
    
    public void removeUser(String user, boolean inform) {
        boolean found = false;
        synchronized (lstUsers) {
            int cnt = lstUsers.countItems();
            for (int i=0; i < cnt; i++) {
                if (lstUsers.getItem(i).equals(user)) {
                    lstUsers.delItem(i);
                    found = true;
                    break;
                }
            }
        }
        if (found) {
            userCnt--;
            lblUserCnt.setText("(" + userCnt + ")");
            if (inform) {
                displayMessage(Translator.getMessage("signonff", user));
            }
        }
    }

    public void clearRooms() {
        synchronized (lstRooms) {
            lstRooms.delItems(0, lstRooms.countItems()-1);
            roomCnt = 0;
            lblRoomCnt.setText("(" + roomCnt + ")");
        }
    }

    public void clearUsers() {
        synchronized (lstUsers) {
            lstUsers.delItems(0, lstUsers.countItems()-1);
            userCnt = 0;
            lblUserCnt.setText("(" + userCnt + ")");
        }
    }

    public boolean action(Event e, Object arg) {
        if (e.id == Event.ACTION_EVENT) {
            if (e.target == lstRooms) {
                String s = (String)arg;
                int idx = s.lastIndexOf("(");
                if (idx >= 0) {
                    String room = s.substring(0, idx-1);
                    _server.joinRoom(room, null);
                }
                return true;
            } else if (e.target == lstUsers) {
                _inputReceiver.userDoubleClick((String)arg);
                return true;
            }
        }
        return super.action(e, arg);
    }

    private String[] getRoomList() {
        synchronized (lstRooms) {
            String[] list = new String[lstRooms.countItems()];
            for (int i=0; i < list.length; i++) {
                list[i] = lstRooms.getItem(i);
            }
            return list;
        }
    }

    public boolean handleEvent(Event e) {
        if (e.id == Event.ACTION_EVENT) {
            if (e.target == btnCreate) {
                IQuery q = _factory.createQuery(Translator.getMessage("label.creating.room"),
                                                null, null, true, 
                                                Translator.getMessage("label.room.name") + ":");
                q.setCallbackParams(CREATE, this);
                q.show();
            }
            if (e.target == btnJoin) {
                IQuery q = _factory.createQuery(Translator.getMessage("label.joining.room"),
                                                Translator.getMessage("label.room") + ":",
                                                getRoomList(), false, null);
                q.setCallbackParams(JOIN, this);
                q.show();
            }
            if (e.target == btnUpdate) {
                _server.requestRoomList();
                _server.requestUserList();
                return true;
            }
            if (e.target == btnExit) {
                IQuery q = _factory.createQuery(Translator.getMessage("confirm.exit"),
                                                null, null, false, null);
                q.setCallbackParams(QUIT, this);
                q.show();
                return true;
            }
        } 
        return super.handleEvent(e);
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
//        _factory.playAudioClip("error.au");
        _txtMessages.displayError(error);
    }

    public void displayPrivateEmote(String user, String message) {
        _factory.playAudioClip("private.au");
        _txtMessages.displayPrivateEmote(user, message);
    }

    public void pleaseStop() {
        _keepGoing = false;
    }

    public void handleQuery(int id, IQuery q) {
        if (q.getCanceled()) {
            q.dispose();
            return;
        }

        String user;
        String msg;

        switch (id) {
        case CREATE:
            _server.joinRoom(q.getText(), null);
            break;
        case JOIN:
            String s = q.getSelection();
            int idx = s.indexOf("(");
            if (idx >= 0) {
                String room = s.substring(0, idx-1);
                _server.joinRoom(room, null);
            }
            break;
        case QUIT:
            _server.signOff();
            break;
        }
        q.dispose();
    }
}









