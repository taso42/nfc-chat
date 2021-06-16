/*
 * Copyright (c) 2000 Lyrisoft Solutions, Inc.
 * Used by permission
 */
package com.lyrisoft.chat.server.remote;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import com.lyrisoft.chat.ICommands;
import com.lyrisoft.chat.Translator;
import com.lyrisoft.chat.server.remote.command.ICommandProcessorRemote;
import com.lyrisoft.chat.server.remote.command.UnknownCommand;

/**
 * When messages come in on the socket from the client, they have to be processed.  This class
 * does that, by invoking the proper ICommandProcessorRemote.  Instance of ICommandProcessorRemote
 * are kept in a registry (HashMap), using the constants in ICommands as keys.
 *
 * @see com.lyrisoft.chat.ICommands
 */
public class CommandProcessorRemote implements ICommands {
    private static HashMap _processors;
    private static HashSet _idleTimeImmune;
    private static UnknownCommand unknownCommandProcessor = new UnknownCommand();

    public static void init(Properties p) {
        if (_processors != null && _idleTimeImmune != null) {
            ChatServer.log("CommandProcessorRemote: Warning: init() called a second time");
        }
        
        _processors = new HashMap();
        _idleTimeImmune = new HashSet();
        
        for (Enumeration e = p.propertyNames(); e.hasMoreElements(); ) {
            String name = (String)e.nextElement();
            int idx = name.indexOf(".");
            if (idx < 1) {
                ChatServer.logError("CommandProcessorRemote: unknown property: " + name);
                continue;
            }
            String command = name.substring(0, idx);
            if (name.endsWith(".class")) {
                String className = p.getProperty(name);
                ChatServer.log("CommandProcessorRemote: initting the " + command + " command processor");
                try {
                    ICommandProcessorRemote cp = 
                            (ICommandProcessorRemote)Class.forName(className).newInstance();
                    
                    String help = p.getProperty(command + ".help");
                    cp.setHelp(help);

                    String usage = p.getProperty(command + ".usage");
                    cp.setUsage(usage);
                    
                    int access = 0;
                    try {
                        String s = p.getProperty(command + ".access");
                        access = Integer.parseInt(s);
                    }
                    catch (Exception ex) {}
                    cp.setAccessRequired(access);
                    
                    CommandProcessorRemote.extendCommandSet("/" + command, cp);
                }
                catch (Exception ex) {
                    ChatServer.logError("Unable to install the " + command + " command");
                    ChatServer.log(ex);
                    ChatServer.logError("Continuing despite error(s)");
                }
            } else if (name.endsWith(".idleImmune")) {
                _idleTimeImmune.add("/" + command);
            }
        }
    }

    /**
     * Used to extend the core set of commands that the server can process.  Note: If a
     * custom reply is to be sent back, there should be a corresponding extension on the
     * client side.
     *
     * @param command the first argument of the command
     * @param processor an ICommandProcessorRemote instance that will handle the new command.
     * @see com.lyrisoft.chat.server.local.CommandProcessorLocal#extendCommandSet
     */
    public static void extendCommandSet(String command, ICommandProcessorRemote processor) {
        _processors.put(command, processor);
    }

    public static void setIdleTimeImmune(String command) {
        _idleTimeImmune.add(command);
    }

    /**
     * Process a raw message from the client
     * <ul>
     * <li>The message is decomposed into a string array of arguments
     * <li>Use the first argument to look up an ICommandProcessorRemote instance in the registry
     * <li>If found, check the access level
     * <li>Invoke process() on the ICommandProcessorRemote instance
     * </ul>
     *
     * If an ICommandProcessorRemote was not found, then use the UnknownCommandProcessor()<br>
     * If the access level on the client is less than the required access, then send a
     * generalError() to the client.
     *
     * @return a boolean indicating whether idle time should be affected
     * 
     * @see com.lyrisoft.chat.server.remote.command
     * @see com.lyrisoft.chat.server.remote.ChatClient#generalError
     */
    public static boolean process(String input, ChatClient client) {
        String[] args = decompose(input);
        if (args.length == 0) {
            return true;
        }
        ICommandProcessorRemote processor = (ICommandProcessorRemote)_processors.get(args[0]);
        if (processor == null) {
            unknownCommandProcessor.process(client, args);
            return true;
        } else {
            if (client.getAccessLevel() < processor.accessRequired()) {
                client.generalError(Translator.getMessage("access_denied"));
//                client.generalError("Access denied");
                return true;
            } else {
                boolean distribute = processor.process(client, args);
                if (distribute) {
                    client.getServer().distribute(client, input);
                }
                return !_idleTimeImmune.contains(args[0]);
            }
        }
    }

    public static void processDistributed(String input, String origin, String client, 
                                          ChatServer server) 
    {
        String[] args = decompose(input);
        if (args.length == 0) {
            return;
        }
        ICommandProcessorRemote processor = (ICommandProcessorRemote)_processors.get(args[0]);
        if (processor == null) { 
            return;
        } else { 
            processor.processDistributed(client, origin, args, server);
        }
    }

    /**
     * Decompose a raw message into an array of String, splitting on
     * the DELIMITER defined in ICommands.
     * @see com.lyrisoft.chat.ICommands
     */
    public static String[] decompose(String input) {
        StringTokenizer st = new StringTokenizer(input, DELIMITER);
        ArrayList list = new ArrayList(5);
        while (st.hasMoreTokens()) {
            list.add(st.nextToken());
        }
        String[] args = new String[list.size()];
        return (String[])list.toArray(args);
    }

    /**
     * Get the map that contains the command processors.  This method exists
     * so the Help command processor can do its work
     */
    public static Map getCommandProcessors() {
        return _processors;
    }
}
