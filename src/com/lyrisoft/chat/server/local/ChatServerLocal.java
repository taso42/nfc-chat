/*
 * Copyright (c) 2000 Lyrisoft Solutions, Inc.
 * Used by permission
 */
package com.lyrisoft.chat.server.local;

import java.io.IOException;
import java.net.Socket;

import com.lyrisoft.chat.ICommands;
import com.lyrisoft.chat.IConnectionHandler;
import com.lyrisoft.chat.IConnectionListener;
import com.lyrisoft.chat.Translator;
import com.lyrisoft.chat.client.IChatClient;

/**
 * This is a view of the ChatServer that lives on the client side.
 * When the client talks to the server, no fancy work has to be done; instead direct
 * method calls are invoked on this object, and this object sees to it that the message gets to
 * the server in a way that the server can understand.
 *
 * Similarly, when a raw message comes in from the server, it is processed, then a
 * direct method call is invoked on the Client.
 *
 * The CommandMakerLocal class is used to construct the messages that are sent over the wire
 *
 * <p>
 * 2000-04-15: Added code so that a connection falls back to an http-tunneling scheme.<br>
 *
 * @see CommandMakerLocal
 */
public class ChatServerLocal implements IChatServer, IConnectionListener, ICommands {
    protected String _host;
    protected int _port;
    protected boolean _connected = false;
    protected IConnectionHandler _connectionHandler;
    protected IChatClient _responseInterface;

    protected SocketOpener _socketOpener;
    protected boolean _alwaysTunnel = false;
    protected boolean _attemptToTunnel = true;

    protected String _readUrl;
    protected String _writeUrl;

    protected CommandProcessorLocal _commandProcessor;

    public ChatServerLocal(String readUrl, String writeUrl, IChatClient responseInterface) {
        _readUrl = readUrl;
        _writeUrl = writeUrl;
        _responseInterface = responseInterface;

        _attemptToTunnel = true;
        _alwaysTunnel = true;
    }

    public ChatServerLocal(String host, int port, IChatClient responseInterface) {
        _host = host;
        _port = port;
        _responseInterface = responseInterface;

        _socketOpener = new SocketOpener(host, port);
        _attemptToTunnel = false;
   }

    public ChatServerLocal(String host, int port, String readUrl, String writeUrl, 
                           IChatClient responseInterface) 
    {
        _readUrl = readUrl;
        _writeUrl = writeUrl;

        _host = host;
        _port = port;
        _responseInterface = responseInterface;

        _socketOpener = new SocketOpener(host, port);
        _attemptToTunnel = true;
    }

    public boolean isConnected() {
        return _connected;
    }

    public void init() {
        _commandProcessor = (CommandProcessorLocal)_responseInterface.getAttribute("commandProcessor");
        if (_commandProcessor == null) {
        	throw new RuntimeException("Could not init commandProcessor");
        }
    }

    /**
     * This method is called by ConnectionHandlerLocal when a new message comes in
     * from the server.  Control is delegated to CommandProcessorLocal.process(), and
     * eventually one of the CommandProcessors will make a direct method call on the Client.
     *
     * @see ConnectionHandlerLocal
     * @see CommandProcessorLocal#process
     */
    public void incomingMessage(String message) {
        _commandProcessor.process(message, _responseInterface);
    }

