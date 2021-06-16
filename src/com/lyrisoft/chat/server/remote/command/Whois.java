/*
 * Copyright (c) 2000 Lyrisoft Solutions, Inc.
 * Used by permission
 */
package com.lyrisoft.chat.server.remote.command;

import java.util.Date;
import java.util.Vector;

import com.lyrisoft.chat.ICommands;
import com.lyrisoft.chat.Translator;
import com.lyrisoft.chat.server.remote.ChatClient;
import com.lyrisoft.chat.server.remote.ChatServer;
import com.lyrisoft.chat.server.remote.Formatter;

public class Whois extends CommandBase implements ICommands {
    public boolean process(ChatClient client, String[] args) {
        if (args.length < 2) {
            client.generalError(getUsage(args[0]));
            return false;
        } else {
            ChatClient otherGuy = client.getServer().getClient(args[1]);
            if (otherGuy == null) {
                if (!client.getServer().userExists(args[1])) {
                    client.error(NO_SUCH_USER, args[1]);
                    return false;
                }
                return true;
            } else {
                String[] whois = getWhois(otherGuy);
                for (int i=0; i < whois.length; i++) {
                    client.generalMessage(whois[i]);
                }
                return false;
            }
        }
    }

    public void processDistributed(String client, String origin, String[] args, ChatServer server) { 
        ChatClient otherGuy = server.getClient(args[1]);
        if (otherGuy != null) {
            String[] whois = getWhois(otherGuy);
            for (int i=0; i < whois.length; i++) {
                server.distribute(otherGuy, REMOTE_CLIENT_MESSAGE + 
                                  DELIMITER + client +
                                  DELIMITER + whois[i]);
            }
        } else {
            System.err.println(args[1] + " not found for whois");
        }
    }

    String[] getWhois(ChatClient c) {
        Vector v = new Vector();
        v.add(Translator.getMessage("whois.user", c.getUserId()));
        v.add("  " + Translator.getMessage("whois.host", c.getHost()));
        v.add("  " + Translator.getMessage("whois.since", (new Date(c.getConnectionTime())).toString()));
        v.add("  " + Translator.getMessage("whois.idle", Formatter.millisToString(c.getIdle())));
        v.add("  " + Translator.getMessage("whois.access", String.valueOf(c.getAccessLevel())));
        v.add("  " + Translator.getMessage("whois.server", c.getServer().getName()));
        v.add("  " + Translator.getMessage("whois.ping", String.valueOf(c.getServerPingAvg()),
                                           String.valueOf(c.getServerPingCount())));
        
        // if we know the client verison, print that too
        String version = c.getClientVersion();
        if (version != null) {
            v.add("  " + Translator.getMessage("whois.version", version));
        }
        
        String[] whois = new String[v.size()];
        v.copyInto(whois);
        return whois;
    }
}
