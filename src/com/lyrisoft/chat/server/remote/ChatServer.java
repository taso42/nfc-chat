/*
 * Copyright (c) 2000 Lyrisoft Solutions, Inc.
 * Used by permission
 */
package com.lyrisoft.chat.server.remote;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.jms.JMSException;
import javax.jms.TextMessage;
import javax.servlet.ServletContext;

import com.lyrisoft.chat.Constants;
import com.lyrisoft.chat.ICommands;
import com.lyrisoft.chat.Translator;
import com.lyrisoft.chat.server.remote.persistence.auth.Auth;
import com.lyrisoft.chat.server.remote.persistence.auth.IAuthenticator;
import com.lyrisoft.chat.server.remote.persistence.auth.room.IRoomAuthenticator;
import com.lyrisoft.chat.server.remote.persistence.ignore.IIgnoreStorage;
import com.lyrisoft.util.io.Logger;
import com.lyrisoft.util.io.ResourceException;
import com.lyrisoft.util.io.ResourceLoader;
import com.lyrisoft.util.properties.PropertyException;
import com.lyrisoft.util.properties.PropertyTool;

/**
 * This is the Chat Server
 */
public class ChatServer implements ICommands, IServerCommands {
    public static final boolean DEBUG = getDebug();

    protected static Logger logger;
    protected static Logger errorLogger;

    protected int _port;
    protected ServerSocket _serverSocket;
    protected boolean _keepGoing = true;

    protected long _creationTime;
	protected IAuthenticator _authenticator;
	protected IRoomAuthenticator _roomAuthenticator; 
	protected IIgnoreStorage _ignoreStore;
    protected HashMap _users;
    protected HashMap _rooms;
    protected String _motd;
    protected Vulture _vulture;
    protected Thread  _vultureThread;

    protected Dispatcher _dispatcher;
    protected Thread _dispatchThread;

    protected Distributor _distributor;
    protected Thread _distributorThread;

    protected DistributedState _distributedState = new DistributedState(this);

    protected int _cumulativeLogins = 0;
    
	protected int _kickBanSeconds;
	protected int _killBanMinutes;
	
	protected TimedUserList _killedUsers;

    protected ServletContext _servletContext;

    public ChatServer(String conf, ServletContext context) throws Exception {
        _servletContext = context;
        _dispatcher = createDispatcher();
        _dispatchThread = new Thread(_dispatcher, "Dispatcher");
        _dispatchThread.start();

        Properties p = PropertyTool.loadProperties(conf);
        preProcessProperties(p);

        String logFile = null;
        String errorLogFile = null;
        logFile = p.getProperty("log.file");
        errorLogFile = p.getProperty("error.log.file");
        createLoggers(logFile, errorLogFile);
        
		String authClassName = PropertyTool.getString("auth.class", p);
		String roomAuthClassName = PropertyTool.getString("roomAuth.class", p);
		String ignoreClassName = PropertyTool.getString("ignoreStore.class", p);
        
        String commands = PropertyTool.getString("commands", p);
        Properties commandProps = PropertyTool.loadProperties(commands);
        initCommandProcessor(commandProps);
        
        String messages = PropertyTool.getString("messages", p);
        Properties messagesProps = PropertyTool.loadProperties(messages);
        initTranslator(messagesProps);

        _port = PropertyTool.getInteger("listen.port", p);

		boolean allowGuests = PropertyTool.getBoolean("auth.allowGuests", p);
		boolean storeGuests = PropertyTool.getBoolean("auth.storeGuests", p);
		Class authClass = Class.forName(authClassName);
		Class[] args = new Class[] {ChatServer.class, boolean.class, boolean.class};
		Constructor c = authClass.getConstructor(args);
		_authenticator = (IAuthenticator)c.newInstance(new Object[] {this, new Boolean(allowGuests), new Boolean(storeGuests)});

		_roomAuthenticator = (IRoomAuthenticator)Class.forName(roomAuthClassName).newInstance();

		_ignoreStore = (IIgnoreStorage)Class.forName(ignoreClassName).newInstance();

        String sIdle = p.getProperty("idle.timeout");
        double idleTimeout = Double.valueOf(sIdle).doubleValue();
        _vulture = new Vulture(this, idleTimeout);

		String sKBS = p.getProperty("kick.banseconds");
		_kickBanSeconds = Integer.valueOf(sKBS).intValue();

		String sKBM = p.getProperty("kill.banminutes");
		_killBanMinutes = Integer.valueOf(sKBM).intValue();

		_killedUsers = new TimedUserList(_killBanMinutes * 60);
		
		readMotd();
        _creationTime = System.currentTimeMillis();
        _users = new HashMap();
        _rooms = new HashMap();
        if (System.getProperty("BSD_HACK") != null) {
            log("FreeBSD Workaround is enabled.");
        }
        
        boolean useJms = false;
        try {
            useJms = PropertyTool.getBoolean("jms.enabled", p);
        }
        catch (PropertyException e) {}
        if (useJms) {
            _distributor = new Distributor(this, p);
            _distributorThread = new Thread(_distributor, "Distributor");
            _distributorThread.start();
        } else {
            log("Not distributing messages over JMS");
        }

        _serverSocket = new ServerSocket(_port);

        _vultureThread = new Thread(_vulture, "Vulture");
        _vultureThread.start();
    }

