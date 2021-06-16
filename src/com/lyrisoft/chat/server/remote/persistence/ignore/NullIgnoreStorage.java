/*
 * Copyright (c) 2000 Lyrisoft Solutions, Inc.
 * Used by permission
 */
package com.lyrisoft.chat.server.remote.persistence.ignore;

import java.util.ArrayList;
import java.util.List;

/**
 * Authenitcator that authenticates everybody
 */
public class NullIgnoreStorage implements IIgnoreStorage {
	private static ArrayList al = new ArrayList(0);
	
	public void ignore(String user, String userToIgnore) {}

	public void unignore(String user, String userIgnored) {}

	public List getIgnoredByUser(String user) {
		return al;
	}
	
}
