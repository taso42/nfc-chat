/*
 * Copyright (c) 2000 Lyrisoft Solutions, Inc.
 * Used by permission
 */
package com.lyrisoft.chat.client.gui.awt102;

import java.awt.Button;
import java.awt.Color;
import java.awt.Component;
import java.awt.Event;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Label;
import java.awt.List;
import java.awt.Menu;
import java.awt.MenuItem;
import java.awt.Panel;
import java.awt.TextArea;
import java.awt.TextField;

import com.lyrisoft.awt.ImageButton;
import com.lyrisoft.chat.ICommands;
import com.lyrisoft.chat.Translator;
import com.lyrisoft.chat.client.gui.IChatClientInputReceiver;
import com.lyrisoft.chat.client.gui.IChatGUIFactory;
import com.lyrisoft.chat.client.gui.IChatRoom;
import com.lyrisoft.chat.client.gui.IQuery;
import com.lyrisoft.chat.client.gui.IQueryCallback;

/**
 * JDK1.0.2 compliant ChatPanel.
 *
 * This object implements IChatRoom.  It is a Panel, not a Frame.  This way, we
 * can stick these things anywhere, not necessarily just in a frame.
 */
public class ChatPanel extends Panel implements IChatRoom, IQueryCallback {
    protected Color bgColor;
    protected IChatClientInputReceiver _inputReceiver;
    protected String _room;
    protected String _title;
    protected IChatGUIFactory _factory;
    
    protected GridBagLayout gridbag = new GridBagLayout();
    protected TextArea txtMessages;
    protected List lstUsers;
    protected TextField txtInput;

    protected Panel _buttonPanel;
    
    protected Component btnClose;
    protected Component btnEmote;
    protected Component btnPing;
    protected Component btnUserInfo;
    protected Component btnEmotePrivate;
    protected Component btnPrivate;
    protected Component btnStats;
    protected Component btnHelp;

    protected MenuItem miPrivate = new MenuItem(Translator.getMessage("label.private"));
    protected MenuItem miEmotePrivate = new MenuItem(Translator.getMessage("label.mesg"));
    protected MenuItem miEmote = new MenuItem(Translator.getMessage("label.emote"));
    protected MenuItem miStats = new MenuItem(Translator.getMessage("label.stats"));
    protected MenuItem miPing = new MenuItem(Translator.getMessage("label.ping"));
    protected MenuItem miWhois = new MenuItem(Translator.getMessage("label.whois"));

    protected boolean _showingBigButtons = true;

    protected static final int PING = 0;
    protected static final int PRIVATE = 1;
    protected static final int EMOTE = 2;
    protected static final int PRIVATE_EMOTE = 3;
    protected static final int WHOIS = 4;

    // Netscape for Mac doesn't like my pop up dialog windows....
    public static boolean s_useDialogs = false;