    public ChatServer(ServletContext context) throws Exception {
        this("conf/nfc.conf", context);
    }

    public ChatServer(String conf) throws Exception {
        this(conf, null);
    }

    public ChatServer() throws Exception {
        this((ServletContext)null);
    }
    
    public int getKickBanSeconds() {
    	return _kickBanSeconds;
    }
    
    public int getRoomAccessLevel(ChatClient client, RoomServer rs) {
		return _roomAuthenticator.getAccessLevel(client, rs);
    }
    
    public ChatClient getRoomNextOp(RoomServer rs) {
		return _roomAuthenticator.getNextOp(rs);
    }
    
    public boolean isExistingUser(String user) {
    	return _authenticator.isExistingUser(user);
    }

    public int getPort() {
        return _port;
    }

    /**
     * Set the ServletContext, if there is one.  The TunnelServlet calls this.
     */
    void setServletContext(ServletContext context) {
        _servletContext = context;
    }

    /**
     * A hook for reading in your own extra nfc.conf properties
     */
    public void preProcessProperties(Properties p) throws PropertyException {
    }
    
    public int getCumulativeLogins() {
        return _cumulativeLogins;
    }

    protected Dispatcher createDispatcher() {
        return new Dispatcher();
    }

    public Dispatcher getDispatcher() {
        return _dispatcher;
    }

    public void initTranslator(Properties p) {
        Translator.init(p);
    }

    public void initCommandProcessor(Properties p) {
        CommandProcessorRemote.init(p);
    }

    void distributorDisconnected(Distributor d) {
    }

    void distributorConnected(Distributor d) {
        announcePresence();
    }

    protected void readMotd() throws ResourceException, IOException {
        BufferedReader reader = null;
        StringBuffer sb = new StringBuffer();
        try {
            reader = new BufferedReader(new InputStreamReader(ResourceLoader.getResource("conf/nfc.motd")));
            String next;
            while ((next = reader.readLine()) != null) {
                sb.append(next);
                sb.append("\n");
            }
            _motd = sb.toString();
        }
        finally {
            if (reader != null) {
                reader.close();
            }
        }
    }

    /**
     * Log a message (goes to the standard log)
     */
    public static void log(String message) {
        logger.log(message);
    }

    /**
     * Log an error (goes to the error log)
     */
    public static void logError(String message) {
        errorLogger.log(message);
    }

    /**
     * Log an exception (goes to the error log)
     */
    public static void log(Exception e) {
        errorLogger.log(e);
    }

    /**
     * Get the RoomServer for a particular room
    */
    public RoomServer getRoom(String roomName) {
        return (RoomServer)_rooms.get(roomKey(roomName));
    }

    /**
     * Get a ChatClient by name
     */
    public ChatClient getClient(String clientName) {
        return (ChatClient)_users.get(clientKey(clientName));
    }

