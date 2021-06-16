/*
 * Copyright (c) 2000 Lyrisoft Solutions, Inc.
 * Used by permission
 */
package com.lyrisoft.chat.server.remote;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.lyrisoft.chat.ICommands;
import com.lyrisoft.chat.IConnectionHandler;
import com.lyrisoft.chat.IConnectionListener;
import com.lyrisoft.chat.Translator;
import com.lyrisoft.chat.server.remote.persistence.auth.IAuthenticator;

/**
 * This is the representation of a Client, on the server side.
 * All the IChatClient interface methods are implemented by constructing a message
 * (with the help of CommandMakerRemote), and queuing it up to be sent to the client.
 *
 * @see CommandMakerRemote
 */
public class ChatClient implements IConnectionListener, ICommands {
    protected IConnectionHandler _connectionHandler;
    protected ChatServer _server;
    protected String  _userId;
    private int _accessLevel = IAuthenticator.NONE;
    private long _connectionTime;
    private String _host;
    private long _idle;
    private String _clientVersion;
    private HashMap _ignored; // users I'm ignoring
    private long _lastServerPing; // when was the last server ping (set by Vulture)
    private long _lastServerPong; // when was the last server pong (set by Pong)
    private long _lastServerPingDuration; // how big was the last ping
    private long _serverPingAvg; // what's the average ping time
    private long _serverPingsTotal; // the sum of all the ping durations
    private long _serverPingCount;  // how many times have we been pinged by the server 

    private boolean _tunneling = false;

    private String _key = "";

    void init(ChatServer server) {
        _server = server;
        _connectionTime = System.currentTimeMillis();
        _lastServerPong = _connectionTime;
        _idle = _connectionTime;
    }

    public ChatClient(ChatServer server, ServletConnectionHandler handler) {
        init(server);
        _tunneling = true;
        _connectionHandler = handler;
        handler.setListener(this);

        _host = handler.getHost() + " (" + Translator.getMessage("tunneling") + ")";
    }

    public ChatClient(ChatServer server, java.net.Socket s) throws java.io.IOException {
        init(server);
        _host = s.getInetAddress().getHostAddress();
        _connectionHandler = createConnectionHandler(s);
        ((ConnectionHandler)_connectionHandler).setDispatcher(server.getDispatcher());
        ((ConnectionHandler)_connectionHandler).init();
    }

    public IConnectionHandler createConnectionHandler(java.net.Socket s) throws java.io.IOException {
        return new ConnectionHandler(s, this);
    }        

    public boolean getTunneling() {
        return _tunneling;
    }
    
    public boolean isIgnoring(String userId) {
		return _ignored.containsKey(ChatServer.clientKey(userId));
    }
    
    public void ignoreList() {
    	String[] ignorees = new String[_ignored.size()];
    	int j = 0;
    	for (Iterator i = _ignored.values().iterator(); i.hasNext(); j++) {
    		ignorees[j] = (String)i.next();
    	}
		_connectionHandler.queueMessage(CommandMakerRemote.constructIgnoreListMessage(ignorees));
    }

    public boolean ignore(String client/*, String message*/) {
		// cannonization necessary for case-sensitive ignore storages
		client = _server.getClient(client).getUserId();

        String key = ChatServer.clientKey(client);
        if (_ignored.containsKey(key)) {
			generalMessage(Translator.getMessage("already_ignoring", client));
			return false;
        }

		if (!_server.isExistingUser(client)) {
			generalMessage(Translator.getMessage("cannot_ignore", client));
			return false;
		}

		_server.getIgnoreStore().ignore(_userId, client);
		_ignored.put(key, client); // need to store correct case as well
        return true;
    }

    public boolean unignore(String client) {
		String key = ChatServer.clientKey(client);
		if (!_ignored.containsKey(key)) {
			generalMessage(Translator.getMessage("cannot_unignore", client));
			return false;
        }

		_server.getIgnoreStore().unignore(_userId, (String)_ignored.get(key));
        _ignored.remove(key);
        return true;
    }

    /**
     * used by the vulture to determine if the client has timed out
     */
    public long getLastServerPong() {
        return _lastServerPong;
    }

    public long getLastServerPing() {
        return _lastServerPing;
    }

    public synchronized void setLastServerPing(long time) {
        _lastServerPing = time;
    }

    public synchronized void setLastServerPong(long time) {
        long now = System.currentTimeMillis();
        _lastServerPingDuration = now - time;
        _lastServerPong = now;

        _serverPingsTotal += _lastServerPingDuration;
        _serverPingCount++;
        _serverPingAvg = _serverPingsTotal / _serverPingCount;
    }

    public long getServerPingAvg() {
        return _serverPingAvg;
    }

    public long getServerPingCount() {
        return _serverPingCount;
    }

    /**
     * Get the idle time (in milliseconds) for this user.
     */
    public long getIdle() {
        return System.currentTimeMillis() - _idle;
    }

