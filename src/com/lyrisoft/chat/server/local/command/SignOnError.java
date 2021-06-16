/*
 * Copyright (c) 2000 Lyrisoft Solutions, Inc.
 * Used by permission
 */
package com.lyrisoft.chat.server.local.command;

import com.lyrisoft.chat.ICommands;
import com.lyrisoft.chat.Translator;
import com.lyrisoft.chat.client.IChatClient;

public class SignOnError implements ICommandProcessorLocal, ICommands {
    public void process(IChatClient client, String[] args) {
        if (args.length < 2) {
            client.generalError(Translator.getMessage("error.protocol", args[0])); 
        } else {
            if (args[0].equals(ALREADY_SIGNED_ON)) {
                client.generalError(args[1] + " is already signed on");
            } else if (args[0].equals(INVALID_CHARACTER)) {
                client.generalError(Translator.getMessage("error.userid.chars"));
            } else {
                client.generalError(Translator.getMessage("error.unknown.signon"));
            }
        }
    }
}
