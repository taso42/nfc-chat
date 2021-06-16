/*
 * Copyright (c) 2000 Lyrisoft Solutions, Inc.
 * Used by permission
 */
package com.lyrisoft.chat.client;

/**
 * Interface for an object that can construct a UserCommand object
 */
public interface IUserCommandFactory {
    public UserCommands createUserCommands();

}
