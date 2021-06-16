/*
 * Copyright (c) 2000 Lyrisoft Solutions, Inc.
 * Used by permission
 */
package com.lyrisoft.chat.server.remote;

import java.io.PrintStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import com.lyrisoft.util.MultiValueHashMap;

/**
 * The Server Table keeps track of all the servers across the system, and the 
 * users each server contains
 */ 
public class ServerTable {
    private HashMap _servers = new HashMap();
    private HashMap _serverNames = new HashMap();
    private HashMap _serverPings = new HashMap();
    private ChatServer _server;

    public ServerTable(ChatServer server) {
        _server = server;
    }

    public boolean userExists(String username) {
        for (Iterator i = _servers.values().iterator(); i.hasNext(); ) {
            Map users = (Map)i.next();
            if (users.get(MultiValueHashMap.key(username)) != null) {
                return true;
            }
        }
        return false;
    }

    public int countUsers(String servername) {
        Map m = (Map)_servers.get(MultiValueHashMap.key(servername));
        if (m != null) {
            return m.size();
        } else {
            return 0;
        }
    }

    public boolean serverExists(String servername) {
        return _servers.get(MultiValueHashMap.key(servername)) != null;
    }

    public void setLastBroadcastPing(String server, long time) {
        String key = MultiValueHashMap.key(server);
        _serverPings.put(key, new Long(time));
    }
    
    public long getLastPingReply(String server) {
        Long l = (Long)_serverPings.get(MultiValueHashMap.key(server));
        if (l != null) {
            return l.longValue();
        } else {
            return 0;
        }
    }

    public Collection getAllUsers() {
        LinkedList allUsers = new LinkedList();
        for (Iterator i = _servers.values().iterator(); i.hasNext(); ) {
            HashMap users = (HashMap)i.next();
            if (users != null) {
                allUsers.addAll(users.values());
            }
        }
        return allUsers;
    }
        
    public Collection getUsers(String server) {
        LinkedList newList = new LinkedList();
        HashMap users = (HashMap)_servers.get(MultiValueHashMap.key(server));
        if (users != null) {
            newList.addAll(users.values()); 
        }
        return newList;
    }
        
    public void add(String server) {
        String key = MultiValueHashMap.key(server);
        boolean reconnect = false;
        if (_servers.keySet().contains(key)) {
            _server.serverSignOff(server);
            reconnect = true;
        }
        _servers.put(key , new HashMap());
        _serverNames.put(key, server);
        setLastBroadcastPing(server, System.currentTimeMillis());
        if (reconnect) {
            ChatServer.log("Server " + server + " reconnected.");
        } else {
            ChatServer.log("Server " + server + " connected.");
        }
    }
        
    public void delete(String server) {
        String key = MultiValueHashMap.key(server);
        _servers.remove(key);
        _serverNames.remove(key);
        _serverPings.remove(key);
    }

    public Collection getServerNames() {
        LinkedList newList = new LinkedList();
        newList.addAll(_serverNames.values());
        return newList;
    }
        
    public void signon(String server, String user) {
        String key = MultiValueHashMap.key(server);
        if (!_serverNames.keySet().contains(key)) {
            add(server);
        }
        MultiValueHashMap.put(_servers, key, user);
    }
        
    public void signoff(String server, String user) {
        MultiValueHashMap.remove(_servers, MultiValueHashMap.key(server), user);
    }

    public void dump(PrintStream out) {
        out.println("ServerTable dump:");
        MultiValueHashMap.dump(_servers, out);
    }

    public void checkServerPings(long timeout) {
        LinkedList deadServers = new LinkedList();

        for (Iterator i = _serverPings.keySet().iterator(); i.hasNext(); ) {
            String key = (String)i.next();
            String name =(String) _serverNames.get(key);
            if (key.equalsIgnoreCase(_server.getName())) { 
                // ignore ourselves
                continue;
            }
            long now = System.currentTimeMillis();
            long last = getLastPingReply(key);
            if (last + timeout < now) {
                ChatServer.log("Ping timeout for server: " + name);
                deadServers.add(key);
            } else {
                if (ChatServer.DEBUG) {
                    double remaining =  ((last+timeout - now)) / 60000.0;
                    ChatServer.DEBUG("Ping for " + name + " is ok.  (" + remaining + " minutes remaining.)");
                }
            }
        }

        for (Iterator i = deadServers.iterator(); i.hasNext(); ) {
            _server.serverSignOff((String)i.next());
        }
    }
}
