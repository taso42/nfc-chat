package com.carnageblender.chat;

import java.util.StringTokenizer;
import java.util.Vector;

import com.lyrisoft.chat.ICommands;

public class Protocol implements ICommands {
	private Net net;

	public Protocol(Net net) {
		this.net = net;
	}

	public void signon(String userId, String password) {
		net.send(SIGNON + DELIMITER + userId + DELIMITER + password);
	}

	// protocol allows rooms to be passworded; we don't support this
	public void joinRoom(String room) {
		net.send(JOIN_ROOM + DELIMITER + room);
	}

	public void partRoom(String room) {
		net.send(PART_ROOM + DELIMITER + room);
	}

	public void sayToRoom(String room, String msg) {
		net.send(SAY_TO_ROOM + DELIMITER + room + DELIMITER + msg);
	}

	public void sayToUser(String user, String msg) {
		net.send(SAY_TO_USER + DELIMITER + user + DELIMITER + msg);
	}

	public void requestUsersInRoomList(String room) {
		net.send(GET_USERS_IN_ROOM + DELIMITER + room);
	}

	public void Signoff() {
		net.send(SIGNOFF);
	}

	public void emoteToRoom(String room, String msg) {
		net.send(EMOTE_TO_ROOM + DELIMITER + room + DELIMITER + msg);
	}

	public void emoteToUser(String user, String msg) {
		net.send(EMOTE_TO_USER + DELIMITER + user + DELIMITER + msg);
	}

	public void requestRoomList() {
		net.send(GET_ROOMS);
	}

	public void requestUserList() {
		net.send(GET_USERS_ON_SERVER);
	}

	public void ping(String user, String arg) {
		net.send(PING + DELIMITER + user + DELIMITER + arg);
	}

	public void pong(String user, String arg) {
		net.send(PONG + DELIMITER + user + DELIMITER + arg);
	}

	public void version(String ver) {
		net.send(VERSION + DELIMITER + ver);
	}

	// sending msg w/ kill not supported
	public void kill(String user) {
		net.send(KILL + DELIMITER + user);
	}

	public void requestIgnoredList() {
		net.send(IGNORE_LIST);
	}

	// sending msg w/ ignore not supported
	public void ignore(String user, String message) {
		net.send(IGNORE + DELIMITER	+ user);
	}

	public void unIgnore(String user) {
		net.send(UNIGNORE + DELIMITER + user);
	}

	public void kick(String room, String userId) {
		net.send(KICK + DELIMITER + room + DELIMITER + userId);
	}
	
	public void op(String room, String userId) {
		net.send(OP + DELIMITER + room + DELIMITER + "+" + DELIMITER + userId);
	}

	/**
	 * Decompose a raw message into an array of String, splitting on
	 * the DELIMITER defined in ICommands.
	 * @see com.lyrisoft.chat.ICommands
	 */
	public static String[] decompose(String input) {
		return decompose(input, DELIMITER);
	}

	public static String[] decompose(String input, String delim) {
		StringTokenizer st = new StringTokenizer(input, delim);
		Vector v = new Vector(5);
		while (st.hasMoreTokens()) {
			v.addElement(st.nextToken());
		}
		String[] args = new String[v.size()];
		v.copyInto(args);
		return args;
	}

	/** Parse the protocol string
	(we assume the server gives us valid commands w/ the correct args)
	 */
	public void parse(String buf) {
		net.client.debug("rcvd: " + buf);

		if (buf == null) {
			net.reconnect("Connection lost");
		}

		String[] args = decompose(buf);
		String command = args[0];

		if (command.equals(SIGNON_ACK)) {
			// server is free to assign us a different name than requested
			net.client.setUser(args[1]);
			requestIgnoredList();
			requestRoomList();
			joinRoom(net.client.getChannel()); // default channel req'd
		} else if (command.equals(IGNORE_LIST)) {
			for (int i = 1; i < args.length; i++) {
				net.client.addIgnored(args[i]);
			}
			if (args.length > 1) {
				net.client.ignoreSlashCommand(""); // list
			}
		} else if (command.equals(JOIN_ROOM_ACK)) {
			net.client.setChannel(args[1]);
			net.client.appendLine("*** Now in room " + args[1]);
			requestUsersInRoomList(args[1]);
		} else if (command.equals(PART_ROOM_ACK)) {
			net.client.setChannel(null);
			net.client.resetUserList();
		} else if (command.equals(GET_ROOMS)) {
			net.client.appendLine("Existing rooms:");
			for (int i = 1; i < args.length; i += 2) {
				net.client.appendLine("\t" + args[i] + ": " + args[i + 1] + " users");
			}
		} else if (command.equals(GET_USERS_IN_ROOM)) {
			if (args[1].equalsIgnoreCase(net.client.getChannel())) {
				// this means if user did /who on his current channel he will see nothing
				// for now i'm willing to live with that rather than record
				// if user sent a /who on his own channel recently.

				for (int i = 2; i < args.length; i++) {
					net.client.addUser(args[i]);
				}
			} else {
				net.client.appendLine("Users in room " + args[1] + ":");
				for (int i = 2; i < args.length; i++) {
					net.client.appendLine("\t" + args[i]);
				}
			}
		} else if (command.equals(PING)) {
			pong(args[1], args[2]);
		} else if (command.equals(ROOM_USER_DIFF)) {
			checkRoom(args[1]);
			if (args[2].equals("+")) {
				net.client.addUser(args[3], true);
			} else {
				net.client.userLeftChannel(args[3]);
			}
		} else if (command.equals(USER_DIFF)) {
			net.client.userLeftChannel(args[2]);
		} else if (command.equals(ACCESS_DENIED)) {
			net.client.appendLine("*** Access denied");
		} else if (command.equals(ROOM_ACCESS_DENIED)) {
			net.client.appendLine("*** Could not join channel");
		} else if (command.equals(SAY_TO_ROOM)) {
			checkRoom(args[2]);
			net.client.appendMsg(args[1], args[3], Client.CHANNEL);
		} else if (command.equals(EMOTE_TO_ROOM)) {
			checkRoom(args[2]);
			net.client.appendMsg(args[1], args[3], Client.EMOTE | Client.CHANNEL);
		} else if (command.equals(EMOTE_TO_USER)) {
			net.client.appendMsg(args[1], args[2], Client.EMOTE | Client.PRIVATE);
		} else if (command.equals(SAY_TO_USER)) {
			// yes we let user talk to self if he likes :P
			net.client.appendMsg(args[1], args[2], Client.PRIVATE);
		} else if (command.equals(KILL)) {
			// KILL is only sent to its target
			net.client.appendLine("*** Killed by " + args[1] + ": " + args[2]);
			net.client.stopNicely();
		} else if (command.equals(ERROR)) {
			net.client.appendLine("Error: " + args[1]);
		} else if (command.equals(ROOM_MSG)) {
			net.client.appendLine("- " + args[1] + ": " + args[2]);
		} else if (command.equals(NO_SUCH_USER)) {
			// the only way to get this is to PM an invalid target
			net.client.appendLine("- User " + args[1] + " is not online", net.client.privateText);
		} else {
			net.client.appendLine("*** " + buf);
		}
	}
	
	private void checkRoom(String s) {
		if (!s.equalsIgnoreCase(net.client.getChannel())) {
			net.client.debug(
				"Server's sending me info about a room I'm not in");
		}
	}
}
