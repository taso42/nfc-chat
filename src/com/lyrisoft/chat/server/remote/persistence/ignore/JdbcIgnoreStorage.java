/*
 * Copyright (c) 2000 Lyrisoft Solutions, Inc.
 * Used by permission
 */
package com.lyrisoft.chat.server.remote.persistence.ignore;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.lyrisoft.chat.server.remote.ChatServer;
import com.lyrisoft.chat.server.remote.persistence.Jdbc;

/** since the server only knows about usernames, you'll want
 * to set up a table referencing the actual primary key
 * and make a view of that, hardcoded here as nfc_users_ignoring_users,
 * that exposes usernames.  This way, when users
 * change their names, you're automatically covered.  See
 * contrib\ignore.sql for an example.
 * 
 * If you're using a database that doesn't allow views (or
 * triggers on views to handle the updates on the base table),
 * you'll have to write a custom IgnoreStorage and it will
 * be a lot uglier b/c this sort of thing really should be
 * handled by the DB, not the client.  Or, you
 * could use a real database like PostgreSQL, which is free. :)
 * 
 * the DB is also responsible for enforcing uniqueness in
 * the ignore mapping; again, this is best done by the DB
 * rather than with a second query from the client with the
 * overhead that entails.
 */

public class JdbcIgnoreStorage implements IIgnoreStorage {
	
	private PreparedStatement _ignorePS, _unignorePS, _getIgnoredPS;
	
	public JdbcIgnoreStorage() {
		try {
			String sql = "insert into nfc_users_ignoring_users (ignorer, ignoree) values (?, ?)";
			_ignorePS = Jdbc.conn.prepareStatement(sql);

			// no need to lcase these
			sql = "delete from nfc_users_ignoring_users where ignorer = ? and ignoree = ?";
			_unignorePS = Jdbc.conn.prepareStatement(sql);

			sql = "select ignoree from nfc_users_ignoring_users where ignorer = ?";
			_getIgnoredPS = Jdbc.conn.prepareStatement(sql);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public synchronized void ignore(String user, String userToIgnore) {
		try {
			_ignorePS.setString(1, user);
			_ignorePS.setString(2, userToIgnore);
			_ignorePS.executeUpdate();
		} catch (Exception e) {
			// we'll expect to see inserts rejected
			// if user tries to /ignore same guy twice.
			// however, most clients should be smarter than that...
			ChatServer.log(e);
		}
	}

	public synchronized void unignore(String user, String userIgnored) {
		try {
			_unignorePS.setString(1, user);
			_unignorePS.setString(2, userIgnored);
			_unignorePS.executeUpdate();
		} catch (Exception e) {
			ChatServer.log(e);
		}
	}

	public synchronized List getIgnoredByUser(String user) {
		// assuming most common case is no ignores
		ArrayList ignored = new ArrayList(0);

		try {
			_getIgnoredPS.setString(1, user);
			ResultSet rs = _getIgnoredPS.executeQuery();
			
			while (rs.next()) {
				ignored.add(rs.getString("ignoree"));
			}
		} catch (Exception e) {
			ChatServer.log(e);
		}

		return ignored;
	}
	
	
}
