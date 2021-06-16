/*
 * Copyright (c) 2000 Lyrisoft Solutions, Inc.
 * Used by permission
 */
package com.lyrisoft.chat.server.local;

/**
 * The client-side view of the ChatServer.
 *
 * These calls are asynchronous.  They all return void.  Responses come back directly against the
 * IChatClient interface
 *
 * @see ChatServerLocal
 * @see CommandProcessorLocal
 */
public interface IChatServer {
    public void init();

    public void signOn(String userId, String password);
    public void signOff();

    public void help(String command);

    public void requestRoomList();
    public void requestUserList();

    public void requestUsersInRoomList(String room);

    public void joinRoom(String room, String passwordjoin);
    public void partRoom(String room);

    public void sayToRoom(String room, String message);
    public void sayToUser(String userId, String message);
    public void emoteToRoom(String room, String message);
    public void emoteToUser(String userId, String message);

    public void sendPing(String user, String arg);
    public void sendPong(String user, String arg);

    public void requestStats();
    public void requestUserInfo(String user);

    public void ignore(String userId, String message);
    public void unignore(String userId);

    public void kill(String userId, String message);
    public void reportVersion(String version);

    public void op(String userId, String room);
    public void deop(String userId, String room);

    public void kick(String userId, String room);

    public boolean isConnected();
}
