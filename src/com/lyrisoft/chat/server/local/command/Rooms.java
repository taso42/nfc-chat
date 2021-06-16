/*
 * Copyright (c) 2000 Lyrisoft Solutions, Inc.
 * Used by permission
 */
package com.lyrisoft.chat.server.local.command;

import java.util.Vector;

import com.lyrisoft.chat.ICommands;
import com.lyrisoft.chat.client.IChatClient;

public class Rooms implements ICommandProcessorLocal {
    public void process(IChatClient client, String[] args) {
        Vector v = new Vector();
        for (int i=1; i < args.length; i++) {
            String name = args[i++];
            String count = args[i];
            v.addElement(name + ICommands.DELIMITER + count);
        }
        String[] rooms = new String[v.size()];
        v.copyInto(rooms);
        client.roomList(rooms);
    }
}
