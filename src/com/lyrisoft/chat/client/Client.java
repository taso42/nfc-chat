/*
 * Copyright (c) 2000 Lyrisoft Solutions, Inc.
 * Used by permission
 */
package com.lyrisoft.chat.client;

import java.applet.Applet;
import java.net.MalformedURLException;
import java.net.URL;

import com.lyrisoft.chat.Constants;
import com.lyrisoft.chat.ICommands;
import com.lyrisoft.chat.Translator;
import com.lyrisoft.chat.client.gui.ChatGUI;
import com.lyrisoft.chat.client.gui.IChatClientInputReceiver;
import com.lyrisoft.chat.client.gui.IChatGUIFactory;
import com.lyrisoft.chat.server.local.ChatServerLocal;
import com.lyrisoft.chat.server.local.IChatServer;


/**
 * The heart of the NFC client program.  Notice that this is just a subclass of 
 * DumbClient, with a whole lot of functionality added in
 */
public class Client 
    extends DumbClient 
    implements IChatClientInputReceiver, IChatClient, Constants, ICommands 
{
    protected IChatServer _serverInterface;
    protected ChatGUI _gui;
    protected String _initialRoom;
    protected Applet _applet;
    protected String _myName;
    protected UserCommands _userCommands = null;
    protected IChatGUIFactory _factory;

    /**
     * Construct a Client that will connect to the given host and port
     */
    public Client(String host, int port) {
        _serverInterface = new ChatServerLocal(host, port, this);
    }

    /**
     * Construct a Client that will connect to the given host and port and fallback
     * to tunneling mode using the readUrl and writeUrl.
     */
    public Client(String host, int port, String readUrl, String writeUrl) {
        _serverInterface = new ChatServerLocal(host, port, readUrl, writeUrl, this);
    }

    /**
     * Construct a Client that will connect to the server in tunnel mode only.
     */
    public Client(String readUrl, String writeUrl) {
        _serverInterface = new ChatServerLocal(readUrl, writeUrl, this);
    }

    /**
     * Exists so subclasses can completely override the construction.
     */
    protected Client() {
    }

    /**
     * get the ChatGUI object
     */
    public ChatGUI getGUI() {
        return _gui;
    }

    /**
     * Call after all attributes have been set
     */
    public void init() {
        _factory = (IChatGUIFactory)getAttribute("guiFactory");
        if ( _factory == null) {
            throw new RuntimeException("Client: no GUI Factory");
        } 
        
        _gui = new ChatGUI(_factory);
        _factory.setInputReceiver(this);
        _factory.setChatServer(_serverInterface);
        _factory.setMainGui(_gui);
        
        _userCommands = (UserCommands)getAttribute("userCommands");
        if ( _userCommands == null) {
            throw new RuntimeException("Client: no UserCommands object");
        } 

        _serverInterface.init();
    }

    public String getMyName() {
        return _myName;
    }

    public void messageToUserPrivate(String rcpt, String msg) {
        _gui.messageToUserPrivate(_myName, rcpt, msg);
    }

    public void emoteToUserPrivate(String rcpt, String msg) {
        _gui.emoteToUserPrivate(_myName, rcpt, msg);
    }


    public void setInitialRoom(String room) {
        _initialRoom = room;
    }

    public boolean runningAsApplet() {
        return _applet != null;
    }

    /**
     * If the client is running as an Applet, the Applet should call this method after
     * instantiating
     */
    public void setApplet(Applet a) {
        _applet = a;
    }

    /**
     * Get's the 'local view' of the server
     */
    public IChatServer getServerInterface() {
        return _serverInterface;
    }

    /**
     * Display the given link in a new browser window (if we're running as an applet)
     * @param link a URL to display
     */
    public void showLink(String link) {
        if (_applet != null) {
            try {
                _applet.getAppletContext().showDocument(new URL(link), "_nfcLinks");
            }
            catch (MalformedURLException e) {
                System.err.println(e);
            }
        } else {
            System.err.println("href=[" + link + "]");
        }
    }

    /**
     * From the IChatClientInputReceiver interface...<p>
     *
     * Delegated to UserCommands.process()
     *
     * @param room the room the message was typed in
     * @param txt the text that the user typed.  if this is null, the method immediately returns
     *
     * @see UserCommands#process
     */
    public void inputEvent(String room, String txt) {
        if (txt == null || txt.length() == 0) {
            return;
        }

        try {
            _userCommands.process(txt, room, this);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * from the IChatClientInputReceiver interface...<p>
     *
     * Delegated to ChatServerLocal.signOn()
     *
     * @see com.lyrisoft.chat.server.local.ChatServerLocal#signOn
     */
    public void loginEvent(String username, String password) {
        _serverInterface.signOn(username, password);
    }

    public void loginCancelEvent() {
        _gui.hideLogin();
        reset();
    }

    // The following methods are all from the IChatClient interface
    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    public void ackSignon(String myName) {
        _gui.hideLogin();
        _gui.generalMessage(Translator.getMessage("pleasewait"));
        _myName = myName;
        _gui.ackSignon();


        if (_initialRoom != null) {
            _serverInterface.joinRoom(_initialRoom, null);
        }
        
        
        _serverInterface.reportVersion(getVersion());
        _serverInterface.requestRoomList();
        _serverInterface.requestUserList();
    }

    public String getVersion() {
        return Translator.getMessage("client.version",
                                     System.getProperty("java.vendor"),
                                     System.getProperty("java.version"),
                                     System.getProperty("os.name"));
    }

    /**
     * The connection is lost.
     * <p>
     * If we're running as an Applet, then show the login dialog.
     * Otherwise, print a message and exit.
     */
    public void connectionLost() {
        _gui.generalMessage(Translator.getMessage("connection.closed"));
        reset();
    }

    public void showLogin() {
        _gui.showLogin();
    }

    public void reset() {
        if (runningAsApplet()) {
            _gui.reset();
            _gui.showLogin();
        } else {
            System.exit(1);
        } 
    }

    public void ackJoinRoom(String room) {
        _gui.ackRoomJoined(room);

        // ok, now that we're in here, let's get a user list
        _serverInterface.requestUsersInRoomList(room);
    }

    public void ackPartRoom(String room) {
        _gui.ackRoomParted(room);
    }

    public void messageFromUser(String user, String room, String msg) {
        _gui.messageFromUser(user, room, msg);
    }

    public void messageFromUserPrivate(String user, String msg) {
        _gui.messageFromUserPrivate(user, msg);
    }

    public void emoteFromUserPrivate(String user, String msg) {
        _gui.emoteFromUserPrivate(user, msg);
    }

    public void roomList(String[] roomList) {
        _gui.roomList(roomList);
    }

    public void globalUserList(String[] users) {
        _gui.globalUserList(users);
    }

    public void roomUserList(String room, String[] users) {
        _gui.roomUserList(room, users);
    }

    public void userJoinedRoom(String user, String room) {
        _gui.userJoinedRoom(user, room);
    }

    public void userPartedRoom(String user, String room, boolean signOff) {
        _gui.userPartedRoom(user, room, signOff);
    }

    public void generalError(String message) {
        _factory.playAudioClip("error.au");
        _gui.generalError(message);
    }

    public void generalMessage(String message) {
        _gui.generalMessage(message);
    }

    public void generalRoomMessage(String room, String message) {
        _gui.generalRoomMessage(room, message);
    }

    public void ping(String user, String arg) {
        _serverInterface.sendPong(user, arg);
        if (!SERVER_NAME.equals(user)) {
            _gui.generalMessage(Translator.getMessage("ping.from", user));
        }
    }

    public void pong(String user, String arg) {
        long delta = System.currentTimeMillis() - Long.valueOf(arg).longValue();
        String s = Translator.getMessage("ping.reply", user, 
                                         String.valueOf(delta),
                                         "ms");
        _gui.generalMessage(s);
    }

    public void killed(String killer, String msg) {
        generalMessage(Translator.getMessage("killed.by",
                                             killer,
                                             msg));
    }

    public void ackKill(String victim) {
        generalMessage(Translator.getMessage("killed", victim));
    }

    public void emote(String from, String room, String message) {
        generalRoomMessage(room, from + " " + message);
    }

    public void userSignOn(String userId) {
        _gui.userSignOn(userId);
    }

    public void userSignOff(String userId) {
        _gui.userSignOff(userId);
    }

    public void roomCreated(String room) {
        _gui.roomCreated(room);
    }

    public void roomDestroyed(String room) {
        _gui.roomDestroyed(room);
    }

    public void userDoubleClick(String user) {
        // we fake a /msg command, just as the user would have typed it
        // and let the command processor deal with it
        inputEvent(null, ICommands.SAY_TO_USER + " " + user);
    }
}