    /**
     * Determines if a user id is valid.  Ensures that the name is made up of
     * alphanumerical characters and contains no spaces.
     */
    protected void validateUserId(String user) throws AccessException {
        if (getName().toLowerCase().equals(user.toLowerCase())) {
            throw new AccessException(Translator.getMessage("reserved_name", getName()));
        } else if (Constants.SERVER_NAME.equalsIgnoreCase(user)) {
            throw new AccessException(Translator.getMessage("reserved_name", Constants.SERVER_NAME));
        }

        char[] chars = user.toLowerCase().toCharArray();
        for (int i=0; i < chars.length; i++) {
			char ch = chars[i];
            if (!(Character.isLetterOrDigit(chars[i])
            	  || ch == '_' || ch == '-'
				  || ch == '\\' || ch == '^'
				  || ch == '`' || ch == '|'
				  || ch == '[' || ch == '{'
				  || ch == ']' || ch == '}'
				  || ch == '(' || ch == ')'
				  || ch == '\'')
            	  ) {
                throw new AccessException(INVALID_CHARACTER);
            }
        }
    }

    public Map getLocalUsers() {
        return _users;
    }

    public void serverSignOn(String server) {
        _distributedState.addServer(server);
    }

    public void serverSignOff(String server) {
        _distributedState.deleteServer(server);
    }

    public void setRemoteUserList(String server, String[] users) {
        Collection oldUsers = _distributedState.getUsersOnServer(server);
        HashMap newMap = new HashMap();

        // copy the ORIGINAL user list for this server into a map
        for (Iterator i = oldUsers.iterator(); i.hasNext(); ) {
            String s = (String)i.next();
            newMap.put(clientKey(s), s);
        }
        
        // go through the NEW user list.  call remoteSignOn for each
        // new user, and remove any already-known users from
        // the map
        for (int i=0; i < users.length; i++) {
            String key = clientKey(users[i]);
            if (newMap.get(key) == null) {
                // user is brand new.  call remoteSignOn
                remoteSignOn(server, users[i]);
            } else {
                // user is already known.  do nothing
                newMap.remove(key);
            }
        }
        
        // now the map contains users which no longer exist, so we remove them
        // -- TL: I'm not sure if the map will ever have anything left in it,
        // but this doesn't hurt.
        for (Iterator i = oldUsers.iterator(); i.hasNext(); ) {
            remoteSignOff(server, (String)i.next());
        }

    }

    public void remoteSignOn(String server, String userId) {
        // XXX check if user exists locally first, and if so we must notify
        _distributedState.signon(server, userId);
    }

    public void remoteSignOff(String server, String userId) {
        synchronized (_rooms) {
            for (Iterator i = _rooms.values().iterator(); i.hasNext(); ) {
                RoomServer room = (RoomServer)i.next();
                if (_distributedState.userExistsInRoom(userId, room.getName())) {
                    remotePartRoom(server, userId, room.getName(), true);
                }
            }
        }

        Collection deadRooms = _distributedState.signoff(server, userId);
    }

    /**
     * Sign on to the server
     * @param client the ChatClient
     * @param password
     * @exception AccessException is the login failed
     */
    public void signOn(ChatClient client, String password) throws AccessException, AccessDenied {
        log(client.getUserId() + " is attempting a signon");
        String userId = client.getUserId();
        validateUserId(userId);
        Auth auth = _authenticator.authenticate(userId, password);
		int access = auth.getAccess();
		if (access == IAuthenticator.NONE
			|| _killedUsers.contains(userId)) 
		{
			throw new AccessException(ACCESS_DENIED);
		} else {
			log(client.getUserId() + " is authenticated.  Access = " + access + 
				(client.getTunneling() ? " (tunneling)" : ""));
		}
        client.setUserId(auth.getUserId());
        synchronized (_users) {
        	// if signed on locally, let new take precedence
        	ChatClient oldC = (ChatClient)_users.get(clientKey(client));
	       	if (oldC != null) {
	       		oldC.killed(client.getUserId(), "Terminated by signing on elsewhere");
	       		signOff(oldC);
	       	}
	       	// if signed on somewhere else, currently we have
	       	// no method to get rid of that so we have to reject login
            if (_distributedState.userExists(clientKey(client))) {
				throw new AccessException(ALREADY_SIGNED_ON);
            } else {
                client.setAccessLevel(access);
                _users.put(clientKey(client), client);
                _cumulativeLogins++;
            }
        }
        client.ackSignon(auth.getUserId());
        sendMotd(client);
        
        _distributedState.signon(getName(), userId);
/*        broadcastSignOnToUsers(userId); */
    }

