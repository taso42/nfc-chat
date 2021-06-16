/*
 * Copyright (c) 2000 Lyrisoft Solutions, Inc.
 * Used by permission
 */
package com.lyrisoft.chat.client.command;

import java.util.StringTokenizer;
import java.util.Vector;

import com.lyrisoft.chat.Translator;
import com.lyrisoft.chat.client.Client;

/**
 * Process a command that came from the user or user interface
 */
public abstract class UserCommandProcessor {
    /**
     * Process some user input. The subclass implements this method.
     *
     * @param input the input that the user typed
     * @param arg an optional additional argument that the GUI might
     *            have passed.  For example, a room name.
     */
    public abstract void process(String input, String arg, Client client) throws NotEnoughArgumentsException;

    /**
     * Subclasses might find this method useful.
     * Decomposes a string into a specified number of args.  The String gets split
     * on the space character (' ').  If there are more tokens in the string
     * than 'nArgs', the last arg is the remainder of the line.
     * <pre>
     * Example:
     * format("msg joe hi, how are you doing", 3)
     * returns: { "msg", "joe", "hi, how are you doing" }
     * </pre>
     *
     * @param input the raw string
     * @param nArgs the number of args to create in the array
     * @return a String composed of args, delimited by ICommands.DELIMITER
     * @exception NotEnoughArgumentsException if there were not enough arguments in the input
     *                                        to create an array of length nArgs
     */
    public String[] decompose(String input, int nArgs) throws NotEnoughArgumentsException
    {
        Vector v = new Vector(nArgs);
        int count=0;
        try {
            StringTokenizer st = new StringTokenizer(input, " ");
            while (count < nArgs-1) {
                v.addElement(st.nextToken());
                count++;
            };
            v.addElement(st.nextToken("").trim());
            String[] args = new String[v.size()];
            v.copyInto(args);
            return args;
        }
        catch (java.util.NoSuchElementException e) {
            if (count > 0) {
                throw new NotEnoughArgumentsException(Translator.getMessage("not.enough.args2",
                                                                            (String)v.elementAt(0)));
            } else {
                throw new NotEnoughArgumentsException(Translator.getMessage("not.enough.args1"));
            }
        }
    }

    /**
     * Subclasses might find this method useful.
     * Does exactly the same as the other decompose() method, except 'arg' is
     * inserted as the second argument.
     *
     * <pre>
     * Example:
     * format("msg hi, how are you", "joe", 3)
     * returns: { "msg", "joe", "hi, how are you" }
     * </pre>
     *
     * @param input the raw string
     * @param arg the argument to insert
     * @param nArgs the number of args to create in the array
     * @return a String composed of args, delimited by ICommands.DELIMITER
     * @exception NotEnoughArgumentsException if there were not enough arguments in the input
     *                                        to create an array of length nArgs
     */
    public String[] decompose(String input, String arg, int nArgs)
        throws NotEnoughArgumentsException
    {
        if (nArgs > 2) {
            Vector v = new Vector(nArgs);
            StringTokenizer st = new StringTokenizer(input, " ");
            int count=0;
            try {
                while (count < nArgs-1) {
                    if (count == 1) {
                        v.addElement(arg);
                    } else {
                        v.addElement(st.nextToken());
                    }
                    count++;
                };
                v.addElement(st.nextToken("").trim());

                String[] args = new String[v.size()];
                v.copyInto(args);
                return args;
            }
            catch (java.util.NoSuchElementException e) {
                if (count > 0) {
                    throw new NotEnoughArgumentsException(Translator.getMessage("not.enough.args2",
                                                                                (String)v.elementAt(0)));
                } else {
                    throw new NotEnoughArgumentsException(Translator.getMessage("not.enough.args1"));
                }
            }
        } else {
            String[] s = { input, arg };
            return s;
        }
    }
}
