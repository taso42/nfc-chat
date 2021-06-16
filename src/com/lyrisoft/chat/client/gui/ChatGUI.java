/*
 * Copyright (c) 2000 Lyrisoft Solutions, Inc.
 * Used by permission
 */
package com.lyrisoft.chat.client.gui;

import java.util.Enumeration;
import java.util.Hashtable;

import com.lyrisoft.chat.server.local.CommandProcessorLocal;

/**
 * Controller of the GUI.  This is the controller of all the GUI pieces
 */
public class ChatGUI {
    private Hashtable _roomGUIs;
    private Hashtable _privateChats;
    private IMessageWindow _status;
    private IChatGUIFactory _guiFactory;
    private ILogin _login;
    private IConsole _console;

    public ChatGUI(IChatGUIFactory factory)
    {
        _roomGUIs = new Hashtable();
        _privateChats = new Hashtable();
        _guiFactory = factory;
    }

    public void showLogin() {
        _login = _guiFactory.createLoginDialog();
        _guiFactory.show(_login);
    }

    public void hideLogin() {
        if (_login != null) {
            _guiFactory.hide(_login);
            _login = null;
        }
    }

    public void reset() {
        for (Enumeration e = _roomGUIs.keys(); e.hasMoreElements(); ) {
            IChatRoom room = (IChatRoom)_roomGUIs.remove(e.nextElement());
            _guiFactory.hide(room);
        }
        for (Enumeration e = _privateChats.keys(); e.hasMoreElements(); ) {
            IPrivateChatRoom room = (IPrivateChatRoom)_privateChats.remove(e.nextElement());
            _guiFactory.hide(room);
        }

        if (_console != null) {
            _guiFactory.hide(_console);
        } 
        if ( _login != null) {
            _guiFactory.hide(_login);
            _login = null;
        } 
        
        _status = null;
        _login = null;
    }

    // for contructing keys into the hashtable
    static String roomKey(String room) {
        return room.toLowerCase();
    }

    public void setStatusGui(IMessageWindow window) {
        _status = window;
    }

    public IMessageWindow getStatusGui() {
        return _status;
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    public void ackSignon() {
//        _guiFactory.hide(_login);
        showConsole();
    }

    void showConsole() {
        _console = _guiFactory.createConsole();
        _guiFactory.show(_console);
        setStatusGui(_console);
    }

    public void ackRoomJoined(String room) {
        // spawn a room
        IChatRoom chatRoom = _guiFactory.createChatRoom(room);
        _roomGUIs.put(roomKey(room), chatRoom);
        _guiFactory.show(chatRoom);
    }

    public void ackRoomParted(String room) {
        // close the room
        IChatRoom chatRoom = (IChatRoom)_roomGUIs.remove(roomKey(room));
        if (chatRoom != null) {
            _guiFactory.hide(chatRoom);
        }
        if (_roomGUIs.size() == 0) {
            System.err.println("Showing console because there are no more room windows.");
            showConsole();
        }
    }

    public void messageFromUser(String user, String room, String msg) {
        // show the message in a particular room
        IChatRoom chatRoom = (IChatRoom)_roomGUIs.get(roomKey(room));
        if (chatRoom != null) {
            chatRoom.displayMessage(user, msg);
        }
    }

    public IPrivateChatRoom getPrivateRoom(String user) {
        IPrivateChatRoom pchat = null;
        synchronized (_privateChats) {
            pchat = (IPrivateChatRoom)_privateChats.get(user.toUpperCase());
            if (pchat == null) {
                pchat = _guiFactory.createPrivateChatRoom(user);
                _guiFactory.show(pchat);
                _guiFactory.playAudioClip("private.au");
                _privateChats.put(user.toUpperCase(), pchat);
            }
        }
        return pchat;
    }

    public void closePrivateChatRoom(String name) {
        IPrivateChatRoom pchat = null;
        synchronized (_privateChats) {
            pchat = (IPrivateChatRoom)_privateChats.remove(name.toUpperCase());
        }
        if (pchat != null) {
            _guiFactory.hide(pchat);
        } 
    }

    public void messageToUserPrivate(String me, String user, String msg) {
        IPrivateChatRoom pchat = getPrivateRoom(user);
        pchat.displayPrivateMessage(me, msg);
    }

    public void emoteToUserPrivate(String me, String user, String msg) {
        IPrivateChatRoom pchat = getPrivateRoom(user);
        pchat.displayPrivateEmote(me, msg);
    }

    public void messageFromUserPrivate(String user, String msg) {
        IPrivateChatRoom pchat = getPrivateRoom(user);
        pchat.displayPrivateMessage(user, msg);
    }

    public void emoteFromUserPrivate(String user, String msg) {
        IPrivateChatRoom pchat = getPrivateRoom(user);
        pchat.displayPrivateEmote(user, msg);
    }

    public void roomList(String[] roomList) {
        _console.clearRooms();
        for (int i=0; i < roomList.length; i++) {
            String[] s = CommandProcessorLocal.decompose(roomList[i]);
            _console.addRoom(s[0], s[1], false);
        }

    }

    public void globalUserList(String[] users) {
        // display the global user list
        _console.clearUsers();

        for (int i=0; i < users.length; i++) {
            _console.addUser(users[i], false);
        }
    }

    public void roomUserList(String room, String[] users) {
        // update a roomGUI with the list of users
        IChatRoom chatRoom = (IChatRoom)_roomGUIs.get(roomKey(room));
        if (chatRoom != null) {
            chatRoom.setUserList(users);
        }
    }

    public void userJoinedRoom(String user, String room) {
        // update a roomGUI with this user
        IChatRoom chatRoom = (IChatRoom)_roomGUIs.get(roomKey(room));
        if (chatRoom != null) {
            chatRoom.userJoinedRoom(user);
        }
    }

    public void userPartedRoom(String user, String room, boolean signOff) {
        // update a roomGUI with this user
        IChatRoom chatRoom = (IChatRoom)_roomGUIs.get(roomKey(room));
        if (chatRoom != null) {
            chatRoom.userPartedRoom(user, signOff);
        }
    }

    public void generalError(String message) {
        // display general error
        if (_status == null) {
            if (_login != null) {
                _login.setStatus(message);
            } else {
                System.err.println("ChatGUI.generalError():" + message);
            }
        } else {
            _status.displayError(message);
        }
    }

    public void generalMessage(String message) {
        if (_status == null) {
            if (_login != null) {
                _login.setStatus(message);
            } else {
                System.err.println("ChatGUI.generalMessage():" + message);
            }
        } else {
            _status.displayMessage(message);
        }
    }

    public void generalRoomMessage(String room, String message) {
        // display general message in a particular room
        IChatRoom chatRoom = (IChatRoom)_roomGUIs.get(roomKey(room));
        if (chatRoom != null) {
            chatRoom.displayMessage(message);
        }
    }

    public void userSignOn(String userId) {
        _console.addUser(userId, true);
    }

    public void userSignOff(String userId) {
        _console.removeUser(userId, true);
    }

    public void roomCreated(String room) {
        _console.addRoom(room, "1", true);
    }

    public void roomDestroyed(String room) {
        _console.removeRoom(room, true);
    }
}
