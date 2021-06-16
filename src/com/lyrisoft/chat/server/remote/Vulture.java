/*
 * Copyright (c) 2000 Lyrisoft Solutions, Inc.
 * Used by permission
 */
package com.lyrisoft.chat.server.remote;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * The Vulture periodically scans the userlist and boots
 * clients who have been idle longer than a specified timeout.
 */
public class Vulture extends Thread implements com.lyrisoft.chat.Constants {
    private static final int SLEEP = 45000;  // 45 seconds

    private static final int CLIENT_TIMEOUT = 300000; // 5 minutes
    private static final int SERVER_TIMEOUT = 300000; // 5 minutes
    private static final int BROADCAST_PING_INTERVAL = 60000; // 1 minute
    private static final int CLIENT_PING_INTERVAL = 60000; // 1 minute

    private long _timeoutMillis;
    private double _timeoutMinutes;
    private HashSet _clients;
    private boolean _keepGoing = true;
    private ChatServer _server;

    private long _lastBroadcastPing;

    /**
     * @param timeout idle timeout, in minutes
     */
    public Vulture(ChatServer server, double timeout) {
        _server = server;
        _timeoutMinutes = timeout;
        _timeoutMillis = (long)(timeout * 60 * 1000);
        setDaemon(true);
        setPriority(MIN_PRIORITY);
        
        _clients = new HashSet();
    }
    
    public void run() {
        if (_timeoutMinutes != 0) {
            ChatServer.log("Vulture: eating connections that are idle over " +
                           _timeoutMinutes + " minutes.");
        } else {
            ChatServer.log("Vulture: idle timeout is disabled.");
        }
        while (_keepGoing) {
            try {
                doClientCleanup();
                doServerCleanup();
                sleep(SLEEP);
            }
            catch (InterruptedException e) {
                return;
            }
        }
    }

    public void pleaseStop() {
        _keepGoing = false;
    }

    void doServerCleanup() {
        long now = System.currentTimeMillis();
        if (now - _lastBroadcastPing > BROADCAST_PING_INTERVAL) {
            _server.sendBroadcastPing();
            _lastBroadcastPing = now;
        }
        _server.checkServerPings(SERVER_TIMEOUT);
    }

    void doClientCleanup() {
        LinkedList deadClients = new LinkedList();
        
        synchronized (_clients) {
            for (Iterator i = _clients.iterator(); i.hasNext(); ) {
                ChatClient c = (ChatClient)i.next();
                
                long now = System.currentTimeMillis();
                // check for ping timeout, and re-ping the clients who are alive
                if (c.getLastServerPing() != 0) {
                    long delta = now - c.getLastServerPong();
                    if (delta > CLIENT_TIMEOUT) {
                        deadClients.add(c);
                        ChatServer.log("ping timeout: " + c.getUserId());
                    } else if (delta > CLIENT_PING_INTERVAL) {
                        c.ping(SERVER_NAME, String.valueOf(now));
                        c.setLastServerPing(now);
                    }
                } else {
                    c.ping(SERVER_NAME, String.valueOf(now));
                    c.setLastServerPing(now);
                }
                

                // check for idletimeouts
                if (_timeoutMillis > 0) {
                    long idle = c.getIdle();
                    
                    if (idle > _timeoutMillis) {
                        c.generalError("idle timeout ( " + _timeoutMinutes + " minutes) exceeded.");
                        deadClients.add(c);
                    }  
                }
            }
        }

        for (Iterator i = deadClients.iterator(); i.hasNext(); ) {
            ChatClient c = (ChatClient)i.next();
            String userId = c.getUserId() == null ? "anonymous user" : c.getUserId();
            ChatServer.log("Vulture: eating " + userId);
            c.getServer().signOff(c);
        }
    }

    public void addClient(ChatClient c) {
        synchronized (_clients) {
            _clients.add(c);
        }
    }

    public void removeClient(ChatClient c) {
        synchronized (_clients) {
            _clients.remove(c);
        }
    }
}
