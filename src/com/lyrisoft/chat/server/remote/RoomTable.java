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
 * The Room Table keeps track of all the rooms across the system, and the 
 * users each room contains.
 */ 
public class RoomTable {
    private HashMap _rooms = new HashMap();
    private HashMap _roomNames = new HashMap();
    private ChatServer _server;
    
    public RoomTable(ChatServer server) {
        _server = server;
    }

    public boolean userExistsInRoom(String username, String roomname) {
        return getUsers(roomname).contains(username);
    }
    
    public boolean roomExists(String roomname) {
        return _roomNames.keySet().contains(roomname);
    }
    
    public Collection getRoomNames() {
        LinkedList newList = new LinkedList();
        newList.addAll(_roomNames.values());
        return newList;
    }
        
    public Collection getUsers(String room) {
        LinkedList newList = new LinkedList();
        String key = MultiValueHashMap.key(room);
        Map users = (Map)_rooms.get(key);
        if (users != null) {
            newList.addAll(users.values());
        }
        return newList;
    }
        
    public int countUsers(String room) {
        return MultiValueHashMap.size(_rooms, MultiValueHashMap.key(room));
    }
        
    public boolean join(String room, String user) {
        String key = MultiValueHashMap.key(room);
        boolean isNew = MultiValueHashMap.size(_rooms, key) == 0;
        MultiValueHashMap.put(_rooms, key, user);
        if (isNew) {
            _roomNames.put(key, room);
        }
        return isNew;
    }
        
    public boolean part(String room, String user) {
        String key = MultiValueHashMap.key(room);
        MultiValueHashMap.remove(_rooms, key, user);
            
        if (MultiValueHashMap.size(_rooms, key) == 0) {
            _roomNames.remove(key);
            _rooms.remove(key);
            return true;
        }
        return false;
    }

    /**
     * part method duplcate used internally (by this class).
     * instead of removing the map entries, it returns the key
     * to be removed.
     */
    private String internalPart(String room, String user) {
        String key = MultiValueHashMap.key(room);
        MultiValueHashMap.remove(_rooms, key, user);
            
        if (MultiValueHashMap.size(_rooms, key) == 0) {
            return key;
        }
        return null;
    }


    public Collection signoff(String user) {
        LinkedList deadRooms = new LinkedList();
        for (Iterator i = _roomNames.values().iterator(); i.hasNext(); ) {
            String room = (String)i.next();
            String deadRoom = internalPart(room, user);
            if (deadRoom != null) {
                i.remove();
                _rooms.remove(deadRoom);
            }
        }
        return deadRooms;
    }

    public void dump(PrintStream out) {
        out.println("RoomTable dump:");
        MultiValueHashMap.dump(_rooms, out);
    }
}

