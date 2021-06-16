/*
 * Copyright (c) 2000 Lyrisoft Solutions, Inc.
 * Used by permission
 */
package com.lyrisoft.chat.server.remote.persistence.auth;

import com.lyrisoft.chat.server.remote.AccessDenied;

/**
 * Interface for objects that will handle authentication.  The server will delegate signOn
 * calls to us.
 */
public interface IAuthenticator {
	// global & per-room
    public static final int NONE = 0;
    public static final int USER = 1;
    // per-room
	public static final int MODERATOR = 2;
	// global
	public static final int GOD = 100;
	public static final int SERVER = 1000;

	/** must implement constructor taking a ChatServer and two boolean arguments -- see NullAuthenticator */

    /**
     * Check the authentication of a user.
     * @param userId the user id
     * @param password the password
     * @return an Auth object that represents the userId and access level.
     */
    public Auth authenticate(String userId, String password) throws AccessDenied;
    
	public boolean isExistingUser(String userId);
}