    void broadcastSignOnToUsers(String userId) {
        synchronized (_users) {
            for (Iterator i = _users.values().iterator(); i.hasNext(); ) {
                ChatClient cc = (ChatClient)i.next();
                if (!cc.getUserId().equals(cc)) {
                    cc.userSignOn(userId);
                }
            }
        }
    }

    void broadcastSignOffToUsers(String userId) {
        synchronized (_users) {
            for (Iterator i = _users.values().iterator(); i.hasNext(); ) {
                ChatClient cc = (ChatClient)i.next();
                cc.userSignOff(userId);
            }
        }
    }

    public void broadcast(String s) {
        synchronized (_users) {
            for (Iterator i = _users.values().iterator(); i.hasNext(); ) {
                ChatClient client = (ChatClient)i.next();
                client.sendRaw(s);
            }
        }
    }

    protected void sendMotd(ChatClient client) {
        StringTokenizer st = new StringTokenizer(_motd, "\r\n");
        while (st.hasMoreTokens()) {
            client.generalMessage(st.nextToken());
        }
    }

	/**
	 * send user list of stored ignores
	 */
	public void ignoreList(ChatClient client) {
		client.ignoreList();
	}

    /**
     * Kick a user off the system.
     */
    public void kill(String victim, ChatClient killer, String message) {
        ChatClient c = getClient(victim);
        if (c == null) {
            killer.error(NO_SUCH_USER, victim);
        } else {
            if (killer.getAccessLevel() < IAuthenticator.GOD) {
                killer.error(ACCESS_DENIED, KILL + " " + victim);
            } else {
            	_killedUsers.add(c.getUserId());
                c.killed(killer.getUserId(), message);
                signOff(c);
                killer.ackKill(victim);
            }
        }
    }

    /**
     * Sign off of the system.  This is called by the ChatClient when the socket unexpectedly
     * closes, or when the user quits.
     */
    public void signOff(ChatClient client) {
        if (client.getAccessLevel() == IAuthenticator.NONE) {
            // not authenticated means not logged in.
            // just kill the client and return
            log("signoff: anonymous user");
            client.die();
            _vulture.removeClient(client);
            return;
        }

        log("signoff: " + client.getUserId());
            
        Object o = null;
        synchronized (_users) {
            o = _users.remove(clientKey(client));
            // sync this w/ _users so signOn doesn't see
            // it gone from _users, but still there in _dS
			Collection deadRooms = _distributedState.signoff(getName(), client.getUserId());
        }
        _vulture.removeClient(client);
        if (o != null) {
            // inform all the rooms that this user is gone
            synchronized (_rooms) {
                for (Iterator i = _rooms.values().iterator(); i.hasNext(); ) {
                    RoomServer room = (RoomServer)i.next();
                    room.part(client, true);
                    if (room.isEmpty()) {
                        log("Removing empty room: " + room.getName());
                        i.remove();
                    }
                }
            }
        }

        client.die();

        // notify the rest of the world (because the SignOff command processor is not
        // guaranteed to execute.. For instance, if the user crashed without signing out)
        distribute(client, SIGNOFF);
    }

    /**
     * Remote a user from a room
     */
    public void partRoom(ChatClient client, String roomName) {
        String key = roomKey(roomName);
        boolean empty = false;
        synchronized (_rooms) {
            RoomServer room = (RoomServer)_rooms.get(key);
            if (room != null) {
                room.part(client, false);
                if (room.isEmpty()) {
                    empty = true;
                    log("Removing empty room: " + roomName);
                    _rooms.remove(roomKey(room));
                    room = null;
                }
            }
        }

        _distributedState.part(getName(), roomName, client.getUserId());
    }
    
