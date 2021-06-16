/*
 * Copyright (c) 2000 Lyrisoft Solutions, Inc.
 * Used by permission
 */
package com.lyrisoft.chat.server.local.command;

import com.lyrisoft.chat.Translator;
import com.lyrisoft.chat.client.IChatClient;

public class UsersInRoom implements ICommandProcessorLocal {
    public void process(IChatClient client, String[] args) {
        if (args.length < 3) {
            client.generalError(Translator.getMessage("error.protocol", args[0])); 
        } else {
            String room = args[1];
            int len = args.length - 2;
            String[] users = new String[len];
            System.arraycopy(args, 2, users, 0, len);
            client.roomUserList(room, users);
        }
    }
}
