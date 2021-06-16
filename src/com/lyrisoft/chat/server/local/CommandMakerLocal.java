/*
 * Copyright (c) 2000 Lyrisoft Solutions, Inc.
 * Used by permission
 */
package com.lyrisoft.chat.server.local;

import com.lyrisoft.chat.ICommands;

/**
 * All-static class that constructs wire-ready commands
 */
public class CommandMakerLocal implements ICommands {
    public static final String constructSignonMessage(String userId, String password) {
        return SIGNON + DELIMITER + userId + DELIMITER + password;
    }

    public static final String constructJoinRoomMessage(String room, String password) {
        return JOIN_ROOM + DELIMITER + room + (password == null ? "" : DELIMITER + password);
    }

    public static final String constructPartRoomMessage(String room) {
        return PART_ROOM + DELIMITER + room;
    }

    public static final String constructSayToRoomMessage(String room, String msg) {
        return SAY_TO_ROOM + DELIMITER + room + DELIMITER + msg;
    }

    public static final String constructSayToUserMessage(String user, String msg) {
        return SAY_TO_USER + DELIMITER + user + DELIMITER + msg;
    }

    public static final String constructRequestUsersInRoomListMessage(String room) {
        return GET_USERS_IN_ROOM + DELIMITER + room;
    }

    public static final String constructSignoffMessage() {
        return SIGNOFF;
    }

    public static final String constructEmoteToRoomMessage(String room, String msg) {
        return EMOTE_TO_ROOM + DELIMITER + room + DELIMITER + msg;
    }

    public static final String constructEmoteToUserMessage(String user, String msg) {
        return EMOTE_TO_USER + DELIMITER + user + DELIMITER + msg;
    }

    public static final String constructRequestRoomListMessage() {
        return GET_ROOMS;
    }

    public static final String constructRequestUserListMessage() {
        return GET_USERS_ON_SERVER;
    }

    public static final String constructPing(String user, String arg) {
        return PING + DELIMITER + user + DELIMITER + arg;
    }

    public static final String constructPong(String user, String arg) {
        return PONG + DELIMITER + user + DELIMITER + arg;
    }

    public static final String constructStats() {
        return STATS;
    }

    public static final String constructUserInfo(String user) {
        return WHOIS + DELIMITER + user;
    }

    public static final String constructHelpMessage(String command) {
        if (command == null) {
            command = "";
        } else if (command.charAt(0) == '/') {
            command = command.substring(1, command.length());
        }
        return HELP + DELIMITER + command;
    }

    public static final String constructVersionMessage(String ver) {
        return VERSION + DELIMITER + ver;
    }

    public static final String constructKillMessage(String user, String message) {
        return KILL + DELIMITER + user + (message == null ? "" : DELIMITER + message);
    }

    public static final String constructIgnoreMessage(String user, String message) {
        return IGNORE + DELIMITER + user + DELIMITER + (message == null ? "" : DELIMITER + message);
    }

    public static final String constructUnIgnoreMessage(String user) {
        return UNIGNORE + DELIMITER + user;
    }

    public static final String constructOpMessage(String userId, String room) {
        return OP + DELIMITER + room + DELIMITER + "+" + DELIMITER + userId;
    }

    public static final String constructDeopMessage(String userId, String room) {
        return OP + DELIMITER + room + DELIMITER + "-" + DELIMITER + userId;
    }

    public static final String constructKickMessage(String userId, String room) {
        return KICK + DELIMITER + room + DELIMITER + userId;
    }
}