    /**
     * Get the time (in milliseconds) when the user logged in
     */
    public long getConnectionTime() {
        return _connectionTime;
    }

    /**
     * Get the access level for this user
     * @see IAuthenticator
     */
    public int getAccessLevel() {
        return _accessLevel;
    }

    /**
     * Get the version of the client
     * @return the version of the client
     */
    public String getClientVersion() {
        return _clientVersion;
    }

    /**
     * Set the version of the client
     * @param version the version
     */
    public void setClientVersion(String version) {
        _clientVersion = version;
    }

    /**
     * Get the host the user is connected from
     */
    public String getHost() {
        return _host;
    }

    /**
     * Set the access level for this user
     */
    public void setAccessLevel(int level) {
        _accessLevel = level;
    }

    /**
     * Get the user's id
     */
    public String getUserId() {
        return _userId;
    }

    /**
     * Set the user's id
     */
    public void setUserId(String userId) {
    	// it's possible for this to be called multiple times
    	// w/ same userId thanks to the way ChatServer & auth work.
    	if (_userId == null || !_userId.equals(userId)) {
	        _userId = userId;
	        _key = userId == null ? null : userId.toLowerCase();
			List savedIgnored = _server.getIgnoreStore().getIgnoredByUser(_userId);
			_ignored = new HashMap(savedIgnored.size());
			for (Iterator i = savedIgnored.iterator(); i.hasNext(); ) {
				String s = (String)i.next();
				_ignored.put(s.toLowerCase(), s);
			}
    	}
    }

    /**
     * Do not call this method.  the proper way to kill a connection is by
     * calling the SignOff method in ChatServer.  Only ChatServer calls this.
     */
    public void die() {
        _connectionHandler.shutdown(false);
    }

    public ChatServer getServer() {
        return _server;
    }

    /**
     * from the  IConnectionListener interface
     * delegated to CommandProcessorRemote.process()
     * @see CommandProcessorRemote#process
     */
    public void incomingMessage(String msg) {
        // deal with message
        boolean affectIdle = CommandProcessorRemote.process(msg, this);
        if (affectIdle) {
            _idle = System.currentTimeMillis();
        }
    }

    /**
     * from the IConnectionListener interface
     * call signOff on the server
     * @see ChatServer#signOff
     */
    public void socketClosed() {
        // tell the server that the user is gone
        _server.signOff(this);
    }


    /**
     * Construct and queue a message that will be sent back to the client
     */
    public void ackSignon(String myName) {
        if (_connectionHandler == null) {
            System.err.println("conn handler is null");
        }
        _connectionHandler.queueMessage(CommandMakerRemote.constructSignonAck(myName));
    }

    /**
     * Construct and queue a message that will be sent back to the client
     */
    public void connectionLost() {
        // no implementation.  if the connection is lost, we can't send a message back
    }

    /**
     * Construct and queue a message that will be sent back to the client
     */
    public void ackJoinRoom(String room) {
        _connectionHandler.queueMessage(CommandMakerRemote.constructJoinRoomAck(room));
    }

    /**
     * Construct and queue a message that will be sent back to the client
     */
    public void ackPartRoom(String room) {
        _connectionHandler.queueMessage(CommandMakerRemote.constructPartRoomAck(room));
    }

    /**
     * Construct and queue a message that will be sent back to the client
     */
    public void messageFromUser(String user, String room, String msg) {
        if (!isIgnoring(user)) {
            _connectionHandler.queueMessage(CommandMakerRemote.constructRoomMessage(user, room, msg));
        } 
    }
        
    /**
     * Construct and queue a message that will be sent back to the client
     */
    public void messageFromUserPrivate(String user, String msg) {
		if (!isIgnoring(user)) {
            _connectionHandler.queueMessage(CommandMakerRemote.constructPrivateMessage(user, msg));
        } else {
            ChatClient client = _server.getClient(user);
            if (client != null) {
                client.generalError(Translator.getMessage("is_ignoring", _userId));
            }
        }
    }

    /**
     * Construct and queue a message that will be sent back to the client
     */
    public void emoteFromUserPrivate(String user, String msg) {
        if (!isIgnoring(user)) {
            _connectionHandler.queueMessage(CommandMakerRemote.constructPrivateEmoteMessage(user, msg));
        } else {
            ChatClient client = _server.getClient(user);
            if (client != null) {
                client.generalError(Translator.getMessage("is_ignoring", _userId));
            }
        }
    }

    /**
     * Construct and queue a message that will be sent back to the client
     */
    public void roomList(String[] roomList) {
        _connectionHandler.queueMessage(CommandMakerRemote.constructRoomListMessage(roomList));
    }

    /**
     * Construct and queue a message that will be sent back to the client
     */
    public void globalUserList(String[] users) {
        _connectionHandler.queueMessage(CommandMakerRemote.constructGlobalUserListMessage(users));
    }

