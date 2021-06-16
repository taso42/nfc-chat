/*
 * Copyright (c) 2000 Lyrisoft Solutions, Inc.
 * Used by permission
 */
package com.lyrisoft.chat.server.remote.command;

import com.lyrisoft.chat.Translator;
import com.lyrisoft.chat.server.remote.ChatClient;
import com.lyrisoft.chat.server.remote.ChatServer;

public class Kill extends CommandBase {
    public boolean process(ChatClient client, String[] args) {
        if (args.length < 2) {
            client.generalError(getUsage(args[0]));
        } else if (args.length < 3) {
            client.getServer().kill(args[1], client, Translator.getMessage("default.msg.kill"));
        } else {
            client.getServer().kill(args[1], client, args[2]);
        }
        return false;
    }

    public void processDistributed(String client, String origin, String[] args, ChatServer server) {
    }
}
