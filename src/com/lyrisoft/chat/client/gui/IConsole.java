/*
 * Copyright (c) 2000 Lyrisoft Solutions, Inc.
 * Used by permission
 */
package com.lyrisoft.chat.client.gui;

/**
 * This is the main console.  It shows the list of rooms and list of users on the server.
 */
public interface IConsole extends IMessageWindow {
    /**
     * Add a room to the room list
     */
    public void addRoom(String room, String userCount, boolean inform);

    /**
     * Add a user to the user list
     */
    public void addUser(String user, boolean inform);

    public void removeRoom(String room, boolean inform);
    
    public void removeUser(String user, boolean inform);

    /**
     * Clear out the list of rooms
     */
    public void clearRooms();

    /**
     * Clear out the list of users
     */
    public void clearUsers();

    /**
     * Make this thing visible on the screen.  Note: It's safer to user IChatGUIFactory.show()
     * instead.
     * @see IChatGUIFactory#show(IConsole)
     */
    public void show();

    /**
     * Make this thing invisible on the screen.  Note: It's safer to user IChatGUIFactory.hide()
     * instead.
     * @see IChatGUIFactory#hide(IConsole)
     */
    public void hide();
}
