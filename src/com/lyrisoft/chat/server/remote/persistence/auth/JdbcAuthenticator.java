/*
 * Jdbc Authentication Module:
 * By Wayne Hogue <w.hogue@chiphead.net>
 * Based on 
 * Mysql Authentication Module:
 * com.lyrisoft.auth.mysql
 * By Leif Jackson <ljackson@jjcons.com>
 * 
 * Updated by Maarten van Hoof <m.s.hoof@chello.nl>, 19.11.2002
 * Changes:
 * - using a PreparedStatement now to increase performance and
 * to prevent people from inserting SQL into the password. Single 
 * quotes are automatically escaped in a PreparedStatement.
 * - created an 'allow guests' option that can be set to false
 * so that users not in the database will be denied access. By
 * default this option is on, so that the original behaviour of
 * this class has not changed.
 * - created a 'store guests' option to work in conjunction with 
 * 'allow guests'. When this option is set to true, guests are
 * stored in the database.
 * - Introduced encryption of passwords through MD5 digest.
 * It is now possible to use encrypted passwords in databases 
 * other than mySQL. 
 * 
 * Updated by Jonathan Ellis, 17.2.2003
 * Changes:
 * - more clean separation between jdbc & jdbcauthenticator
 * - r/m countStmt; no need to do two queries when one will do.
 * - selectStmt only returns non-redundant info
 * - r/m unnecessary class JdbcRecord; use Auth directly
 * 
 * Updated by Maarten van Hoof, 25.2.2003
 * Changes:
 * - put countStmt back: it differentiates between non-existent
 * users and existing users that enter an incorrect password
 * - Put some of the comments back in from my local copy.
 * todo: create a script to add users to the database.
 */

package com.lyrisoft.chat.server.remote.persistence.auth;

import java.security.MessageDigest;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.lyrisoft.chat.Translator;
import com.lyrisoft.chat.server.remote.AccessDenied;
import com.lyrisoft.chat.server.remote.ChatServer;
import com.lyrisoft.chat.server.remote.persistence.Jdbc;
import com.lyrisoft.util.properties.PropertyException;
import com.lyrisoft.util.properties.PropertyTool;

public class JdbcAuthenticator extends NullAuthenticator {
	
	/** No password encryption. */
	private static final int NONE = 0;
	/** Leave password encryption to the MySQL database. */
	private static final int MYSQL = 1;
	/** MD5 password encryption. */
	private static final int MD5 = 2;
	/** The encryption method to use. One of the static's below. Defaults to NONE. */
	private int _cryptMethod = NONE;
	
	/** The MessageDigest object that creates the digest (md5). */
	private MessageDigest _digest;
	/** The PreparedStatement to see whether a user is in the database. */
	private PreparedStatement _countStmt;
	/** The PreparedStatement that checks a user's password. */
	private PreparedStatement _selectStmt;
	/** The PreparedStatement that adds new users to the database. */
	private PreparedStatement _insertStmt;
	
	public JdbcAuthenticator(ChatServer server, boolean allowGuests, boolean storeGuests) {
		super(server, allowGuests, storeGuests);
		try {
			
			String table = PropertyTool.getString("jdbc.Table", Jdbc.p);
			String idField = PropertyTool.getString("jdbc.IdField", Jdbc.p);
			String passwordField = PropertyTool.getString("jdbc.PasswordField", Jdbc.p);
			String authField = PropertyTool.getString("jdbc.AuthField", Jdbc.p);
			
			// this ensures that old configuration files will still work.
			try {
				if(PropertyTool.getBoolean("jdbc.CryptPassword", Jdbc.p))
					_cryptMethod = MYSQL;
			}
			catch (PropertyException pe) {}
			// but the new key takes precedence over the old one
			try {
				String cType = PropertyTool.getString("jdbc.CryptMethod", Jdbc.p);
				if (cType.equalsIgnoreCase("md5")) {
					_cryptMethod = MD5;
					_digest = MessageDigest.getInstance("MD5");
				}
				else if (cType.equalsIgnoreCase("mySQL"))
					_cryptMethod = MYSQL;
			}
			catch (PropertyException pe) {}
			
			
			// create the prepared statement that counts.
			_countStmt = Jdbc.conn.prepareStatement("SELECT COUNT(*) FROM "+table+" WHERE lower("+idField+") = lower(?)");
			
			// create the prepared statement that checks the password.
			String query = "SELECT "+authField+" FROM "+table+" WHERE "+idField+"=? ";
			if(_cryptMethod == MYSQL)
				query += "AND "+passwordField+"=password(?)";
			else
				query += "AND "+passwordField+"=?";
			_selectStmt = Jdbc.conn.prepareStatement(query);
			
			// create the statement that inserts new users.
			query = "INSERT INTO "+table+" ("+idField+", "+passwordField+", "+authField+")";
			if(_cryptMethod == MYSQL)
				query = query + " VALUES (?, password(?), ?)";
			else
				query = query + " VALUES (?, ?, ?)";
			_insertStmt = Jdbc.conn.prepareStatement(query);

			if (_allowGuests) {
				if (_storeGuests)
					ChatServer.log("JDBC authentication initialized. Guest access allowed, adding guests to the database.");
				else
					ChatServer.log("JDBC authentication initialized. Guest access allowed, not adding guests to the database.");
			}
			else
				ChatServer.log("JDBC authentication initialized. No guest access allowed.");
			
		}
		catch (Exception e) {
			ChatServer.log(e);
			throw new RuntimeException(e.toString());
		}
	}
	
