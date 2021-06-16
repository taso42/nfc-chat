/*
 * Copyright (c) 2000 Lyrisoft Solutions, Inc.
 * Used by permission
 */
package com.lyrisoft.chat.client.command;

import com.lyrisoft.chat.Translator;
import com.lyrisoft.chat.client.Client;

/**
 * a "private message"
 */
public class SayToUser extends UserCommandProcessor {
    /**
     * @see com.lyrisoft.chat.server.local.ChatServerLocal#sayToUser
     */
    public void process(String input, String arg, Client client) throws NotEnoughArgumentsException {
        String[] args = null;
        try {
            args = decompose(input, 3);
        }
        catch (NotEnoughArgumentsException e) {
            args = decompose(input, 2);
        }
        if (args[1].equalsIgnoreCase(client.getMyName())) {
            client.generalMessage(Translator.getMessage("msg.self"));
        } else {
            if (args.length < 3) {
                // message to user with no content.  so, just open the 
                // private window
                client.getGUI().getPrivateRoom(args[1]);
                // yes, getting the private room has the side affect of
                // opening a new window
                
            } else {
                // otherwise, go ahead and send the private message
                client.getServerInterface().sayToUser(args[1], args[2]);
                client.messageToUserPrivate(args[1], args[2]);
            }
        }
    }
}
