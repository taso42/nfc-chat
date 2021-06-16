/*
 * Copyright (c) 2000 Lyrisoft Solutions, Inc.
 * Used by permission
 */
package com.lyrisoft.chat.server.remote.persistence.auth;

import com.lyrisoft.chat.server.remote.AccessDenied;
import com.lyrisoft.chat.server.remote.ChatServer;

/**
 * Authenitcator that authenticates everybody
 * also serves as superclass for "real" authenticators
 */
public class NullAuthenticator implements IAuthenticator {
	protected ChatServer _server;
	/** Boolean indicating whether users not in the database are allowed access. */
	protected boolean _allowGuests;
	/** Boolean indicating whether guests should be stored in the database. */
	protected boolean _storeGuests;
	
	public NullAuthenticator(ChatServer server, boolean allowGuests, boolean storeGuests) {
		_server = server;
		_allowGuests = allowGuests;
		_storeGuests = storeGuests;
	}

	/** handles username conflicts by appending integers until it finds an unused one */	
    public Auth authenticate(String userId, String password) 
    throws AccessDenied {
    	// allow/store Guests ignored
    	String newUserId = userId;
    	int i = 1;
    	while (isExistingUser(newUserId)) {
    		newUserId = newUserId + i;
    		i++;
    	}
        return new Auth(newUserId, USER);
    }
    
    public boolean isExistingUser(String userId) {
    	return _server.getClient(userId) != null;
    }
    
}