    public void notifyClientsRoomDestroyed(String roomName) {
        synchronized (_users) {
            for (Iterator i = _users.values().iterator(); i.hasNext(); ) {
                ChatClient cc = (ChatClient)i.next();
                cc.roomDestroyed(roomName);
            }
        }
    }

    public void remoteJoinRoom(String server, String username, String roomname, String password) {
        boolean created = _distributedState.join(server, roomname, username);
        RoomServer room = (RoomServer)_rooms.get(roomKey(roomname));
        if (room != null) {
            room.remoteJoin(username, password);
        }
    }

    public void remotePartRoom(String server, String username, String roomname, 
                               boolean isSignoff) 
    {
        boolean destroyed = _distributedState.part(server, roomname, username);
        RoomServer room = (RoomServer)_rooms.get(roomKey(roomname));
        if (room != null) {
            room.remotePart(username, isSignoff);
        }
    }

	public void joinRoom(ChatClient client, String roomName, String password)
	throws RoomJoinException {
		String key = roomKey(roomName);
		RoomServer room = (RoomServer) _rooms.get(key);
		synchronized (_rooms) {
			if (room == null) {
				if (!_roomAuthenticator.isCreateAllowed(client, roomName, password)) {
					throw new RoomJoinException(ICommands.ROOM_ACCESS_DENIED);
				}
				room = (RoomServer) _rooms.get(roomKey(roomName));
				if (room == null) {
					log(client.getUserId() + " created new room: " + roomName);
					room = createRoomServer(roomName, password);
					_rooms.put(roomKey(room), room);
				}
			}
		}
		room.join(client, password);

		_distributedState.join(getName(), room.getName(), client.getUserId());
	}

    public void notifyClientsRoomCreated(String roomName) {
        synchronized (_users) {
            for (Iterator i = _users.values().iterator(); i.hasNext(); ) {
                ChatClient cc = (ChatClient)i.next();
                cc.roomCreated(roomName);
            }
        }
    }

    public RoomServer createRoomServer(String roomName, String password) {
        return new RoomServer(roomName, password, this);
    }

    /**
     * returns a key for referencing the rooms hashmap.  mainly to avoid
     * case-sensitivity problems.
     */
    public static String roomKey(String room) {
        return room.toLowerCase();
    }

    /**
     * returns a key for referencing the rooms hashmap.  mainly to avoid
     * case-sensitivity problems.
     */
    public static String roomKey(RoomServer room) {
        return roomKey(room.getName());
    }

    /**
     * returns a key for referencing the users hashmap.  mainly to avoid
     * case-sensitivity problems.
     */
    public static String clientKey(ChatClient client) {
        return client.getKey();
    }

    /**
     * returns a key for referencing the users hashmap.  mainly to avoid
     * case-sensitivity problems.
     */
    public static String clientKey(String client) {
        return client.toLowerCase();
    }

    /**
     * Tells the server to begin accepting connections.
     */
    protected void acceptConnections() {
//        System.err.println("Chat Server listening on port " + _port);
        log("Accepting socket connections on port " + _port);
        while (_keepGoing) {
            try {
                Socket s = _serverSocket.accept();
                s.setTcpNoDelay(true); // couldn't hurt
                log("Got a connection from " + s.getInetAddress().getHostAddress());
                ChatClient client = createChatClient(s);
                _vulture.addClient(client);
            }
            catch (IOException e) {
                log(e);
                try {
                    Thread.sleep(1000);
                }
                catch (InterruptedException ex) {
                }
            }
        }
    }

    protected ChatClient createChatClient(Socket s) throws IOException {
        return new ChatClient(this, s);
    }

    public void pleaseStop() {
        _keepGoing = false;
    }

    /**
     * Get the names of all the rooms on this server
     * @return an array contains the names of all the rooms
     */
    public String[] getRoomNames() {
        Collection c = _distributedState.getAllRooms();
        String[] rooms = new String[c.size()];
        c.toArray(rooms);
        return rooms;
    }

