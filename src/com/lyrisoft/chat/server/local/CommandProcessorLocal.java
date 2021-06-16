/*
 * Copyright (c) 2000 Lyrisoft Solutions, Inc.
 * Used by permission
 */
package com.lyrisoft.chat.server.local;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;

import com.lyrisoft.chat.ICommands;
import com.lyrisoft.chat.client.IChatClient;
import com.lyrisoft.chat.server.local.command.ICommandProcessorLocal;
import com.lyrisoft.chat.server.local.command.UnknownCommand;

/**
 * CommandProcessorLocal lives on the local (client) side and processes
 * messages that come from the server.
 *
 * This class maintains a registry of ICommandProcessorLocal instances.  When the process
 * method is called, the appropriate ICommandProcessorLocal is invoked.  If no processor
 * is found, generalMessage is invoked on the client.
 *
 * @see com.lyrisoft.chat.server.local.command
 * @see com.lyrisoft.chat.server.local.command.ICommandProcessorLocal
 */
public class CommandProcessorLocal implements ICommands {
    private Hashtable _processors;
    private UnknownCommand unknownCommandProcessor = new UnknownCommand();

    public CommandProcessorLocal(Properties p) {
        _processors = new Hashtable();
        for (Enumeration e = p.propertyNames(); e.hasMoreElements(); ) {
            String name = (String)e.nextElement();
            try {
                String className = p.getProperty(name);
                ICommandProcessorLocal cp = (ICommandProcessorLocal)Class.forName(className).newInstance();
                extendCommandSet("/" + name, cp);
//                System.err.println("Initiliazed " + name + " command processor");
            }
            catch (Exception ex) {
                ex.printStackTrace();
                System.err.println("Failed to initialize " + name + " command processor");
            }
            
        }
    }

    /**
     * Used to extend the core set of commands that the client can process. 
     * 
     * @param command the first argument of the command
     * @param processor an ICommandProcessorLocal instance that will handle the new command.
     */
    public void extendCommandSet(String command, ICommandProcessorLocal processor) {
        _processors.put(command, processor);
    }

    /**
     * Process a message that came over the socket
     * Find the appropriate ICommandProcessorLocal instance in the registry, then
     * invoke its process method
     *
     * @param input the raw message
     * @param client the chat client to perform the callback on
     */
    public void process(String input, IChatClient client) {
        String[] args = decompose(input);
        if (args.length == 0) {
            return;
        }
        ICommandProcessorLocal processor = (ICommandProcessorLocal)_processors.get(args[0]);
        if (processor == null) {
            client.generalMessage(input);
        } else {
            processor.process(client, args);
        }
    }

    /**
     * Helper function that breaks a server message down into an array
     */
    public static String[] decompose(String input) {
        StringTokenizer st = new StringTokenizer(input, DELIMITER);
        Vector v = new Vector(5);
        while (st.hasMoreTokens()) {
            v.addElement(st.nextToken());
        }
        String[] args = new String[v.size()];
        v.copyInto(args);
        return args;
    }
}
