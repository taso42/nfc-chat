/*
 * Copyright (c) 2000 Lyrisoft Solutions, Inc.
 * Used by permission
 */
package com.lyrisoft.chat.client.gui;

/**
 * An interface that objects implement when they want to receive input
 * from some part of the GUI (like when the user types something)
 */
public interface IChatClientInputReceiver {
    /**
     * Called by the GUI when the user types something.
     * @param room the name of the room the message was typed in
     * @param txt what the user typed
     */
    public void inputEvent(String room, String txt);

    /**
     * Called by the login component, when the user has entered a username and password
     */
    public void loginEvent(String username, String password);

    public void loginCancelEvent();

    /**
     * Callback when a user is double-clicked
     */
    public void userDoubleClick(String user);
}
