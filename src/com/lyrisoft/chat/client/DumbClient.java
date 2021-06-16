package com.lyrisoft.chat.client;

import java.io.File;
import java.io.FileInputStream;
import java.util.Hashtable;
import java.util.Properties;

/**
 * An empty implementation of IChatClient.  You can subclass this class
 * and just override the methods you're interested in.
 * com.lyrisoft.chat.test.LoadTestClient is an example implementation,
 * and of course, com.lyrisoft.chat.client.Client is another example.
 * Think of this class like you think of event Adapter classes:  You
 * only have to override the methods you're interested in.
 */
public abstract class DumbClient implements IChatClient {
    protected Hashtable _attributes = new Hashtable();

    /**
     * The server has authenticated us and acknowledged our sign on
     */
    public void ackSignon(String myName) {
    }

    /**
     * The connection to the server was closed
     */
    public void connectionLost() {
    }

    /**
     * The server has acknowledged our room join
     */
    public void ackJoinRoom(String room) {
    }

    /**
     * The server has acknowledged our room part
     */
    public void ackPartRoom(String room) {
    }

    /**
     * The server is indicating that someone is saying something in a room
     */
    public void messageFromUser(String user, String room, String msg) {
    }

    /**
     * The server is indicating that someone is saying something to us, privately
     */
    public void messageFromUserPrivate(String user, String msg) {
    }

    /**
     * The server is indicating that someone is privately emoting something to us.
     */
    public void emoteFromUserPrivate(String user, String msg) {
    }
    
    /**
     * The server is sending us a list of rooms
     */
    public void roomList(String[] roomList) {
    }

    /**
     * The server is sending us the global user list
     */
    public void globalUserList(String[] users) {
    }

    /**
     * The server is sending us the user list for a particular room
     */
    public void roomUserList(String room, String[] users) {
    }

    /**
     * The server is indicating that someone just joined a room we're in.
     */
    public void userJoinedRoom(String user, String room) {
    }

    /**
     * The server is indicating that someone just parted a room we're in.
     */
    public void userPartedRoom(String user, String room, boolean signOff) {
    }

    /**
     * The server is giving us an error message to display
     */
    public void generalError(String message) {
    }

    /**
     * The server is giving us a general message to display.  (This could be a MOTD or the
     * answer to some general request like the /stats command)
     */
    public void generalMessage(String message) {
    }

    /**
     * The server is giving us a message to display in the context of a particular room.  This
     * is commonly an emote message.
     */
    public void generalRoomMessage(String room, String message) {
    }

    /**
     * The server is indicating that somebody ping'ed us.  It expects to get a pong message back
     */
    public void ping(String user, String arg) {
    }

    /**
     * The server is giving us the reply to a ping that we already sent out.
     */
    public void pong(String user, String arg) {
    }
    
    /**
     * Indicates that we have been killed.
     * @param killer the use who killed us
     * @param msg an explanation of why were have been killed
     */
    public void killed(String killer, String msg) {
    }

    /**
     * Acknowledgement that we successfully killed somebody
     */
    public void ackKill(String victim) {
    }

    /**
     * Indicates that somebody is emoting
     */
    public void emote(String from, String room, String message) {
    }

    /**
     * Indicates that a user has signed on
     */
    public void userSignOn(String userId) {
    }

    /**
     * Indicates that a user has signed off
     */
    public void userSignOff(String userId) {
    }

    /**
     * Indicates that a room has been created
     */
    public void roomCreated(String room) {
    }

    /**
     * Indicates that a room has been destroyed
     */
    public void roomDestroyed(String room) {
    }

    /**
     * Set any arbitrary attribute
     */
    public void setAttribute(String attribute, Object value) {
        _attributes.put(attribute, value);
    }

    /**
     * Get the value of an atrribute
     */
    public Object getAttribute(String attribute) {
        return _attributes.get(attribute);
    }

    /**
     * Load a Properties file from NFC_HOME
     */
    public Properties getProperties(String name) {
        String nfcHome = System.getProperty("NFC_HOME");
        String file = nfcHome + File.separator + "web" + File.separator + "resources" + 
                File.separator + name;
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            Properties p = new Properties();
            p.load(fis);
            return p;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            if (fis != null) {
                try {
                    fis.close();
                } 
                catch (Exception e) {}
            }
        }
        return null;
    }
}
