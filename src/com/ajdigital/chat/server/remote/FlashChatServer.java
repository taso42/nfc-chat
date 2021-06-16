/*
 * NFC Flash Chat, http://www.ajdigital.com/nfcchat
 * Copyright (C) 2000  ajdigital development
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package com.ajdigital.chat.server.remote;

import java.io.IOException;
import java.net.Socket;

import javax.servlet.ServletContext;

import com.lyrisoft.chat.server.remote.ChatClient;
import com.lyrisoft.chat.server.remote.ChatServer;

/**
 * This is the Flash Chat Server
 */
public class FlashChatServer extends ChatServer {

    public FlashChatServer() throws Exception {
        super();
    }

    public FlashChatServer(String conf) throws Exception {
        super(conf);
    }
       
    public FlashChatServer(String conf, ServletContext context) throws Exception {
        super(conf, context);
    }

    public FlashChatServer(ServletContext context) throws Exception {
        super(context);
    }
    
    /**
     * We use a FlashChatClient b/c the message format is slightly different
     * 
     * @param s
     * @return FlashChatClient
     * @exception IOException
     */
    protected ChatClient createChatClient(Socket s) throws IOException {
        ChatServer.DEBUG("! Creating FlashChatClient");
        return new FlashChatClient(this, s);
    }

    /**
     * Instantiate a FlashChatServer and start accepting connections.
     * It would be nice to have one chat server be able to handle multiple
     * client types.
     * 
     * @param args
     */
    public static void main(String[] args) {
        try {
            FlashChatServer server = null;
            if (args.length > 1) {
                if ("-f".equals(args[0])) {
                    server = new FlashChatServer(args[1]);
                }
            }
            if (server == null) {
                server = new FlashChatServer();
            }
            server.acceptConnections();
        }
        catch (Exception e) {
            System.err.println("Error creating server");
            e.printStackTrace();
        }
    }

}
