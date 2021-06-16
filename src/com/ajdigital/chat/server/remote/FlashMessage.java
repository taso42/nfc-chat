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

import com.lyrisoft.chat.server.remote.ConnectionHandler;
import com.lyrisoft.chat.server.remote.Message;

/**
 * Flash messages need to be wrapped in XML and ended with a null byte
 * 
 * @author <A HREF="mailto:alon@ajdigital.com">Alon Salant</A>
 */
public class FlashMessage extends Message {
    
    /**
     * Wrap in XML and add null byte
     * 
     * @param handler The connection handler that will send the message
     * @param message The raw message string
     */
    public FlashMessage(ConnectionHandler handler, String message) {
	super(handler, "<message value=\"" + message + "\"/>\u0000");
    }

}
