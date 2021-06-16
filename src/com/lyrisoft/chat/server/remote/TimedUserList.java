package com.lyrisoft.chat.server.remote;

import java.util.ArrayList;
import java.util.Iterator;

class TimedUserList {
    // we wrap instead of subclassing b/c we're only going to
    // provide a small subset of the Collection interface
	ArrayList _users = new ArrayList(2);
	int _seconds;

	public TimedUserList(int seconds) {
		_seconds = seconds;
	}
	
	public void add(String userId) {
		synchronized(_users) {
			_users.add(new UserRecord(userId));
		}
	}

	/** determines if userId is currently in list.
	 * Also prunes outdated UserRecords.
	 */
	public boolean contains(String userId) {
		long now = System.currentTimeMillis();
		synchronized (_users) {
			for (Iterator i = _users.iterator(); i.hasNext(); ) {
				UserRecord u = (UserRecord)i.next();
				if (now > u.whenAdded + _seconds * 1000) {
					i.remove();
				} else if (u.userId.equalsIgnoreCase(userId)) {
					return true;
				}
			}
		}
		return false;
	}
	
	/** records when users were kicked.  store uid instead of ChatClient
	 * in case users logs off and on again. */
	private class UserRecord {
		public String userId;
		public long whenAdded;
    	
		public UserRecord(String userId) {
			this.userId = userId;
			whenAdded = System.currentTimeMillis();
		}
	}
}