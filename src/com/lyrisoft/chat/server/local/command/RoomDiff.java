/*
 * Copyright (c) 2000 Lyrisoft Solutions, Inc.
 * Used by permission
 */
package com.lyrisoft.chat.server.local.command;

import com.lyrisoft.chat.Translator;
import com.lyrisoft.chat.client.IChatClient;

public class RoomDiff implements ICommandProcessorLocal {
    public void process(IChatClient client, String[] args) {
        if (args.length < 3) {
            client.generalError(Translator.getMessage("error.protocol", args[0]));
        } else {
            char op = args[1].charAt(0);
            switch (op) {
            case '+':
                client.roomCreated(args[2]);
                break;
            case '-':
                client.roomDestroyed(args[2]);
                break;
            default:
                client.generalError(Translator.getMessage("error.protocol", args[1]));
            }
        }
    }
}
