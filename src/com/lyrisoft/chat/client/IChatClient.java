/*
 * Copyright (c) 2000 Lyrisoft Solutions, Inc.
 * Used by permission
 */
package com.lyrisoft.chat.client;

import com.lyrisoft.chat.server.local.IChatServer;

/**
 * The view of the client. This is a sort of call-back interface that the server uses to talk to
 * the client.
 *
 * There is an implementation of this interface on both the server and the client side.  On the
 * server side, the implementation's job is to create some kind of message and pass it over the
 * wire.  On the client side, the implementation's job is to react somehow (i.e., display a
 * message in a window, or whatever..)
 */
public interface IChatClient {
    /**
     * The server has authenticated us and acknowledged our sign on
     */
    public void ackSignon(String myname);

    /**
     * The connection to the server was closed
     */
    public void connectionLost();

    /**
     * The server has acknowledged our room join
     */
    public void ackJoinRoom(String room);

    /**
     * The server has acknowledged our room part
     */
    public void ackPartRoom(String room);

    /**
     * The server is indicating that someone is saying something in a room
     */
    public void messageFromUser(String user, String room, String msg);

    /**
     * The server is indicating that someone is saying something to us, privately
     */
    public void messageFromUserPrivate(String user, String msg);

    /**
     * The server is indicating that someone is privately emoting something to us.
     * (Note, there is no "emoteFromUserPublic";  A public emote comes in as a
     * generalRoomMessage
     *
     * @see #generalRoomMessage
     */
    public void emoteFromUserPrivate(String user, String msg);

    /**
     * The server is sending us a list of rooms
     */
    public void roomList(String[] roomList);

    /**
     * The server is sending us the global user list
     */
    public void globalUserList(String[] users);

    /**
     * The server is sending us the user list for a particular room
     */
    public void roomUserList(String room, String[] users);

    /**
     * The server is indicating that someone just joined a room we're in.
     */
    public void userJoinedRoom(String user, String room);

    /**
     * The server is indicating that someone just parted a room we're in.
     */
    public void userPartedRoom(String user, String room, boolean signOff);

    /**
     * The server is giving us an error message to display
     */
    public void generalError(String message);

    /**
     * The server is giving us a general message to display.  (This could be a MOTD or the
     * answer to some general request like the /stats command)
     */
    public void generalMessage(String message);

    /**
     * The server is giving us a message to display in the context of a particular room.  This
     * is commonly an emote message.
     */
    public void generalRoomMessage(String room, String message);

    /**
     * The server is indicating that somebody ping'ed us.  It expects to get a pong message back
     */
    public void ping(String user, String arg);

    /**
     * The server is giving us the reply to a ping that we already sent out.
     */
    public void pong(String user, String arg);

    public void killed(String killer, String msg);
    public void ackKill(String victim);

    public void emote(String from, String room, String message);

    public void userSignOn(String userId);
    public void userSignOff(String userId);

    public void roomCreated(String room);
    public void roomDestroyed(String room);

    public void setAttribute(String name, Object value);

    public Object getAttribute(String name);

    // added 2000-08-18..  I consider some or all of the following 
    // methods to be "pollution".  Hopefully I'll find an elegant way 
    // to remove these from the interface.
    public void init();
    public void setApplet(java.applet.Applet a);
    public void setInitialRoom(String room);
    public void showLogin();
    public IChatServer getServerInterface();
}