    public String[] getLocalRoomNames() {
        ArrayList al = new ArrayList();
        synchronized (_rooms) {
            for (Iterator i = _rooms.values().iterator(); i.hasNext(); ) {
                RoomServer room = (RoomServer)i.next();
                al.add(room.getName());
            }
        }
        String[] rooms = new String[al.size()];
        al.toArray(rooms);
        return rooms;
    }

    public String[] getServerNames() {
        Collection c = _distributedState.getAllServers();
        String[] servers = new String[c.size()];
        c.toArray(servers);
        return servers;
    }

    /**
     * Get the names of all the users on this server
     * @return an array contains the names of all the users
     */
    public String[] getUserNames() {
        Collection c = _distributedState.getAllUsers();
        String[] users = new String[c.size()];
        c.toArray(users);
        return users;
    }

    public String[] getLocalUserNames() {
        ArrayList al = new ArrayList();
        synchronized (_users) {
            for (Iterator i = _users.values().iterator(); i.hasNext(); ) {
                ChatClient client = (ChatClient)i.next();
                al.add(client.getUserId());
            }
        }
        String[] users = new String[al.size()];
        al.toArray(users);
        return users;
    }
    

    /**
     * get the number of rooms currently on the server
     * @return the number of rooms
     */
    public int getRoomCount() {
        return _rooms.size();
    }

    /**
     * get the number of users currently on the server
     * @return the number of users
     */
    public int getUserCount() {
        return _users.size();
    }

    /**
     * Get the uptime
     * @return the uptime in milliseconds
     */
    public long getUptime() {
        return System.currentTimeMillis() - _creationTime;
    }

    /**
     * Instantiate a ChatServer and start accepting connections
     */
    public static void main(String[] args) {
        try {
            ChatServer server = null;
            if (args.length > 1) {
                if ("-f".equals(args[0])) {
                    server = new ChatServer(args[1]);
                }
            }
            if (server == null) {
                server = new ChatServer();
            }
            server.acceptConnections();
        }
        catch (Exception e) {
            System.err.println("Error creating server");
            e.printStackTrace();
        }
    }

