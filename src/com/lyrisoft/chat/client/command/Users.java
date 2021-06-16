/*
 * Copyright (c) 2000 Lyrisoft Solutions, Inc.
 * Used by permission
 */
package com.lyrisoft.chat.client.command;

import com.lyrisoft.chat.client.Client;

/**
 * Handle a user list request message
 */
public class Users extends UserCommandProcessor {
    /**
     * @see com.lyrisoft.chat.server.local.ChatServerLocal#requestUserList
     */
    public void process(String input, String arg, Client client) {
        client.getServerInterface().requestUserList();
    }
}
