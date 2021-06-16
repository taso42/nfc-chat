/*
 * LDAP Authenticator backend
 * By Marty Lee <marty@maui.co.uk>
 * 3rd March 2002
 * 
 * Refactored by Jonathan Ellis, Feb 2003
 */

package com.lyrisoft.chat.server.remote.persistence.auth;

import java.util.Hashtable;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import com.lyrisoft.chat.server.remote.AccessDenied;
import com.lyrisoft.chat.server.remote.ChatServer;
import com.lyrisoft.util.properties.PropertyTool;

/**
 * Backend that connects to an LDAP server for verification
 *
 */

public class LdapAuthenticator extends NullAuthenticator {

	Hashtable _env;

	String _userAttr;
	String _srchBase;
	String _serverURL;

	public LdapAuthenticator(ChatServer server, boolean allowGuests, boolean storeGuests) {
		super(server, allowGuests, storeGuests);

		_userAttr="uid";
		_srchBase="";
		_serverURL="ldap://localhost:389";

		_env = new Hashtable();

		_env.put(Context.INITIAL_CONTEXT_FACTORY,
				"com.sun.jndi.ldap.LdapCtxFactory");

		//ChatServer.log("LDAP Authenticator initialize");

		try {
			Properties p = PropertyTool.loadProperties("conf/ldapAuth.conf");
			try {
				String factory=stripQuotes(PropertyTool.getString("ldap.CONTEXT_FACTORY", p));
				_env.put(Context.INITIAL_CONTEXT_FACTORY,
					factory);
			} catch (Exception e1) {
				//ignore it
			}

			try {
				String bindDN=stripQuotes(PropertyTool.getString("ldap.bindDN", p));
				String bindPW=stripQuotes(PropertyTool.getString("ldap.bindPW", p));
				_env.put(Context.SECURITY_AUTHENTICATION, "simple");
				_env.put(Context.SECURITY_PRINCIPAL, bindDN);
				_env.put(Context.SECURITY_CREDENTIALS, bindPW);
			} catch (Exception e2) {
				//ignore it
			}

			try {
				String serverURL=stripQuotes(PropertyTool.getString("ldap.serverURL", p));
				_serverURL=serverURL;
			} catch (Exception e3) {
				//ignore it
			}

			try {
				String searchBase=stripQuotes(PropertyTool.getString("ldap.searchBase", p));
				_srchBase=searchBase;
			} catch (Exception e4) {
				//ignore it
			}

			try {
				String userAttr=stripQuotes(PropertyTool.getString("ldap.userAttr", p));
				_userAttr=userAttr;
			} catch (Exception e5) {
				//ignore it
			}

			//ChatServer.log("LDAP Properties loaded");

		} catch (Exception e) {
			// ignore it
			//ChatServer.log("LDAP Properties failed to load: "+e.getClass().getName());
		}
	}
	
	private NamingEnumeration getNamingEnumeration(String userId) 
	throws NamingException {
		DirContext dir = new InitialDirContext(_env);
		NamingEnumeration ne = getNamingEnumeration(userId, dir);
		dir.close();
		return ne;
	}

	private NamingEnumeration getNamingEnumeration(String userId, DirContext dir) 
	throws NamingException {
		NamingEnumeration results = null;
		
		// Lookup the user
		SearchControls cons = new SearchControls();
		cons.setSearchScope(SearchControls.SUBTREE_SCOPE);

		results =
			dir.search(_srchBase,
				"("+_userAttr+"="+userId+")",
				cons);

		return results;
	}
	
	private boolean isStoredUser(String userId)
	{
		try {
			return getNamingEnumeration(userId).hasMore();
		} catch(javax.naming.NamingException e) {
			ChatServer.log(e);
		}
		return false;
	}
				
	public boolean isExistingUser(String userId)
	{
		return super.isExistingUser(userId) || isStoredUser(userId);
	}

	public Auth authenticate(String userId, String password) 
	throws AccessDenied
	{
		//ChatServer.log("LDAP Authenticate "+userId);
		try {
			DirContext dir = new InitialDirContext(_env);
			NamingEnumeration results = getNamingEnumeration(userId, dir);	
	
			while(getNamingEnumeration(userId).hasMore()) {
				SearchResult sr = (SearchResult)results.next();

				String userDN = sr.getName();
				if(sr.isRelative() &&
					!_srchBase.equals("")) {
					userDN=userDN+","+_srchBase;
				}

				// try to connect as this user
				Hashtable userenv=new Hashtable(_env);

				userenv.put(Context.SECURITY_AUTHENTICATION,
						"simple");
				userenv.put(Context.SECURITY_PRINCIPAL,
						userDN);
				userenv.put(Context.SECURITY_CREDENTIALS,
						password);

				try {
					DirContext userbind =
						new InitialDirContext(userenv);
					userbind.close();
					results.close();
					dir.close();
					return new Auth(userId, USER);
				} catch(javax.naming.NamingException e) {
					// Ignore it
				}
				results.close();
				dir.close();
			}
		} catch(javax.naming.NamingException e) {
			ChatServer.log(e);
		}

		if (_allowGuests) {
			return super.authenticate(userId, password);
		}
		
		throw new AccessDenied(userId);
	}

	private String stripQuotes(String token) {

		// Remove quotes from around a string
		if(token.startsWith("\"")) {
			token=token.substring(1);
		}

		if(token.endsWith("\"")) {
			token=token.substring(0,token.length()-1);
		}

		return token;
	}
}
				
