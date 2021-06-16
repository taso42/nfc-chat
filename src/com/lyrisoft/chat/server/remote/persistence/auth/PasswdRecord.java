/*
 * Copyright (c) 2000 Lyrisoft Solutions, Inc.
 * Used by permission
 */
package com.lyrisoft.chat.server.remote.persistence.auth;

/**
 * Represents a line in the passwd file.  All fields are public
 */
public class PasswdRecord {
    public String userId;
    public String passwd;
    public int access;

    /**
     * Constructor for convenience
     */
    public PasswdRecord(String userId, int access, String cryptedPasswd) {
        this.userId = userId;
        this.access = access;
        this.passwd = cryptedPasswd;
    }
}