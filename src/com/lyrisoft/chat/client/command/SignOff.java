/*
 * Copyright (c) 2000 Lyrisoft Solutions, Inc.
 * Used by permission
 */
package com.lyrisoft.chat.client.command;

import com.lyrisoft.chat.client.Client;

/**
 * Handle a signoff message
 */
public class SignOff extends UserCommandProcessor {
    /**
     * @see com.lyrisoft.chat.server.local.ChatServerLocal#signOff
     */
    public void process(String input, String arg, Client client) {
        client.getServerInterface().signOff();
    }
}
