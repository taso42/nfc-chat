/*
 * Copyright (c) 2000 Lyrisoft Solutions, Inc.
 * Used by permission
 */
package com.lyrisoft.chat.server.remote.command;

import com.lyrisoft.chat.ICommands;
import com.lyrisoft.chat.Translator;
import com.lyrisoft.chat.server.remote.AccessDenied;
import com.lyrisoft.chat.server.remote.AccessException;
import com.lyrisoft.chat.server.remote.ChatClient;
import com.lyrisoft.chat.server.remote.ChatServer;

public class SignOn extends CommandBase implements ICommands {
    public boolean process(ChatClient client, String[] args) {
        if (args.length < 2) {
            client.generalError(getUsage(args[0]));
            return false;
        } else {
            if (client.getUserId() != null) {
                client.generalError(Translator.getMessage("already_on"));
                return false;
            }
            client.setUserId(args[1]);
            try {
                if (args.length < 3) {
                    client.getServer().signOn(client, null);
                } else {
                    client.getServer().signOn(client, args[2]);
                }
                return true;
            }
            catch (AccessException e) {
                String userId = client.getUserId();
                client.setUserId(null);
                client.signOnError(e.getMessage(), userId);
            }
            catch (AccessDenied e) {
                client.setUserId(null);
                client.error(ACCESS_DENIED, e.getMessage());
            }
        }
        return false;
    }
    
    public void processDistributed(String client, String origin, String[] args, ChatServer server) {
        server.remoteSignOn(origin, client);
    }
}
