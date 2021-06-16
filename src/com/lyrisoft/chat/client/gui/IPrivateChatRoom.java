/*
 * Copyright (c) 2000 Lyrisoft Solutions, Inc.
 * Used by permission
 */
package com.lyrisoft.chat.client.gui;

public interface IPrivateChatRoom {
    public void displayPrivateMessage(String user, String message);
    public void displayPrivateEmote(String user, String message);
}
