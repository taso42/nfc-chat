/*
 * Copyright (c) 2000 Lyrisoft Solutions, Inc.
 * Used by permission
 */
package com.lyrisoft.chat.client;

import java.util.Properties;

import com.lyrisoft.chat.Translator;
import com.lyrisoft.chat.client.gui.IChatGUIFactory;
import com.lyrisoft.chat.server.local.CommandProcessorLocal;

/**
 * Main class for standalone-client execution.
 * <p>
 * Command-line args: [host] [port]
 * <p>
 * Immediately instantiates a new Client object, which gets the client going
 */
public class Main {
    public static void main(String[] args) {
        String host = null;
        int port = 0;

        try {
            host = args[0];
            port = Integer.parseInt(args[1]);
        }
        catch (Exception e) {
            showUsageAndQuit();
        }

        IChatGUIFactory factory = null;
        UserCommands userCommands = null;
        CommandProcessorLocal commandProcessor = null;
        Client client = new Client(host, port);
        try {
            // get the specified GUI Factory (AWT is default)
            String guiFactoryName = System.getProperty("guiFactory", "com.lyrisoft.chat.client.gui.awt102.GUIFactory");
            factory = (IChatGUIFactory)Class.forName(guiFactoryName).newInstance();

            // load up the command processor
            Properties p = client.getProperties("commandProcessors.properties");
            if (p != null) {
                commandProcessor = new CommandProcessorLocal(p);
            } else {
                throw new Exception("Could not load commandProcessors.properties");
            }

            // load up the user-command processor
            p = client.getProperties("userCommands.properties");
            if (p != null) {
                userCommands = new UserCommands(p);
            } else {
                throw new Exception("Could not load userCommands.properties");
            }

            // load up our messages file, for the translator, and init the translator
            p = client.getProperties("messages.properties");
            initTranslator(p);
        }
        catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }

        // set the guiFactory, userCommands, and commandProcessor attributes in the client
        client.setAttribute("guiFactory", factory);
        client.setAttribute("userCommands", userCommands);
        client.setAttribute("commandProcessor", commandProcessor);

        // now we can init the client
        client.init();

        // and show a login prompt
        client.getGUI().showLogin();
    }

    protected static void initTranslator(Properties p) {
        Translator.init(p);
    }

    static void showUsageAndQuit() {
        System.err.println("usage: java com.lyrisoft.chat.client.Main host port");
        System.exit(1);
    }
}
