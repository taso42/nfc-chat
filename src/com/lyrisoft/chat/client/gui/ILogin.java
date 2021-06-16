/*
 * Copyright (c) 2000 Lyrisoft Solutions, Inc.
 * Used by permission
 */
package com.lyrisoft.chat.client.gui;

/**
 * This is the login widget.  It should have text entry fields for userId and password, and
 * a status field.
 */
public interface ILogin {
    /**
     * Set the text of the status field
     */
    public void setStatus(String txt);

    /**
     * Make this thing visible on the screen.  Note: It's safer to user IChatGUIFactory.show()
     * instead.
     * @see IChatGUIFactory#show(ILogin)
     */
    public void show();

    /**
     * Make this thing invisble on the screen.  Note: It's safer to user IChatGUIFactory.hide()
     * instead.
     * @see IChatGUIFactory#hide(ILogin)
     */
    public void hide();
}
