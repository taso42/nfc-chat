/*
 * Copyright (c) 2000 Lyrisoft Solutions, Inc.
 * Used by permission
 */
package com.lyrisoft.chat.client;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import java.util.StringTokenizer;

import com.lyrisoft.chat.Translator;
import com.lyrisoft.chat.client.command.NotEnoughArgumentsException;
import com.lyrisoft.chat.client.command.SayToRoom;
import com.lyrisoft.chat.client.command.UserCommandProcessor;

/**
 * Everything that a user types, or clicks comes here for processing.
 * Based on what was typed, the appropriate UserCommandProcessor is
 * found and invoked.  By default, the SayToRoom UserCommandProcessor
 * gets invoked.
 *
 * @see com.lyrisoft.chat.client.command.UserCommandProcessor
 */
public class UserCommands {
    private Hashtable _processors;
    private UserCommandProcessor defaultProcessor = new SayToRoom();

    /**
     * Instantiate a UserCommands object that initializes from the
     * given Properties object.
     */
    public UserCommands(Properties p) {
        _processors = new Hashtable();
        for (Enumeration e = p.propertyNames(); e.hasMoreElements(); ) {
            String name = (String)e.nextElement();
            try {
                String className = p.getProperty(name);
                UserCommandProcessor cp = (UserCommandProcessor)Class.forName(className).newInstance();
                extendCommandSet("/" + name, cp);
            }
            catch (Exception ex) {
                ex.printStackTrace();
                System.err.println("Failed to initialize " + name + " user command");
            }
        }
    }
    
    /**
     * Extend the command set by specifying a new command and a
     * UserCommandProcessor that will handle it.
     */
    public void extendCommandSet(String command, UserCommandProcessor p) {
        _processors.put(command, p);
    }

    /**
     * Process some user input.  Use the first word in the input string
     * to look up the appropriate UserCommandProcessor (the default UserCommandProcessor,
     * if none are found, is SayToRoom), and invoke its process() method.
     *
     * @param input the text that the user typed
     * @param arg an argument (may be null).  Typically the room the message was typed in
     * @param client the instance of the Client
     *
     * @see com.lyrisoft.chat.client.command.UserCommandProcessor
     * @see com.lyrisoft.chat.client.command
     * @see com.lyrisoft.chat.client.command.SayToRoom
     */
    public void process(String input, String arg, Client client) {
        input = input.trim();
        if (input.length() == 0) {
            return;
        }
        StringTokenizer st = new StringTokenizer(input, " ");
        String key = st.nextToken();
        UserCommandProcessor processor = (UserCommandProcessor)_processors.get(key);
        try {
            if (processor != null) {
                processor.process(input, arg, client);
            } else {
                if (key.charAt(0) != '/') {
                    defaultProcessor.process(input, arg, client);
                } else {
                    client.generalError(Translator.getMessage("no.such.command", key));
                }
            }
        }
        catch (NotEnoughArgumentsException e) {
            client.generalError(e.getMessage());
            client.getServerInterface().help(key);
        }
    }
}


