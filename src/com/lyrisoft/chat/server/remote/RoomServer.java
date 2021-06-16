/*
 * Copyright (c) 2000 Lyrisoft Solutions, Inc.
 * Used by permission
 */
package com.lyrisoft.chat.server.remote;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import com.lyrisoft.chat.Constants;
import com.lyrisoft.chat.ICommands;
import com.lyrisoft.chat.Translator;
import com.lyrisoft.chat.server.remote.persistence.auth.IAuthenticator;

/**
 * RoomServer.  This represents a room in the system, and is sort of a little server of
 * its own.
 */
public class RoomServer implements ICommands {
    protected String _roomName;
    protected ArrayList _users;
    protected HashMap _ops;
    protected TimedUserList _kickedUsers;

    protected ChatServer _server;
    private String _password;
//    private HashSet _invitations = new HashSet();

    public RoomServer(String name, ChatServer server) {
        _server = server;
        _roomName = name;
        _users = new ArrayList();
        _ops = new HashMap();
        _kickedUsers = new TimedUserList(_server.getKickBanSeconds());
    }

    public RoomServer(String name, String password, ChatServer server) {
    	this(name, server);
        _password = password;
    }
    
    public ChatClient getOldestClient() {
    	synchronized(_users) {
	    	return (_users.size() > 0) ? (ChatClient)_users.get(0) : null;
    	}
    }
    
    /**
     * Get the name of this room
     */
    public String getName() {
        return _roomName;
    }

    /**
     * Tells whether or not this room is empty
     * @return true if emtpy, false otherwise
     */
    public boolean isEmpty() {
        return _users.size() == 0;
    }

    /**
     * Send a general message to all the clients in this room
     * @param message the general message
     */
    public void broadcast(String message) {
        synchronized (_users) {
            for (Iterator i = _users.iterator(); i.hasNext(); ) {
                ChatClient rcpt = (ChatClient)i.next();
                rcpt.generalRoomMessage(_roomName, message);
            }
        }
    }

    /**
     * Send an emote to the room.
     * @param sender the person doing the emote
     * @param message the emote text
     */
    public void emote(ChatClient sender, String message) {
        emote(sender.getUserId(), message);
    }

    public void emote(String from, String message) {
        synchronized (_users) {
            for (Iterator i = _users.iterator(); i.hasNext(); ) {
                ChatClient rcpt = (ChatClient)i.next();
                rcpt.emote(from, _roomName, message);
            }
        }
    }

    /**
     * Say something to everyone in the room
     * @param sender the person doing the talking
     * @param message the message text
     */
    public void say(ChatClient sender, String message) {
        String from = sender.getUserId();
        say(from, message);
    }

    public void say(String from, String message) {
        synchronized (_users) {
            for (Iterator i = _users.iterator(); i.hasNext(); ) {
                ChatClient rcpt = (ChatClient)i.next();
                rcpt.messageFromUser(from, _roomName, message);
            }
        }
    }

    protected void notifyJoin(String userId) {
        synchronized (_users) {
            // notify the other people
            for (Iterator i = _users.iterator(); i.hasNext(); ) {
                ((ChatClient)i.next()).userJoinedRoom(userId, _roomName);
            }
        }
    }

    public void remoteJoin(String username, String password) {
        notifyJoin(username);
    }

    protected void notifyPart(String userId, boolean isSignoff) {
        synchronized (_users) {
            // inform the other users here that someone signed off
            ChatClient rcpt = null;
            for (Iterator i = _users.iterator(); i.hasNext(); ) {
                rcpt = (ChatClient)i.next();
                rcpt.userPartedRoom(userId, _roomName, isSignoff);
            }
        }
    }

    public void remotePart(String username, boolean isSignoff) {
        notifyPart(username, isSignoff);
    }
    
    /**
     * Add a user to this room.  All other room members are notified
     * @param client the client that is joining
     * @see ChatClient#userJoinedRoom
     */
    public void join(ChatClient client, String password) throws RoomJoinException {
        String userId = client.getUserId();
        int level = _server.getRoomAccessLevel(client, this);
        if (level == IAuthenticator.NONE
        	|| _kickedUsers.contains(userId)
        	|| (_password != null && !_password.equals(password))) {
            throw new RoomJoinException(ROOM_ACCESS_DENIED);
        }

		synchronized (_users) {
	        if (!_users.contains(client)) {
                notifyJoin(client.getUserId());

                // add the user to the hashtable
                _users.add(client);
                
                client.ackJoinRoom(_roomName);
			}
        }

		if (level >= IAuthenticator.MODERATOR) {
			op(client);
		} else {
			if (_ops.size() == 0) {
				ChatClient nextOp = _server.getRoomNextOp(this);
				if (nextOp != null) {
					op(nextOp);
				}
			}
		}
    }