    /**
     * Construct and queue a message that will be sent back to the client
     */
    public void roomUserList(String room, String[] users) {
		_connectionHandler.queueMessage(CommandMakerRemote.constructRoomUserListMessage(room, users));
    }

    /**
     * Construct and queue a message that will be sent back to the client
     */
    public void userJoinedRoom(String user, String room) {
        _connectionHandler.queueMessage(CommandMakerRemote.constructUserJoinedRoomMessage(user, room));
    }

    /**
     * Construct and queue a message that will be sent back to the client
     */
    public void userPartedRoom(String user, String room, boolean signOff) {
        _connectionHandler.queueMessage(CommandMakerRemote.constructUserPartedRoomMessage(user, room, signOff));
    }

    /**
     * Construct and queue a message that will be sent back to the client
     */
    public void generalError(String message) {
        _connectionHandler.queueMessage(CommandMakerRemote.constructErrorMessage(message));
    }

    /**
     * Construct and queue a message that will be sent back to the client
     */
    public void generalMessage(String message) {
        _connectionHandler.queueMessage(CommandMakerRemote.constructGeneralMessage(message));
    }

    /**
     * Construct and queue a message that will be sent back to the client
     */
    public void generalRoomMessage(String room, String message) {
        _connectionHandler.queueMessage(CommandMakerRemote.constructGeneralRoomMessage(room, message));
    }

    /**
     * Construct and queue a message that will be sent back to the client
     */
    public void ping(String user, String arg) {
        _connectionHandler.queueMessage(CommandMakerRemote.constructPing(user, arg));
    }

    /**
     * Construct and queue a message that will be sent back to the client
     */
    public void pong(String user, String arg) {
        _connectionHandler.queueMessage(CommandMakerRemote.constructPong(user, arg));
    }

    public boolean equals(Object o) {
        return super.equals(o);
    }

    public int hashCode() {
        return super.hashCode();
    }

    /**
     * Create a RoomJoinError message and send it to the client.
     * @param error the error.
     * @param room the room.
     */
    public void roomJoinError(String error, String room) {
        _connectionHandler.queueMessage(CommandMakerRemote.constructRoomJoinError(error, room));
    }

    /**
     * Create a SignOnError message and send it to the client.
     * @param error the error.
     * @param user the user.
     */
    public void signOnError(String error, String user) {
        _connectionHandler.queueMessage(CommandMakerRemote.constructSignOnError(error, user));
    }

    /**
     * Create an Error message and send it to the client.
     * @param type the error type.
     * @param arg anything you'd want to add.
     */
    public void error(String type, String arg) {
        _connectionHandler.queueMessage(CommandMakerRemote.constructError(type, arg));
    }

    /**
     * Create a killed message and send it to the client.
     * @param killer the user that killed this ChatClient.
     * @param msg 
     */
    public void killed(String killer, String msg) {
        _connectionHandler.sendImmediately(CommandMakerRemote.constructKilled(killer, msg));
    }

    /**
     * Create an ackkill message and send it to the client. 
     * This confirms that this ChatClient made a kill.
     * @param victim the victim.
     */
    public void ackKill(String victim) {
        _connectionHandler.queueMessage(CommandMakerRemote.constructAckKill(victim));
    }

    /**
     * Create and send an emote to room message, if the user sending it is not on
     * this user's ignore list.
     * @param user the user that sent the emote.
     * @param room the room to which the emote was sent.
     * @param message the emote.
     */
    public void emote(String user, String room, String message) {
        if (!isIgnoring(user)) {
            _connectionHandler.queueMessage(CommandMakerRemote.constructEmote(user, room, message));
        }
    }

    /** 
     * Send a raw String over the socket.
     */
    public void sendRaw(String s) {
        _connectionHandler.queueMessage(s);
    }

    /**
     * Notify this user that another user has signed on.
     * @param userId the user that signed on.
     */
    public void userSignOn(String userId) {
        _connectionHandler.queueMessage(CommandMakerRemote.constructUserSignOn(userId));
    }

    /**
     * Notify this user that another user has signed off.
     * @param userId the user that signed off.
     */
    public void userSignOff(String userId) {
        _connectionHandler.queueMessage(CommandMakerRemote.constructUserSignOff(userId));
    }

    /** 
     * Notify this user that a room was created.
     * @param room the room that was created.
     */
    public void roomCreated(String room) {
        _connectionHandler.queueMessage(CommandMakerRemote.constructRoomCreated(room));
    }

    /** 
     * Notify this user that a room was destroyed.
     * @param room the room that was destroyed.
     */
    public void roomDestroyed(String room) {
        _connectionHandler.queueMessage(CommandMakerRemote.constructRoomDestroyed(room));
    }

    public void setAttribute(String name, Object value) {
    }

    public Object getAttribute(String name) {
        return null;
    }

    public String getKey() {
        return _key;
    }
}

