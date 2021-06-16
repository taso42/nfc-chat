/*
 * Copyright (c) 2000 Lyrisoft Solutions, Inc.
 * Used by permission
 */
package com.lyrisoft.chat.server.local.command;

import com.lyrisoft.chat.Translator;
import com.lyrisoft.chat.client.IChatClient;

public class RoomUserDiff implements ICommandProcessorLocal {
    public void process(IChatClient client, String[] args) {
        if (args.length < 4) {
            client.generalError(Translator.getMessage("error.protocol", args[0]));
        } else {
            String room = args[1];
            char op = args[2].charAt(0);
            String user = args[3];
            switch (op) {
            case '+':
                client.userJoinedRoom(user, room);
                break;
            case '-':
                boolean signOff = false;
                if (args.length == 5) {
                    signOff = (new Boolean(args[5])).booleanValue();
                }
                client.userPartedRoom(user, room, signOff);
                break;
            default:
                client.generalError(Translator.getMessage("error.protocol", args[1]));
            }
        }
    }
}
