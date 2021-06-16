/*
 * Copyright (c) 2000 Lyrisoft Solutions, Inc.
 * Used by permission
 */
package com.lyrisoft.chat.client.gui;


/**
 * Interface for a ChatRoom GUI component.
 * This thing usually has:
 * <ul>
 * <li>A text area widget where the banter in the room is displayed
 * <li>A list widget that shows the users in this room
 * <li>An input field where the user can type his messages/commands
 * </ul>
 */
public interface IChatRoom extends IMessageWindow {
    /**
     * Set the users in the user list widget
     */
    public void setUserList(String[] users);

    /**
     * Indicates that a user just joined this room.  The user list widget show be updated,
     * and a message should be printed to the screen
     */
    public void userJoinedRoom(String user);

    /**
     * Indicates that a user just parted this room.  The user list widget show be updated,
     * and a message should be printed to the screen
     */
    public void userPartedRoom(String user, boolean signoff);

    /**
     * Get the name of this chat room
     */
    public String getName();

    /**
     * Make this thing visible on the screen.  Note: It's safer to use IChatGUIFactory.show()
     * instead.
     * @see IChatGUIFactory#show(IChatRoom room)
     */
    public void show();

    /**
     * Make this thing invisible on the screen.  Note: It's safer to use IChatGUIFactory.show()
     * instead.
     * @see IChatGUIFactory#hide(IChatRoom room)
     */
    public void hide();
}



