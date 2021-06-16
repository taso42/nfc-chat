/*
 * Copyright (c) 2000 Lyrisoft Solutions, Inc.
 * Used by permission
 */
package com.lyrisoft.chat.server.remote.command;

import com.lyrisoft.chat.Translator;
import com.lyrisoft.chat.server.remote.ChatClient;
import com.lyrisoft.chat.server.remote.ChatServer;
import com.lyrisoft.chat.server.remote.Formatter;

public class Stats extends CommandBase {
    public static final String JAVA_VERSION = System.getProperty("java.version");
    public static final String JAVA_VENDOR = System.getProperty("java.vendor");
    public static final String OS_NAME = System.getProperty("os.name");

    public boolean process(ChatClient client, String[] args) {
        ChatServer server = client.getServer();
        int nRooms = server.getRoomCount();
        int nUsers = server.getUserCount();
        long uptime = server.getUptime();

        client.generalMessage(Translator.getMessage("stats", server.getName()));
        client.generalMessage("  " + Translator.getMessage("stats.uptime", Formatter.millisToString(uptime)));
        client.generalMessage("  " + Translator.getMessage("stats.cumulative.logins", 
                                                    String.valueOf(server.getCumulativeLogins())));
        client.generalMessage("  " + Translator.getMessage("stats.num.rooms", String.valueOf(nRooms)));
        client.generalMessage("  " + Translator.getMessage("stats.num.users", String.valueOf(nUsers)));
        client.generalMessage("");
        client.generalMessage(Translator.getMessage("stats.jvm"));
        client.generalMessage("  " + Translator.getMessage("stats.java.version", JAVA_VERSION, JAVA_VENDOR));
        client.generalMessage("  " + Translator.getMessage("stats.os", OS_NAME));
        client.generalMessage("  " + Translator.getMessage("stats.mem", 
                                                    String.valueOf(Runtime.getRuntime().freeMemory() / 
                                                                   1024),
                                                    String.valueOf(Runtime.getRuntime().totalMemory() /
                                                                   1024),
                                                    "KB"));
        client.generalMessage(Translator.getMessage("stats.servers"));
        String[] servers = server.getServerNames();
        int scount = 0;
        for (int i=0; i < servers.length; i++) {
            if (servers[i].equalsIgnoreCase(server.getName())) {
                continue;
            }
            scount++;
            int cnt = server.getUserCountOnServer(servers[i]);
            client.generalMessage(Translator.getMessage("stats.server", servers[i], 
                                                        String.valueOf(cnt)));
        }
        if (scount == 0) {
            client.generalMessage("  " + Translator.getMessage("none"));
        }
       return false;
    }


    public void processDistributed(String client, String origin, String[] args, ChatServer server) { 
    }
}
