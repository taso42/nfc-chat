/*
 * Copyright (c) 2000 Lyrisoft Solutions, Inc.
 * Used by permission
 */
package com.lyrisoft.chat.server.remote.persistence.auth.room;
import java.sql.ResultSet;

import com.lyrisoft.chat.server.remote.ChatClient;
import com.lyrisoft.chat.server.remote.ChatServer;
import com.lyrisoft.chat.server.remote.RoomServer;
import com.lyrisoft.chat.server.remote.persistence.Jdbc;
import com.lyrisoft.chat.server.remote.persistence.JdbcStatementTemplate;
import com.lyrisoft.chat.server.remote.persistence.auth.IAuthenticator;

/**
 * Interface for objects that will handle authentication.  The server will delegate signOn
 * calls to us.
 */
public class JdbcRoomAuthenticator extends NullRoomAuthenticator {
	private JdbcStatementTemplate _createAllowed
		= new JdbcStatementTemplate(Jdbc.conn, new String[] {":username", ":roomname", ":password"});
	private JdbcStatementTemplate _opRequired
		= new JdbcStatementTemplate(Jdbc.conn, new String[] {":roomname"});
	private JdbcStatementTemplate _accessLevel
		= new JdbcStatementTemplate(Jdbc.conn, new String[] {":username", ":roomname"});
		
	public JdbcRoomAuthenticator() {
		_createAllowed.parse(Jdbc.p.getProperty("jdbc.isCreateAllowed"));
		_opRequired.parse(Jdbc.p.getProperty("jdbc.isOpRequired"));
		_accessLevel.parse(Jdbc.p.getProperty("jdbc.getRoomAccessLevel"));
	}
	
	public boolean isCreateAllowed(ChatClient client, String room, String password) {
		boolean b = true;
		try {
			ResultSet rs = _createAllowed.bind(new Object[] {client.getUserId(), room, password}).executeQuery();
			rs.next();
			b = rs.getBoolean(1);
			rs.close();
		} catch (Exception e) {
			ChatServer.log(e);
		}
		return b;
	}
    
    public int getAccessLevel(ChatClient client, RoomServer room) {
    	if (client.getAccessLevel() >= IAuthenticator.GOD) {
    		return client.getAccessLevel();
    	}
    	
    	int access = IAuthenticator.USER;
		try {
			ResultSet rs = _accessLevel.bind(new Object[] {client.getUserId(), room.getName()}).executeQuery();
			rs.next();
			access = rs.getInt(1);
			rs.close();
		} catch (Exception e) {
			ChatServer.log(e);
		}
		
		return access;
    }
    
    public ChatClient getNextOp(RoomServer room) {
		boolean required = true;
		try {
			ResultSet rs = _opRequired.bind(new Object[] {room.getName()}).executeQuery();
			rs.next();
			required = rs.getBoolean(1);
			rs.close();
		} catch (Exception e) {
			ChatServer.log(e);
		}

		if (!required) {
			return null;
		}
		return super.getNextOp(room);
    }
}
