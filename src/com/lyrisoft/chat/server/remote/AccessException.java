/*
 * Copyright (c) 2000 Lyrisoft Solutions, Inc.
 * Used by permission
 */
package com.lyrisoft.chat.server.remote;

/**
 * Thrown if there is a problem authenticating
 *
 * @see ChatServer#signOn
 */
public class AccessException extends Exception {
    public AccessException(String s) {
        super(s);
    }
}
