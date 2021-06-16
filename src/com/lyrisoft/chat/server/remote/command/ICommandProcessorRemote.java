/*
 * Copyright (c) 2000 Lyrisoft Solutions, Inc.
 * Used by permission
 */
package com.lyrisoft.chat.server.remote.command;

import com.lyrisoft.chat.server.remote.ChatClient;
import com.lyrisoft.chat.server.remote.ChatServer;

/**
 * An object that can process a message that came from the client side.
 * @see com.lyrisoft.chat.server.remote.command
 */
public interface ICommandProcessorRemote {
    /**
     * @return true if this message should be distributed to other clients
     */
    public boolean process(ChatClient client, String[] args);

//    public void processDistributed(String client, String[] args, ChatServer server);
    public void processDistributed(String client, String origin, String[] args, ChatServer server);

    /**
     * Construct a usage line (e.g., "usage: /msg [user] [message]")
     *
     * The reason we pass name here is because this object doesn't have a particular name.  The name
     * is really determined by the CommandProcessorRemote class.
     * A command could possibly have aliases, or it's name may change.  To play it safe then, we
     * will pass the name here in order to derive the usage string.
     *
     * Note: the output of this command might not be completely accurate.  The output should
     * reflect what the user has to type, not the actual parameters according to the protocol. (The
     * client program knows how to add implicit paramaters, such as the time argument for ping)
     *
     * @param name the name to use in constructing the usage line
     * @return the usage line
     */
    public String getUsage(String myName);

    /**
     * Get a help line about this command
     * @return a short blurb about what this command does, or null if this command should not show
     *         up in a help message
     */
    public String getHelp();

    /**
     * Return the minimum access level required to carry out this command.  Access levels
     * are defined as constants in IAuthenticator.
     * @return the minimum access level required to carry out this command.
     * @see com.lyrisoft.chat.server.remote.IAuthenticator
     */
    public int accessRequired();


    // Setter methods
    public void setHelp(String help);

    public void setAccessRequired(int access);

    public void setUsage(String usage);
}
