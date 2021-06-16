/*
 * Copyright (c) 2000 Lyrisoft Solutions, Inc.
 * Used by permission
 */
package com.lyrisoft.chat.server.remote;

import java.io.PrintStream;
import java.util.Collection;
import java.util.Iterator;

/**
 * Encapulates the state of the whole distributed system.
 * Each server will have its own distributed state object
 */
// note, some methods are called from a synchronized (ChatServer._users) block.
// this means, none of these methods can call anything else
// that syncs on _users *after* syncing on _serverTable (or _roomTable), or
// you could end up with deadlock.
public class DistributedState {
    private RoomTable _roomTable;
    private ServerTable _serverTable;
    private ChatServer _server;

    public DistributedState(ChatServer server) {
        _server = server;
        _serverTable = new ServerTable(server);
        _roomTable = new RoomTable(server);
    }
    
    public Collection getUsersOnServer(String server) {
        synchronized(_serverTable) {
			return _serverTable.getUsers(server);
		}
    }

    public Collection getAllUsers() {
        synchronized(_serverTable) {
			return _serverTable.getAllUsers();
		}
    }

    public Collection getAllRooms() {
        synchronized(_roomTable) {
			return _roomTable.getRoomNames();
		}
    }

    public Collection getUsersInRoom(String roomname) {
        synchronized(_roomTable) {
			return _roomTable.getUsers(roomname);
		}
    }

    public Collection getAllServers() {
        synchronized(_serverTable) {
			return _serverTable.getServerNames();
		}
    }

    public void signon(String server, String user) {
        synchronized(_serverTable) {
			_serverTable.signon(server, user);
		}
    }

    /**
     * @return a Collection of rooms that have been destroyed as a result of the signoff
     */
    public Collection signoff(String server, String user) {
        synchronized(_serverTable) {
			_serverTable.signoff(server, user);
			synchronized(_roomTable) {
				return _roomTable.signoff(user);
			}
		}
    }

    public boolean userExists(String username) {
        synchronized(_serverTable) {
			return _serverTable.userExists(username);
		}
    }

    public boolean userExistsInRoom(String username, String roomname) {
        synchronized(_roomTable) {
			return _roomTable.userExistsInRoom(username, roomname);
		}
    }

    public boolean serverExists(String servername) {
        synchronized(_serverTable) {
			return _serverTable.serverExists(servername);
		}
    }

    public boolean roomExists(String roomname) {
        synchronized(_roomTable) {
			return _roomTable.roomExists(roomname);
		}
    }

    public void checkServerPings(long timeout) {
        synchronized(_serverTable) {
			_serverTable.checkServerPings(timeout);
		}
    }

    /**
     * @return true if this room is newly created
     */
    public boolean join(String server, String room, String user) {
        synchronized(_roomTable) {
			return _roomTable.join(room, user);
		}
    }

    /**
     * @return true if the room is destroyed
     */
    public boolean part(String server, String room, String user) {
        synchronized(_roomTable) {
			return _roomTable.part(room, user);
		}
    }

    public void addServer(String server) {
        synchronized(_serverTable) {
			_serverTable.add(server);
		}
    }

    public void deleteServer(String server) {
        synchronized(_serverTable) {
			// first get rid of any dead users (and possibly dead rooms)
			Collection deadUsers = _serverTable.getUsers(server);
			for (Iterator i = deadUsers.iterator(); i.hasNext(); ) {
				String user = (String)i.next();
				_server.remoteSignOff(server, user);
			}
			_serverTable.delete(server);
		}
    }

    public int countUsersOnServer(String server) {
        synchronized(_serverTable) {
			return _serverTable.countUsers(server);
		}
    }

    public int countUsersInRoom(String room) {
        synchronized(_roomTable) {
			return _roomTable.countUsers(room);
		}
    }

    public void setLastBroadcastPing(String server, long time) {
        synchronized(_serverTable) {
			_serverTable.setLastBroadcastPing(server, time);
		}
    }

    public void dumpServerTable(PrintStream out) {
        _serverTable.dump(out);
    }

    public void dumpRoomTable(PrintStream out) {
        _roomTable.dump(out);
    }

}




