/*
 * Copyright (c) 2000 Lyrisoft Solutions, Inc.
 * Used by permission
 */
package com.lyrisoft.chat.server.remote.command;

public abstract class CommandBase implements ICommandProcessorRemote {
    protected String _help;
    protected int _access;
    protected String _usage;

    public void setHelp(String help) {
        _help = help;
    }

    public String getHelp() {
        return _help;
    }

    public void setAccessRequired(int access) {
        _access = access;
    }

    public int accessRequired() {
        return _access;
    }

    public void setUsage(String usage) {
        _usage = usage;
    }

    public String getUsage(String myName) {
        return _usage;
    }
}
