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
public interface IRoomAuthenticator {
	/** default constructor will always be invoked by ChatServer */

	/** Call on create. */
	// we need to include password here so we can reject
	// attempts to create w/ password a room that's supposed
	// to be open to all.
	public boolean isCreateAllowed(ChatClient client, String room, String password);
    
    /** Call on join.  returns one of the values defined by IAuthenticator. */
    // clints whose AccessLevel is MODERATOR will be added to ops list.
    public int getAccessLevel(ChatClient client, RoomServer room);
    
    /** Call on create and whenever last op leaves.  Returns null for nobody. */
	// allows implemenation of "user longest in room gets to be op."
    public ChatClient getNextOp(RoomServer room);
}
