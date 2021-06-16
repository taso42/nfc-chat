package com.lyrisoft.chat.server.remote.persistence;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

import com.lyrisoft.chat.server.remote.ChatServer;
import com.lyrisoft.util.properties.PropertyTool;

/**
 * This provides a Connection to our database persistence classes.
 * <p>
 * It reads its configuration
 * from the file jdbc.conf, which must be located in the conf directory.
 * Configuration options include the JDBC driver to use, the database URL.
 * See the file for details.
 */
public class Jdbc {

	/** The connection to the database. */
	public static Connection conn;

	public static Properties p;
	
	static {
		try {
			// Get the properties
			p = PropertyTool.loadProperties("conf/jdbc.conf");
			String JdbcClass = PropertyTool.getString("jdbc.Class", p);
			String dbUrl = PropertyTool.getString("jdbc.DBUrl", p);
			String dbUsername = PropertyTool.getString("jdbc.DBUsername", p);
			String dbPassword = PropertyTool.getString("jdbc.DBPassword", p);
			
			// load the driver
			Class.forName(JdbcClass).newInstance();
			
			if(dbUsername == null)
				conn = DriverManager.getConnection(dbUrl);
			else
				conn = DriverManager.getConnection(dbUrl,dbUsername,dbPassword);
			ChatServer.log("Jdbc connected successfully");
		}
		catch (Exception e) {
			ChatServer.log(e);
			throw new RuntimeException(e);
		}
	}
	
}
