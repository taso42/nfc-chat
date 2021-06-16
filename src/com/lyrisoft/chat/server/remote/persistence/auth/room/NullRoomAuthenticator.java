/*
 * Copyright (c) 2000 Lyrisoft Solutions, Inc.
 * Used by permission
 */
package com.lyrisoft.chat.server.remote.persistence.auth.room;
import com.lyrisoft.chat.server.remote.ChatClient;
import com.lyrisoft.chat.server.remote.RoomServer;

/**
 * Interface for objects that will handle authentication.  The server will delegate signOn
 * calls to us.
 */
public class NullRoomAuthenticator implements IRoomAuthenticator {
	public boolean isCreateAllowed(ChatClient client, String room, String password) {
		return true;
	}
    
    public int getAccessLevel(ChatClient client, RoomServer room) {
    	return client.getAccessLevel();
    }
    
    public ChatClient getNextOp(RoomServer room) {
    	return room.getOldestClient();
    }
}
