/*
 * Copyright (c) 2000 Lyrisoft Solutions, Inc.
 * Used by permission
 */
package com.lyrisoft.chat.server.local.command;

import com.lyrisoft.chat.ICommands;
import com.lyrisoft.chat.Translator;
import com.lyrisoft.chat.client.IChatClient;

public class AckKill implements ICommandProcessorLocal, ICommands {
    public void process(IChatClient client, String[] args) {
        if (args.length < 2) {
            client.generalError(Translator.getMessage("error.protocol", args[0]));
        } else {
            client.ackKill(args[1]);
        }
    }
}
