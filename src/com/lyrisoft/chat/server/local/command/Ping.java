/*
 * Copyright (c) 2000 Lyrisoft Solutions, Inc.
 * Used by permission
 */
package com.lyrisoft.chat.server.local.command;

import com.lyrisoft.chat.Translator;
import com.lyrisoft.chat.client.IChatClient;

// Ping.
public class Ping implements ICommandProcessorLocal {
    public void process(IChatClient client, String[] args) {
        if (args.length < 3) {
            client.generalError(Translator.getMessage("error.protocol", args[0]));
        } else {
            client.ping(args[1], args[2]);
        }
    }
}
