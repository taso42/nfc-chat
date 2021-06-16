/*
 * Copyright (c) 2000 Lyrisoft Solutions, Inc.
 * Used by permission
 */
package com.lyrisoft.chat.server.remote.command;

import java.util.Iterator;
import java.util.Map;

import com.lyrisoft.chat.Translator;
import com.lyrisoft.chat.server.remote.ChatClient;
import com.lyrisoft.chat.server.remote.ChatServer;
import com.lyrisoft.chat.server.remote.CommandProcessorRemote;

public class Help extends CommandBase {
    public boolean process(ChatClient client, String[] args) {
        Map m = CommandProcessorRemote.getCommandProcessors();
        if (args.length < 2) {
            client.generalMessage(Translator.getMessage("help1"));
            client.generalMessage(Translator.getMessage("help2"));
            client.generalMessage(Translator.getMessage("help3"));
            for (Iterator i = m.keySet().iterator(); i.hasNext(); ) {
                String name = (String)i.next();
                ICommandProcessorRemote cp = (ICommandProcessorRemote)m.get(name);
                if (client.getAccessLevel() >= cp.accessRequired()) {
                    String help = cp.getHelp();
                    if (help != null) {
                        client.generalMessage("  " + name + ": " + cp.getHelp());
                    }
                }
            }
        } else {
            ICommandProcessorRemote cp = (ICommandProcessorRemote)m.get("/" + args[1]);
            if (cp == null || cp.getHelp() == null) {
                client.generalError(Translator.getMessage("nosuchcommand", args[1]));
            } else {
                client.generalMessage(args[1] + ": " + cp.getHelp());
                client.generalMessage(cp.getUsage("/" + args[1]));
            }
        }
        return false;
    }

    public void processDistributed(String client, String origin, String[] args, ChatServer server) {
    }
}
