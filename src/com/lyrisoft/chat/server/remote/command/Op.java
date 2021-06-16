/*
 * Copyright (c) 2000 Lyrisoft Solutions, Inc.
 * Used by permission
 */
package com.lyrisoft.chat.server.remote.command;

import com.lyrisoft.chat.Translator;
import com.lyrisoft.chat.server.remote.ChatClient;
import com.lyrisoft.chat.server.remote.ChatServer;
import com.lyrisoft.chat.server.remote.RoomServer;

public class Op extends CommandBase {
    public boolean process(ChatClient client, String[] args) {
        if (args.length < 4) {
            client.generalError(getUsage(args[0]));
        } else {
            String room = args[1];
            String operation = args[2];
            String user = args[3];
            RoomServer roomServer = client.getServer().getRoom(room);
            if (room == null) {
                if (client.getServer().roomExists(room)) {
                    return true;
                } else {
                    client.generalError(Translator.getMessage("nosuchroom", room));
                }
            }
            
            ChatClient newOp = client.getServer().getClient(user);
            if (newOp == null) {
                return true;
            } else {
                if (operation.equals("+")) {
                    roomServer.op(client, newOp);
                } else {
                    roomServer.deop(client, newOp);
                }
            }
        } 
        return false;
    }

    public void processDistributed(String client, String origin, String[] args, ChatServer server) {
        // todo
    }
}
