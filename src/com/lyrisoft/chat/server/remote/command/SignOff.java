/*
 * Copyright (c) 2000 Lyrisoft Solutions, Inc.
 * Used by permission
 */
package com.lyrisoft.chat.server.remote.command;

import com.lyrisoft.chat.Translator;
import com.lyrisoft.chat.server.remote.ChatClient;
import com.lyrisoft.chat.server.remote.ChatServer;

public class SignOff extends CommandBase {
    public boolean process(ChatClient client, String[] args) {
        client.generalMessage(Translator.getMessage("bye"));
        client.getServer().signOff(client);
        return false; // the server's signoff method will distribute for us..
    }
    
    public void processDistributed(String client, String origin, String[] args, ChatServer server) {
        server.remoteSignOff(origin, client);
    }
}
