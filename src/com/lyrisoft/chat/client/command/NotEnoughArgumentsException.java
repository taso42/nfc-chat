/*
 * Copyright (c) 2000 Lyrisoft Solutions, Inc.
 * Used by permission
 */
package com.lyrisoft.chat.client.command;

/**
 * Thrown by UserCommandProcessor's decompose() methods
 */
public class NotEnoughArgumentsException extends Exception {
    public NotEnoughArgumentsException(String s) {
        super(s);
    }
}
