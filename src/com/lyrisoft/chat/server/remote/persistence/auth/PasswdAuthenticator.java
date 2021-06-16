/*
 * Copyright (c) 2000 Lyrisoft Solutions, Inc.
 * Used by permission
 */
package com.lyrisoft.chat.server.remote.persistence.auth;

import java.io.IOException;

import com.lyrisoft.chat.server.remote.AccessDenied;
import com.lyrisoft.chat.server.remote.ChatServer;

/**
 * Authenitcator that reads from a password file.<p>
 *
 * The file is made up of colon-delimited fields:
 * <userId>:<access_level>:<password>
 * <p>
 * If a user if found in the password file, his password is checked.
 * If a user is not found in the password file, the access level IAuthenticator.USER
 * is returned.
 */
public class PasswdAuthenticator extends NullAuthenticator {
	
	public PasswdAuthenticator(ChatServer server, boolean allowGuests, boolean storeGuests) {
		super(server, allowGuests, storeGuests);
	}
			
    public Auth authenticate(String userId, String password) throws AccessDenied {
        try {
            PasswdRecord record = Passwd.getRecord(userId, password);
            if (record == null) {
            	if (_allowGuests) {
            		Auth auth = super.authenticate(userId, password);
            		if (_storeGuests) {
		            	Passwd.writeRecord(auth.getUserId(), USER, password);
            		}
    	            return auth;
            	} else {
            		throw new AccessDenied(userId);
            	}
            } else {
                return new Auth(userId, record.access);
            }
        }
        catch (IOException e) {
            ChatServer.log(e);
            throw new AccessDenied(userId);
        }
    }
    
	/** checks all stored users besides just those currently logged on */
    public boolean isExistingUser(String userId) {
    	return super.isExistingUser(userId) || Passwd.isExistingUser(userId);
    }
    
}
