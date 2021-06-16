package com.lyrisoft.chat.test;

import java.util.HashMap;

import com.lyrisoft.chat.client.DumbClient;
import com.lyrisoft.chat.server.local.ChatServerLocal;
import com.lyrisoft.chat.server.local.IChatServer;

public class LoadTestClient extends DumbClient implements Runnable {
    IChatServer server;
    String myName;
    String room;
    HashMap messages = new HashMap();
    int sleepTime;
    String host;
    int port;
    String url = null;

    double average = 0;
    long sum = 0;
    long last = 0;
    int count = 0;
    
    public LoadTestClient(String name, String room, String host, int port, 
                          int sleep) 
    {
        myName = name;
        this.room = room;
        sleepTime = sleep;
        this.host = host;
        this.port = port;
    }
    
    public LoadTestClient(String name, String room, String url, int sleep) {
        myName = name;
        this.room = room;
        sleepTime = sleep;
        this.url = host;
    }

    public void init() {
        server.init();
    }

    public void showLogin() {
        // noop
    }

    public void setApplet(java.applet.Applet a) {
    }

    public void setInitialRoom(String room) {
    }
    
    public IChatServer getServerInterface() {
        return server;
    }

    public void run() {
        if (url == null) {
            server = new ChatServerLocal(host, port, this);
        } else {
            server = new ChatServerLocal(url, url, this);
        }
        
        init();

        server.signOn(myName, null);
        server.joinRoom(room, null);

        for (int i=0; ; i++) {

            // construct a unique message (and stash our current statistics in there as well)
            String message = "message" + i + ": " + 
                    "last = " + last + "; avg = " + average + "; samples = " + count;

            // remember the time of this message
            messages.put(message, new Long(System.currentTimeMillis()));

            // now broadcast it
            server.sayToRoom(room, message);

            // sleep for a random amount of time (between 5 and 30 seconds) before repeating
            int sleep = 5000 +  (int)(Math.random() * 30000);
            try { Thread.sleep(sleep); }
            catch (InterruptedException e) { return ; }
        }
    }

    public void ackSignon() {
        server.reportVersion("LoadTester 1.0");
    }
    
    /**
     * The server is indicating that someone is saying something in a room
     */
    public void messageFromUser(String user, String room, String msg) {
        if (user.equals(myName)) {
            handleMessage(msg);
        }
    }

    /**
     * The server is indicating that somebody ping'ed us.  It expects to get a pong message back
     */
    public void ping(String user, String arg) {
        server.sendPong(user, arg);
    }

    public void handleMessage(String message) {
        long now = System.currentTimeMillis();
        long then = ((Long)messages.get(message)).longValue();
        messages.remove(message);

        last = now - then;
        sum += last;
        count++;
        average = (double)sum / count;

//        System.err.println("last = " + last + "; avg = " + average + "; samples = " + count);
    }
}
