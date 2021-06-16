/*
 * Copyright (c) 2000 Lyrisoft Solutions, Inc.
 * Used by permission
 */
package com.lyrisoft.chat.server.local.command;

import com.lyrisoft.chat.Translator;
import com.lyrisoft.chat.client.IChatClient;

public class UnknownCommand implements ICommandProcessorLocal {
    public void process(IChatClient client, String[] args) {
        if (args.length > 0) {
            client.generalError(Translator.getMessage("error.huh", "(" + args[0] + ")"));
        } else {
            client.generalError(Translator.getMessage("error.huh", ""));
        }
    }
}