    /**
     * This method is called by ConnectionHandlerLocal when the connect to the server is lost.
     * connectionLost() is called on the client to inform it that the connection is lost.
     * @see ConnectionHandlerLocal
     * @see com.lyrisoft.chat.client.Client#connectionLost
     */
    public void socketClosed() {
        _connected = false;
        _responseInterface.connectionLost();
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    /**
     * Construct and queue an outgoing help request
     * @param command the command you want help on (optional)
     */
    public void help(String command) {
        _connectionHandler.queueMessage(CommandMakerLocal.constructHelpMessage(command));
    }

    /**
     * Construct and queue an outgoing message.
     */
    public void signOn(String userId, String password) {
        if (userId == null || userId.length() == 0) {
            _responseInterface.generalError(Translator.getMessage("error.need.userid"));
            return;
        }

        try {
            connect();
        }
        catch (IOException e) {
            e.printStackTrace();
            _responseInterface.generalError(Translator.getMessage("error.cannot.connect",
                                                                  _host + ":" + _port));
            return;
        }
        _responseInterface.generalMessage(Translator.getMessage("connected.logging.in"));
        _connectionHandler.queueMessage(CommandMakerLocal.constructSignonMessage(userId, password));
    }

    /**
     * Construct and queue an outgoing message.
     */
    public void signOff() {
        _connectionHandler.queueMessage(CommandMakerLocal.constructSignoffMessage());
    }

    /**
     * Construct and queue an outgoing message.
     */
    public void requestRoomList() {
        _connectionHandler.queueMessage(CommandMakerLocal.constructRequestRoomListMessage());
    }

    /**
     * Construct and queue an outgoing message.
     */
    public void requestUserList() {
        _connectionHandler.queueMessage(CommandMakerLocal.constructRequestUserListMessage());
    }

    /**
     * Construct and queue an outgoing message.
     */
    public void requestUsersInRoomList(String room) {
        _connectionHandler.queueMessage(CommandMakerLocal.constructRequestUsersInRoomListMessage(room));
    }

    /**
     * Construct and queue an outgoing message.
     */
    public void joinRoom(String room, String password) {
        _connectionHandler.queueMessage(CommandMakerLocal.constructJoinRoomMessage(room, password));
    }

    /**
     * Construct and queue an outgoing message.
     */
    public void partRoom(String room) {
       _connectionHandler.queueMessage(CommandMakerLocal.constructPartRoomMessage(room));
    }

    /**
     * Construct and queue an outgoing message.
     */
    public void sayToRoom(String room, String message) {
        _connectionHandler.queueMessage(CommandMakerLocal.constructSayToRoomMessage(room, message));
    }

    /**
     * Construct and queue an outgoing message.
     */
    public void sayToUser(String userId, String message) {
        _connectionHandler.queueMessage(CommandMakerLocal.constructSayToUserMessage(userId, message));
    }

    /**
     * Construct and queue an outgoing message.
     */
    public void emoteToRoom(String room, String message) {
        _connectionHandler.queueMessage(CommandMakerLocal.constructEmoteToRoomMessage(room, message));
    }

    /**
     * Construct and queue an outgoing message.
     */
    public void emoteToUser(String userId, String message) {
        _connectionHandler.queueMessage(CommandMakerLocal.constructEmoteToUserMessage(userId, message));
    }

    /**
     * Construct and queue an outgoing message.
     */
    public void kill(String userId, String message) {
        _connectionHandler.queueMessage(CommandMakerLocal.constructKillMessage(userId, message));
    }

    /**
     * Construct and queue an outgoing message.
     */
    public void sendPing(String user, String arg) {
        _connectionHandler.queueMessage(CommandMakerLocal.constructPing(user, arg));
    }

    /**
     * Construct and queue an outgoing message.
     */
    public void sendPong(String user, String arg) {
        _connectionHandler.queueMessage(CommandMakerLocal.constructPong(user, arg));
    }

    /**
     * Construct and queue an outgoing message.
     */
    public void requestStats() {
        _connectionHandler.queueMessage(CommandMakerLocal.constructStats());
    }

    /**
     * Construct and queue an outgoing message.
     */
    public void requestUserInfo(String user) {
        _connectionHandler.queueMessage(CommandMakerLocal.constructUserInfo(user));
    }

    /**
     * Construct and queue an outgoing message.
     */
    public void ignore(String userId, String message) {
        _connectionHandler.queueMessage(CommandMakerLocal.constructIgnoreMessage(userId, message));
    }
    
    /**
     * Construct and queue an outgoing message.
     */
    public void unignore(String userId) {
        _connectionHandler.queueMessage(CommandMakerLocal.constructUnIgnoreMessage(userId));
    }

    public void reportVersion(String version) {
        _connectionHandler.queueMessage(CommandMakerLocal.constructVersionMessage(version));
    }

    public void op(String userId, String room) {
        _connectionHandler.queueMessage(CommandMakerLocal.constructOpMessage(userId, room));
    }

    public void deop(String userId, String room) {
        _connectionHandler.queueMessage(CommandMakerLocal.constructDeopMessage(userId, room));
    }

    public void kick(String userId, String room) {
        _connectionHandler.queueMessage(CommandMakerLocal.constructKickMessage(userId, room));
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    /**
     * Make a socket connection to the server (if we're not already connected).
     * Once connected, create a ConnectionHandlerLocal, that will handle I/O.
     * @see ConnectionHandlerLocal
     */
    void connect() throws IOException {
        if (_connected) {
            System.err.println("already connected...");
            return;
        }
        
        IOException ioexception = null;

        if (!_alwaysTunnel) {
            System.err.println("Opening socket connection to " + _host + ":" + _port);
            _responseInterface.generalMessage(Translator.getMessage("connecting.to",
                                                                    _host + ":" + _port));
            Socket s = null;
            try {
                if (!_attemptToTunnel) {
                    s = new Socket(_host, _port);
                } else {
                    s = _socketOpener.makeSocket(5000);
                }

                System.err.println("ChatServerLocal: connected to " + _host + ":" + _port);
//                System.err.println("setting NO_DELAY = true");
//                s.setTcpNoDelay(true);
                _connectionHandler = new ConnectionHandlerLocal(s);
                _connectionHandler.setListener(this);
                _connected = true;
                return;
            }
            catch (IOException e) {
                ioexception = e;
            }
        } 

        if (_attemptToTunnel) {
            _responseInterface.generalMessage(Translator.getMessage("connecting.to",
                                                                    _readUrl));
            System.err.println("Opening tunnel connetion to " + _readUrl);
            _connectionHandler = new HttpConnectionHandler(_readUrl, _writeUrl);
            System.err.println("Got the connection.");
            _connectionHandler.setListener(this);
            _connected = true;
            return;
        } 

        System.err.println("giving up");

        if (ioexception != null) {
            throw ioexception;
        }
    }
}
