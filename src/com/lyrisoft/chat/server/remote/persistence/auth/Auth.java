/*
 * Copyright (c) 2000 Lyrisoft Solutions, Inc.
 * Used by permission
 */
package com.lyrisoft.chat.server.remote.persistence.auth;

public class Auth {
    private String _userId;
    private int _level;

    public Auth(String userId, int level) {
        _userId = userId;
        _level = level;
    }

    public String getUserId() {
        return _userId;
    }

    public int getAccess() {
        return _level;
    }
}