    public ChatPanel(IChatGUIFactory factory, String room, String title,
                     IChatClientInputReceiver inputReceiver) 
    {
        _title = title;
        _factory = factory;

        bgColor = (Color)factory.getAttribute("bgColor");
        if (bgColor == null) {
            bgColor = new Color(0x384CC7);
        }

        _room = room;
        _inputReceiver = inputReceiver;
        try {
            jbInit();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    public ChatPanel(IChatGUIFactory factory, String room,
                     IChatClientInputReceiver inputReceiver) 
    {
        this(factory, room, room, inputReceiver);
    }


    public String getName() {
        return _room;
    }

    public void requestFocus() {
        txtInput.requestFocus();
    }

    // receive messages from the ChatPanel
    public void inputEvent(String txt) {
        _inputReceiver.inputEvent(_room, txt);
    }

    public void displayPrivateMessage(String user, String message) {
        displayMessage(Translator.getMessage("private", user, message));
    }

    public void displayPrivateEmote(String user, String message) {
        displayMessage(Translator.getMessage("mesg", user, message));
    }

    public void displayMessage(String user, String message) {
        displayMessage(user + ": " + message);
    }

    public void displayMessage(String message) {
        txtMessages.appendText(message + "\n");
    }

    public void displayError(String error) {
//        _factory.playAudioClip("error.au");
        displayMessage(Translator.getMessage("error", error));
    }

    public void setUserList(String[] users) {
        synchronized (lstUsers) {
            lstUsers.clear();
            for (int i=0; i < users.length; i++) {
                lstUsers.addItem(users[i]);
            }
            forceUserListRepaint();
        }
    }

    // Netscape-on-Windows problem workaround
    void forceUserListRepaint() { 
        lstUsers.invalidate();
        lstUsers.getParent().validate();
/*        lstUsers.validate();
        lstUsers.getParent().repaint(); 
        lstUsers.repaint(); */
    }

    public String[] getUserList() {
        synchronized (lstUsers) {
            String[] list = new String[lstUsers.countItems()];
            for (int i=0; i < list.length; i++) {
                list[i] = lstUsers.getItem(i);
            }
            return list;
        }
    }

    public void userJoinedRoom(String user) {
        displayMessage(Translator.getMessage("joined", user));
        if (!listContains(user)) {
            lstUsers.addItem(user);
            forceUserListRepaint();
        }
    }

    private boolean listContains(String s) {
        int len = lstUsers.countItems();
        for (int i=0; i < len; i++) {
            if (s.equals(lstUsers.getItem(i))) {
                return true;
            }
        }
        return false;
    }

    public void userPartedRoom(String user, boolean signoff) {
        if (signoff) {
            displayMessage(Translator.getMessage("part2", user));
        } else {
            displayMessage(Translator.getMessage("part1", user));
        }
        int len = lstUsers.countItems();
        for (int i=0; i < len; i++) {
            if (user.equals(lstUsers.getItem(i))) {
                lstUsers.delItem(i);
                forceUserListRepaint();
                break;
            }
        }
    }

    protected Component createTextWidget() {
        txtMessages = new TextArea();
        txtMessages.setBackground(Color.white);
        txtMessages.setText("");
        txtMessages.setEditable(false);
        return txtMessages;
    }

    protected Component createTitleArea() {
        Label l = new Label(_title);
        l.setBackground(bgColor);
        l.setForeground(Color.white);
        l.setFont(new Font("Dialog", Font.BOLD, 14));
        return l;
    }

    private void jbInit() throws Exception {
        setLayout(gridbag);

        Component lblTitle = createTitleArea();
        if (lblTitle != null) {
            gridbag.setConstraints(lblTitle,
                                   Constraints.create(0, 0, 2, 1, 1.0, 0,
                                                      GridBagConstraints.WEST,
                                                      GridBagConstraints.BOTH,
                                                      new Insets(0, 0, 0, 0), 0, 0));
            add(lblTitle);
        }
            
        Component c = createButtonPanel();
        _buttonPanel = new Panel();
        _buttonPanel.setBackground(bgColor);
        GridBagLayout gb2 = new GridBagLayout();
        _buttonPanel.setLayout(gb2);
        gb2.setConstraints(c, Constraints.create(0, 0, 1, 1, 1.0, 1.0,
                                                GridBagConstraints.EAST,
                                                GridBagConstraints.NONE,
                                                new Insets(0, 0, 0, 0), 0, 0));
        _buttonPanel.add(c);
        gridbag.setConstraints(_buttonPanel,
                               Constraints.create(0, 1, 2, 1, 1.0, 0,
                                                  GridBagConstraints.EAST,
                                                  GridBagConstraints.HORIZONTAL,
                                                  new Insets(0, 0, 0, 0), 0, 0));
        add(_buttonPanel);

        Component txtWidget = createTextWidget();
        gridbag.setConstraints(txtWidget,
                               Constraints.create(0, 2, 1, 1, 1.0, 1.0,
                                                  GridBagConstraints.CENTER,
                                                  GridBagConstraints.BOTH,
                                                  new Insets(0, 0, 0, 0), 0, 0));
        add(txtWidget);

        lstUsers = new List();
        gridbag.setConstraints(lstUsers,
                               Constraints.create(1, 2, 1, 1, 0.0, 1.0,
                                                  GridBagConstraints.CENTER,
                                                  GridBagConstraints.VERTICAL,
                                                  new Insets(0, 0, 0, 0), 0, 0));
        add(lstUsers);

        txtInput = new TextField();
        txtInput.setBackground(Color.white);
        gridbag.setConstraints(txtInput,
                               Constraints.create(0, 3, 2, 1, 1.0, 0.0,
                                                  GridBagConstraints.WEST,
                                                  GridBagConstraints.HORIZONTAL,
                                                  new Insets(0, 0, 0, 0), 0, 4));
        add(txtInput);
    }

    protected Component createButton(String img, String name) {
        if (img != null) {
            try {
                Image i = _factory.getImage(img);
                if (i != null && i.getHeight(this) > 0 && i.getWidth(this) > 0) {
                    return new ImageButton(i, name);
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        Button b = new Button(name);
        b.setBackground(bgColor);
        b.setForeground(Color.yellow);
        return b;
    }

    protected GridBagConstraints createButtonConstraints(int number) {
           return Constraints.create(number, 0, 1, 1, 0.0, 0.0,
                                  GridBagConstraints.EAST,
                                  GridBagConstraints.NONE,
                                  new Insets(2, 2, 2, 2), 0, 0);
    }


    protected Component createButtonPanel() {
        Panel p = new Panel(); // panel to contain the button
        p.setBackground(bgColor);
        GridBagLayout gb2 = new GridBagLayout();
        p.setLayout(gb2);

        int i=0;

        btnPrivate = createButton("private.gif", Translator.getMessage("label.private"));
        gb2.setConstraints(btnPrivate, createButtonConstraints(i++));
        p.add(btnPrivate);

        btnEmote = createButton("emote.gif", Translator.getMessage("label.emote"));
        gb2.setConstraints(btnEmote, createButtonConstraints(i++));
        p.add(btnEmote);

/*        btnEmotePrivate = createButton("pemote.gif", Translator.getMessage("label.mesg"));
        gb2.setConstraints(btnEmotePrivate, createButtonConstraints(i++));
        p.add(btnEmotePrivate);
*/
        btnPing = createButton("ping.gif", Translator.getMessage("label.ping"));
        gb2.setConstraints(btnPing, createButtonConstraints(i++));
        p.add(btnPing);

        btnUserInfo = createButton("whois.gif", Translator.getMessage("label.whois"));
        gb2.setConstraints(btnUserInfo, createButtonConstraints(i++));
        p.add(btnUserInfo);

        btnStats = createButton("stats.gif", Translator.getMessage("label.stats"));
        gb2.setConstraints(btnStats, createButtonConstraints(i++));
        p.add(btnStats);

/*        btnClose = createButton("blank.gif", Translator.getMessage("label.close"));
        gb2.setConstraints(btnClose, createButtonConstraints(i++));
        p.add(btnClose);
*/
        btnHelp = createButton("help.gif", Translator.getMessage("label.help"));
        gb2.setConstraints(btnHelp, createButtonConstraints(i++));
        p.add(btnHelp);

        return p;
    }

    public boolean action(Event e, Object o) {
        if (e.id == Event.ACTION_EVENT) {
            if (e.target == lstUsers) {
                _inputReceiver.userDoubleClick((String)o);
                return true;
            }
            if (e.target == txtInput) {
                _inputReceiver.inputEvent(_room, (String)o);
                txtInput.setText("");
                return true;
            }
            if (e.target == btnStats || e.target == miStats) {
                _inputReceiver.inputEvent(_room, ICommands.STATS);
                return true;
            }
            if (e.target == btnClose) {
                _inputReceiver.inputEvent(_room, ICommands.PART_ROOM);
                return true;
            } 
            if (e.target == btnPing || e.target == miPing) {
                if (!s_useDialogs) {
                    String user = lstUsers.getSelectedItem();
                    if (user != null) {
                        _inputReceiver.inputEvent(_room, ICommands.PING + " " + user);
                    } else {
                        displayError(Translator.getMessage("error.ping"));
                    } 
                } else {
                    IQuery q = _factory.createQuery(Translator.getMessage("ping"),
                                                    Translator.getMessage("label.user") + ":",
                                                    getUserList(), false, null);
                    q.setCallbackParams(PING, this);
                    String def = lstUsers.getSelectedItem();
                    if (def != null) {
                        q.setDefaultSelection(def);
                    }
                    q.show();
                }
                return true;
            }
            if (e.target == btnUserInfo || e.target == miWhois) {
                if (!s_useDialogs) {
                    String user = lstUsers.getSelectedItem();
                    if (user != null) {
                        _inputReceiver.inputEvent(_room, ICommands.WHOIS + " " + user);
                    } else {
                        displayError(Translator.getMessage("error.whois"));
                    } 
                } else {
                    IQuery q = _factory.createQuery(Translator.getMessage("label.compose.whois"),
                                                    Translator.getMessage("label.user") + ":",
                                                    getUserList(), false, null);
                    q.setCallbackParams(WHOIS, this);
                    String def = lstUsers.getSelectedItem();
                    if (def != null) {
                        q.setDefaultSelection(def);
                    }
                    q.show();
                }
                return true;
            }
            if (e.target == btnPrivate || e.target == miPrivate) {
                if (!s_useDialogs) {
                    String user = lstUsers.getSelectedItem();
                    if (user != null) {
                        String msg = txtInput.getText();
                        if (msg != null && msg.length() > 0) {
                            _inputReceiver.inputEvent(_room, ICommands.SAY_TO_USER + " " + user + " " + msg);
                            txtInput.setText("");
                        } else {
                            displayError(Translator.getMessage("error.private2"));
                        } 
                    } else {
                        displayError(Translator.getMessage("error.private1"));
                    } 
                } else {
                    IQuery q = _factory.createQuery(Translator.getMessage("label.compose.private"),
                                                    Translator.getMessage("label.to") + ":", 
                                                    getUserList(), true, 
                                                    Translator.getMessage("label.private") + ":");
                    q.setCallbackParams(PRIVATE, this);
                    String def = lstUsers.getSelectedItem();
                    if (def != null) {
                        q.setDefaultSelection(def);
                    }
                    q.show();
                }

                return true;
            }
            if (e.target == btnEmotePrivate || e.target == miEmotePrivate) {
                if (!s_useDialogs) {
                    String user = lstUsers.getSelectedItem();
                    if (user != null) {
                        String msg = txtInput.getText();
                        if (msg != null && msg.length() > 0) {
                            _inputReceiver.inputEvent(_room, ICommands.EMOTE_TO_USER + " " + user + " " + msg);
                            txtInput.setText("");
                        } else {
                            displayError(Translator.getMessage("error.mesg2"));
                        } 
                    } else {
                        displayError(Translator.getMessage("error.mesg1"));
                    } 
                } else {
                    IQuery q = _factory.createQuery(Translator.getMessage("label.compose.mesg"),
                                                    Translator.getMessage("label.to" + ":"),
                                                    getUserList(), true, 
                                                    Translator.getMessage("label.emote") + ":");
                    q.setCallbackParams(PRIVATE_EMOTE, this);
                    String def = lstUsers.getSelectedItem();
                    if (def != null) {
                        q.setDefaultSelection(def);
                    }
                    q.show();
                }
                
                return true;
            }
            if (e.target == btnEmote || e.target == miEmote) {
                if (!s_useDialogs) {
                    String msg = txtInput.getText();
                    if (msg != null && msg.length() > 0) {
                        _inputReceiver.inputEvent(_room, ICommands.EMOTE_TO_ROOM + " " + msg);
                        txtInput.setText("");
                    } else {
                        displayError(Translator.getMessage("error.emote"));
                    } 
                } else {
                    IQuery q = _factory.createQuery(Translator.getMessage("label.compose.emote"),
                                                    null, null, true, 
                                                    Translator.getMessage("label.emote"));
                    q.setCallbackParams(EMOTE, this);
                    q.show();
                }
                return true;
            }
            if (e.target == btnHelp) {
                _inputReceiver.inputEvent(_room, ICommands.HELP);
            }
        }
        return super.action(e, o);
    }

    public void handleQuery(int id, IQuery q) {
        if (q.getCanceled()) {
            q.dispose();
            return;
        }

        String user;
        String msg;

        switch (id) {
        case PING:
            user = q.getSelection();
            _inputReceiver.inputEvent(_room, ICommands.PING + " " + user);
            break;
        case PRIVATE:
            user = q.getSelection();
            msg = q.getText();
            _inputReceiver.inputEvent(_room, ICommands.SAY_TO_USER + " " + user + " " + msg);
            txtInput.setText("");
            break;
        case PRIVATE_EMOTE:
            user = q.getSelection();
            msg = q.getText();
            _inputReceiver.inputEvent(_room, ICommands.EMOTE_TO_USER + " " + user + " " + msg);
            txtInput.setText("");
            break;
        case EMOTE:
            msg = q.getText();
            _inputReceiver.inputEvent(_room, ICommands.EMOTE_TO_ROOM + " " + msg);
            txtInput.setText("");
            break;
        case WHOIS:
            user = q.getSelection();
            _inputReceiver.inputEvent(_room, ICommands.WHOIS + " " + user);
            break;
        }
        q.dispose();
    }

    protected Menu createActionMenu() {
        Menu m = new Menu(Translator.getMessage("label.action"));
        m.add(miPrivate);
        m.add(miEmotePrivate);
        m.add(miEmote);
        m.add(miStats);
        m.add(miPing);
        m.add(miWhois);
        return m;
    }

    public void showBigButtons(boolean b) {
        if (b) {
            if (!_showingBigButtons) {
                add(_buttonPanel);
                gridbag.setConstraints(_buttonPanel,
                                       Constraints.create(0, 1, 2, 1, 1.0, 0,
                                                          GridBagConstraints.EAST,
                                                          GridBagConstraints.HORIZONTAL,
                                                          new Insets(0, 0, 0, 0), 0, 0));
                _buttonPanel.invalidate();
                validate();
            }
            _showingBigButtons = true;
        } else {
            if (_showingBigButtons) {
                remove(_buttonPanel);
                validate();
                _showingBigButtons = false;
            }
        }
    }
}

