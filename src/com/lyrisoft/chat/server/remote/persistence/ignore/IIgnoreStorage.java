package com.lyrisoft.chat.server.remote.persistence.ignore;

import java.util.List;

public interface IIgnoreStorage {
	public void ignore(String user, String userToIgnore);

	public void unignore(String user, String userIgnored);

	public List getIgnoredByUser(String user);
	
}
