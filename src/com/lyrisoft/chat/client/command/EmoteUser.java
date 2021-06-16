/*
 * Copyright (c) 2000 Lyrisoft Solutions, Inc.
 * Used by permission
 */
package com.lyrisoft.chat.client.command;

import com.lyrisoft.chat.Translator;
import com.lyrisoft.chat.client.Client;

/**
 * Handle a private emote message
 */
public class EmoteUser extends UserCommandProcessor {
    /**
     * @see com.lyrisoft.chat.server.local.ChatServerLocal#emoteToUser
     */
    public void process(String input, String arg, Client client) throws NotEnoughArgumentsException {
        try {
            String[] args = decompose(input, 3);
            if (args[1].equalsIgnoreCase(client.getMyName())) {
                client.generalMessage(Translator.getMessage("msg.self"));
            } else {
                client.getServerInterface().emoteToUser(args[1], args[2]);
                client.emoteToUserPrivate(args[1], args[2]);
            }
        }
        catch (NotEnoughArgumentsException e) {
            client.generalError(e.getMessage());
        }
    }
}
