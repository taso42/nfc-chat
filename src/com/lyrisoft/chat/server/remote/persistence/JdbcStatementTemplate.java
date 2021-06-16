package com.lyrisoft.chat.server.remote.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.lyrisoft.chat.server.remote.ChatServer;

public class JdbcStatementTemplate {
	private static final Pattern _p = Pattern.compile(":.+?\\b");
	
	private String[] _varNames;
	private PreparedStatement _ps;
	private Connection _conn;
	private HashMap _varMap;
	
	public JdbcStatementTemplate(Connection c, String[] varNames) {
		_varNames = varNames;
		_conn = c;
	}
	
	public void parse(String sql) {
		_varMap = new HashMap(5);
		try {
			ChatServer.log("Parsing " + sql);
			Matcher m = _p.matcher(sql);
			for (int i = 1; m.find(); i++) {
				String var = sql.substring(m.start(), m.end());
				_varMap.put(var, new Integer(i));
			}
			String parsedSql = m.replaceAll("?");
			_ps = _conn.prepareStatement(parsedSql);
			ChatServer.log("Parsed to " + parsedSql);
		} catch (Exception e) {
			ChatServer.log(e);
		}
	}
	
	public PreparedStatement bind(Object[] vars) {
		try {
			for (int i = 0; i < _varNames.length; i++) {
				Integer j = (Integer)_varMap.get(_varNames[i]);
				if (j != null) {
					_ps.setObject(j.intValue(), vars[i]);
				}
			}
		} catch (Exception e) {
			ChatServer.log(e);
		}
		return _ps;
	}
}