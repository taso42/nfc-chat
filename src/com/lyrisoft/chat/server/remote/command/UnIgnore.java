/*
 * Copyright (c) 2000 Lyrisoft Solutions, Inc.
 * Used by permission
 * 
 * Tasso Mulzer (tasso@cs.tu-berlin.de)
 *
 * Updated by Maarten van Hoof (maarten@singularit.com):
 * - fixed a bug that caused a NullPointer if no arguments were given
 * - added code to notify the unignored user
 */
package com.lyrisoft.chat.server.remote.command;

import com.lyrisoft.chat.Translator;
import com.lyrisoft.chat.server.remote.ChatClient;
import com.lyrisoft.chat.server.remote.ChatServer;

/**
 * Handles an unignore command from a client.
 */
public class UnIgnore extends CommandBase {

    /**
     * Handles an unignore command from a client on the server that the client is logged in to.
     * @client the ChatClient object representing the client
     * @param args {ICommands.UNIGNORE, unignoreduser}
     */
    public boolean process(ChatClient client, String[] args) {
        if (args.length < 2) {
            client.generalError(getUsage(args[0]));
            return false;
        } 
        if (client.unignore(args[1])) {
			client.generalMessage(Translator.getMessage("not_ignoring", args[1]));
			ChatClient victim = client.getServer().getClient(args[1]);
			if(victim != null && !victim.isIgnoring(client.getUserId())) {
				victim.generalMessage(Translator.getMessage("not_ignored", client.getUserId()));
			}
			return (victim == null);
        } else {
        	return false;
        }
    }

    /**
     * Handles an unignore command from a client on a server that the client is not logged in to.
     * @param client the userid of the client that sent the command
     * @param origin the name of the server the command came in on
     * @param args {ICommands.UNIGNORE, unignoreduser}
     * @param server the ChatServer processing the command
     */
    public void processDistributed(String client, String origin, String[] args, ChatServer server) { 
        ChatClient victim = server.getClient(args[1]);
        if(victim != null)
          victim.generalMessage(Translator.getMessage("not_ignored", client));
    }
}
