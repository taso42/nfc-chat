/*
 * NFC Flash Chat, http://www.ajdigital.com/nfcchat/
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

import com.lyrisoft.chat.IConnectionListener;
import com.lyrisoft.chat.server.remote.ChatServer;
import com.lyrisoft.chat.server.remote.ConnectionHandler;
import com.lyrisoft.chat.server.remote.ReaderThread;

/**
 * Extends ConnectionHandler to add Flash specific Message format
 * 
 * @author <A HREF="mailto:alon@ajdigital.com">Alon Salant</A>
 */
public class FlashConnectionHandler extends ConnectionHandler {

    public FlashConnectionHandler(Socket s, IConnectionListener listener) throws IOException {
	super(s, listener);
    }

    /**
     * We need to use a FlashReaderThread since our messages are coming from a Flash client
     * 
     * @return 
     */
    public ReaderThread createReader() {
        return new FlashReaderThread(this, _connectionListener, _inputStream);
    }

    /**
     * Queue a Flash message
     * 
     * @param message
     */
    public void queueMessage(String message) {
        _dispatcher.queue(new FlashMessage(this, message));
    }

    /**
     * Print message w/out newline
     * 
     * @param message
     */
    public void sendImmediately(String message) {
        ChatServer.DEBUG("< " + message);
	_out.print(message);
        _out.flush();
    }

}
