/*
 * Copyright (c) 2000 Lyrisoft Solutions, Inc.
 * Used by permission
 */
package com.lyrisoft.chat.client.command;

import com.lyrisoft.chat.client.Client;

/**
 * Handle a stats request
 */
public class Stats extends UserCommandProcessor {
    /**
     * @see com.lyrisoft.chat.server.local.ChatServerLocal#requestStats
     */
    public void process(String input, String arg, Client client) {
        client.getServerInterface().requestStats();
    }
}