    protected Logger createServletContextLogger(String logFile) {
        try {
            return new Logger(ResourceLoader.touch(logFile), true);
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    protected void createLoggers(String logFile, String errorLogFile) {
        // create the logger
        if (logFile == null) {
            logger = new Logger(System.out);
        } else {
            if (_servletContext == null) {
                try {
                    logger = new Logger(logFile, true);
                }
                catch (IOException e) {
                    logger = new Logger(System.out);
                }
            } else {
              logger = createServletContextLogger(logFile);
              if (logger == null) {
                  logger = new Logger(System.out);
              }
            }
        }
        // create the error logger
        if (errorLogFile == null) {
            errorLogger = new Logger(System.err);
        } else {
            if (_servletContext == null) {
                try {
                    errorLogger = new Logger(errorLogFile, true);
                }
                catch (IOException e) {
                    errorLogger = new Logger(System.err);
                }
            } else {
                errorLogger = createServletContextLogger(errorLogFile);
                if (errorLogger == null) {
                    errorLogger = new Logger(System.err);
                }
            }
        }
    }

    protected static void showUsageAndExit() {
        System.err.println("usage: java com.lyrisoft.chat.server.remote.ChatServer port");
        System.exit(1);
    }

    public void distribute(ChatClient client, String text) {
        if (_distributor == null || !_distributor.isConnected()) {
            return;
        }
        try {
        TextMessage message = _distributor.createTextMessage();
            message.setStringProperty("origin", getName());
            if (client != null) {
                message.setStringProperty("client", client.getUserId());
            }
            message.setText(text);
            _distributor.push(message);
        }
        catch (JMSException e) {
            // this error is logged elsewhere
        }
    }

    /**
     * Incoming message from the Distributor (JMS)
     */ 
   public void handleIncoming(javax.jms.Message message) {
        try {
            String origin = message.getStringProperty("origin");
            if (getName().equals(origin)) {
                return;
            }
            String client = message.getStringProperty("client");
            String text = ((TextMessage)message).getText();
            CommandProcessorRemote.processDistributed(text, origin, client, this);
        }
        catch (JMSException e) {
            log(e);
        }
    }

    public String getName() {
        try {
            return java.net.InetAddress.getLocalHost().getHostName() + ":" + _port;
        }
        catch (java.net.UnknownHostException e) {
            throw new RuntimeException(e.toString());
        }
    }

    public void announcePresence() {
        distribute(null, HELLO);
        distributeUserList();
        distributeRoomList();
    }

    public void distributeRoomList() {
        String[] roomNames = getLocalRoomNames();
        if (roomNames.length == 0) {
            return;
        }
        StringBuffer sb = new StringBuffer(GET_ROOMS);
        sb.append(DELIMITER);
        for (int i=0; i < roomNames.length; i++) {
            String roomName = roomNames[i];
            sb.append(roomNames[i]);
            sb.append(DELIMITER);
        }
        distribute(null, sb.toString());

        for (int i=0; i < roomNames.length; i++) {
            distributeRoomState(roomNames[i]);
        }
    }

    public void distributeRoomState(String roomName) {
        RoomServer room = getRoom(roomName);
        if (room == null) {
            return;
        }
        String[] users = room.getUsers();
        String msg = CommandMakerRemote.constructRoomUserListMessage(roomName, users);
        distribute(null, msg);
    }

    /**
     * If user list is blank, just send an empty reply.  The  
     * empty reply is important because it's the only way
     * somebody who just said /hello will see that we exist
     */
    public void distributeUserList() {
        String[] userNames = getLocalUserNames();
        StringBuffer sb = new StringBuffer(GET_USERS_ON_SERVER);
        sb.append(DELIMITER);
        for (int i=0; i < userNames.length; i++) {
            sb.append(userNames[i]);
            sb.append(DELIMITER);
        }
        distribute(null, sb.toString());
    }

    // -------------------------------------------------------------------
    // -------------------------------------------------------------------
    public int getUserCountInRoom(String roomName) {
        return _distributedState.countUsersInRoom(roomName);
    }
    
    public int getUserCountOnServer(String server) {
        return _distributedState.countUsersOnServer(server);
    }


    public String[] getUsersInRoom(String roomName) {
        Collection c = _distributedState.getUsersInRoom(roomName);
        String[] names = new String[c.size()];
        c.toArray(names);
        return names;
    }

    public boolean userExists(String username) {
        return _distributedState.userExists(username);
    }

    public boolean serverExists(String servername) {
        return _distributedState.serverExists(servername);
    }

    public boolean roomExists(String roomname) {
        return _distributedState.roomExists(roomname);
    }
    
    public void checkServerPings(long timeout) {
        _distributedState.checkServerPings(timeout);
    }
  
    public void sendBroadcastPing() {
        distribute(null, BROADCAST_PING);
    }
  
    public void sendServerPing(String server) {
        StringBuffer sb = new StringBuffer();
        sb.append(PING);
        sb.append(DELIMITER);
        sb.append(server);
        sb.append(DELIMITER);
        sb.append(System.currentTimeMillis());
        distribute(null, sb.toString());
    }

    public void sendServerPong(String server, String arg) {
        StringBuffer sb = new StringBuffer();
        sb.append(PONG);
        sb.append(DELIMITER);
        sb.append(server);
        sb.append(DELIMITER);
        sb.append(arg);
        distribute(null, sb.toString());
    }

    public void handlePong(String server, String arg) {
        try {
            long now = System.currentTimeMillis();
            long delta = now - Long.valueOf(arg).longValue();
            ChatServer.log("Ping reply from " + server + ": " + delta + " ms.");
        }
        catch (NumberFormatException e) {
            ChatServer.log(e);
        }
    }
    
    public void handleBroadcastPing(String server) {
        _distributedState.setLastBroadcastPing(server, System.currentTimeMillis());
    }
    
	public IIgnoreStorage getIgnoreStore() {
		return _ignoreStore;
	}

    public static void DEBUG(String s) {
        if (DEBUG) {
            System.err.println(s);
        }
    }

    public static boolean getDebug() {
        String debug = System.getProperty("DEBUG");
        if (debug == null) {
            return false;
        }
        
        return (new Boolean(debug)).booleanValue();
    }
}