	/**
	 * Encrypt the given password with the currently selected encryption method.
	 * If the encryption method is NONE or MYSQL, just return the password itself.
	 */
	private String encrypt(String password) {
		if (password == null)
			password = "";
		if (_cryptMethod == MD5)
			return asHexString(_digest.digest(password.getBytes()));
		else
			return password;
	}

	/**
	 * Create a hexidecimal representation of the given byte array.
	 */
	private String asHexString(byte[] b) {
		StringBuffer buffer = new StringBuffer(b.length * 2);
		int i;

		for (i = 0; i < b.length; i++) {
			if (((int)b[i] & 0xff) < 0x10) 
				buffer.append("0");
			buffer.append(Long.toString(((int) b[i]) & 0xff, 16));
		}

	  	return buffer.toString();
	}
	
	// authenticator implementation
	
	/** checks all stored users besides just those currently logged on */
	public boolean isExistingUser(String userId)
	{
		return super.isExistingUser(userId) || isStoredUser(userId);
	}

	private synchronized final boolean isStoredUser(String userId) {
		boolean exists = false;
		try {
			_countStmt.setString(1, userId);
			ResultSet rs = _countStmt.executeQuery();
			if (rs.next()) {
				exists = rs.getInt(1) > 0;
			}
			rs.close();
		} catch (Exception e) {
			ChatServer.log(e);
		}
		return exists;
	}
	
	/**
	 * If a user if found in the users table, his password is checked.
	 * If a user is not found, the access level IAuthenticator.USER
	 * is returned if guests are allowed.
	 */
	public synchronized final Auth authenticate(String userId, String password) throws AccessDenied {

		ResultSet rs = null;
		
		try {
			_selectStmt.setString(1, userId);
			_selectStmt.setString(2, encrypt(password));
			rs = _selectStmt.executeQuery();
			ChatServer.log("Jdbc auth attempting " + userId );
			if(rs.next()) {// match
				return new Auth(userId, rs.getInt(1));
			} 
			else {// no match
				if (_allowGuests)  {
					Auth auth = super.authenticate(userId, password);
					if (_storeGuests) {
						// Add this guest to the database.
						_insertStmt.setString(1, auth.getUserId());
						_insertStmt.setString(2, encrypt(password));
						_insertStmt.setInt(3, IAuthenticator.USER);
						_insertStmt.executeUpdate();
					}
					return auth;
				}
				else
					// No guests allowed.
					throw new AccessDenied(userId);
			}
		} 
		catch (SQLException sqle) {
			ChatServer.log(sqle);
			ChatServer.logError("  SQLException: " + sqle.getMessage());
			ChatServer.logError("  SQLState:     " + sqle.getSQLState());
			ChatServer.logError("  VendorError:  " + sqle.getErrorCode());
			throw new AccessDenied(Translator.getMessage("sql_error", sqle.toString()));
		} 
		finally {
			// always Clean Up
			try {
				_countStmt.clearParameters();
				_selectStmt.clearParameters();
			}
			catch (SQLException nevermind) {}
			if (rs != null) {
				try {
					rs.close(); 
				} 
				catch (SQLException e) {
					ChatServer.log(e);
				}
			}
		}
	}
	
}