    public void op(ChatClient client) {
    	op(null, client);
    }

    public void deop(ChatClient client) {
    	deop(null, client);
    }

    public void op(ChatClient op, ChatClient newOp) {
        synchronized (_ops) {
            if (op == null || _ops.keySet().contains(ChatServer.clientKey(op))) {
                if (!_ops.keySet().contains(ChatServer.clientKey(newOp))) {
                    _ops.put(ChatServer.clientKey(newOp), newOp);
                    newOp.generalMessage(Translator.getMessage("op.add.confirm", _roomName));
					String actor = (op == null) ? Constants.SERVER_NAME : op.getUserId();
                    broadcast(Translator.getMessage("op.add", actor, newOp.getUserId()));
                }
            } else {
                op.generalError(Translator.getMessage("op.denied"));
            }
        }
    }

    public void deop(ChatClient op, ChatClient newOp) {
        synchronized (_ops) {
            if (op == null || _ops.keySet().contains(ChatServer.clientKey(op))) {
                if (_ops.keySet().contains(ChatServer.clientKey(newOp))) {
                    _ops.remove(ChatServer.clientKey(newOp));
                    newOp.generalMessage(Translator.getMessage("op.remove.confirm", _roomName));
					String actor = (op == null) ? Constants.SERVER_NAME : op.getUserId();
                    broadcast(Translator.getMessage("op.remove", actor, newOp.getUserId()));
                }
                if (_ops.size() == 0) {
					ChatClient nextOp = _server.getRoomNextOp(this);
					if (nextOp != null) {
						op(nextOp);
					}
                }
            } else {
                op.generalError(Translator.getMessage("op.denied"));
            }
        }
    }

    /**
     * Remove a user from this room.  All other room members are notified
     * @param client the client that is leaving
     * @param isSignoff true if the client is signing off; false if the client is leaving only
     *                  this room.
     * @see ChatClient#userPartedRoom
     */
    public void part(ChatClient client, boolean isSignoff) {
    	String userId = client.getUserId();
        synchronized (_users) {
            if (_users.remove(client)) {
            	// we only send ack if not signing off.
            	if (_server.getClient(userId) != null) {
                    client.ackPartRoom(_roomName);
                }

                notifyPart(userId, isSignoff);
            }
        }
        deop(client);
    }

    public void kick(ChatClient kicker, String victim) {
        if (kicker.getAccessLevel() >= IAuthenticator.GOD
        	|| _ops.keySet().contains(ChatServer.clientKey(kicker)))
        {
            ChatClient victimClient = _server.getClient(victim);
            if (victimClient == null || !_users.contains(victimClient)) {
                kicker.error(NO_SUCH_USER, victim);
            } else {
            	_kickedUsers.add(victim);
                _server.partRoom(victimClient, _roomName);
                victimClient.generalMessage(Translator.getMessage("kicked", _roomName, kicker.getUserId()));
                broadcast(Translator.getMessage("kick.room.msg", kicker.getUserId(), victim));
            }
        }
    }
  
    /**
     * Get the number of users in this room
     */
    public int getUserCount() {
        return _users.size();
    }

    /**
     * Get a string array containing the names of all the users in this room
     */
    public String[] getUsers() {
		String[] names = new String[_users.size()];
        synchronized (_users) {
        	int j = 0;
            for (Iterator i = _users.iterator(); i.hasNext(); ) {
                String name = ((ChatClient)i.next()).getUserId();
                names[j++] = name;
            }
        }
        return names;
    }

    public void invite(ChatClient inviter, String invitee) {
/*
        if (inviter == _owner) {
            synchronized (_invitations) {
                _invitations.add(ChatServer.clientKey(invitee));
            }
        } else {
            inviter.error(ACCESS_DENIED, ICommands.INVITE + " " + invitee);
        }
*/
    }

    public void uninvite(ChatClient inviter, String invitee) {
/*
        if (inviter == _owner) {
            synchronized (_invitations) {
                _invitations.remove(ChatServer.clientKey(invitee));
            }
        } else {
            inviter.error(ACCESS_DENIED, ICommands.UNINVITE + " " + invitee);
        }
*/
    }
    
}



