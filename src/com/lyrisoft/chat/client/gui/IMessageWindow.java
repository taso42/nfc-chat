/*
 * Copyright (c) 2000 Lyrisoft Solutions, Inc.
 * Used by permission
 */
package com.lyrisoft.chat.client.gui;

public interface IMessageWindow {
    public void displayPrivateMessage(String user, String message);
    public void displayPrivateEmote(String user, String message);
    public void displayMessage(String user, String message);
    public void displayMessage(String message);
    public void displayError(String error);
}
