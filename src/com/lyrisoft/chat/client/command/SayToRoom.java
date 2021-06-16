/*
 * Copyright (c) 2000 Lyrisoft Solutions, Inc.
 * Used by permission
 */
package com.lyrisoft.chat.client.command;

import com.lyrisoft.chat.client.Client;

/**
 * Say something to everyone in the room.
 * This happens to be the default UserCommandProcessor.
 */
public class SayToRoom extends UserCommandProcessor {
    /**
     * Invokes sayToRoom() on the local view of the ChatServer.
     * @see com.lyrisoft.chat.server.local.ChatServerLocal#sayToRoom
     */
    public void process(String input, String arg, Client client) {
        if (arg != null) {
            client.getServerInterface().sayToRoom(arg, input);
        } else {
            (new Throwable()).printStackTrace();
            System.err.println("FIXME: what should I do here?");
        }
    }
}
