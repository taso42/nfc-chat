/*
 * Copyright (c) 2000 Lyrisoft Solutions, Inc.
 * Used by permission
 */
package com.lyrisoft.chat.server.remote;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.SocketException;

import com.lyrisoft.chat.IConnectionListener;

/**
 * Constantly reads from the BufferedReader.
 * Notifies the ChatServerLocal via the incomingMessage() method
 */
public class ReaderThread extends Thread {
    protected IConnectionListener _connectionListener;
    protected BufferedReader _in;
    protected InputStream _inputStream;
    protected boolean _keepGoing = true;
    protected ConnectionHandler _connectionHandler;
    
    public ReaderThread(ConnectionHandler handler, IConnectionListener listener, InputStream in) {
        super("ReaderThread");
        _connectionHandler = handler;
        _connectionListener = listener;
        _inputStream = in;
        _in = new BufferedReader(new InputStreamReader(in));
    }
    
    public void normalRun() {
        try {
            String newLine;
            while (_keepGoing &&
                   (newLine = _in.readLine()) != null)
            {
                _connectionListener.incomingMessage(newLine);
                ChatServer.DEBUG("> " + newLine);
            }
        }
		catch (SocketException se) {
			_keepGoing = false;
			ChatServer.DEBUG("Socket closed.  closing connection.");
		}
        catch (IOException e) {
            ChatServer.log(e);
        }
    }
    
    public void bsdHackRun() {
        try {
            String newLine;
            while (_keepGoing) {
                try {
                    while (_inputStream.available() < 1) {
                        Thread.sleep(25);
                    }
                }
                catch (InterruptedException e) {}
                newLine = _in.readLine();
                _connectionListener.incomingMessage(newLine);
                ChatServer.DEBUG("> " + newLine);
            }
        }
        catch (IOException e) {
            ChatServer.log(e);
        }
    }
    
    public void run() {
        String s = System.getProperty("BSD_HACK");
        if (s == null) {
            normalRun();
        } else {
            bsdHackRun();
        }
        _connectionHandler.shutdown(true);
    }
    
    void pleaseStop() {
        _keepGoing = false;
    }
}
