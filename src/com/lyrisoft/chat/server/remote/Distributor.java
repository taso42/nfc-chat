/*
 * Copyright (c) 2000 Lyrisoft Solutions, Inc.
 * Used by permission
 */
package com.lyrisoft.chat.server.remote;

import java.util.Collections;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import javax.jms.JMSException;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;
import javax.jms.TopicSubscriber;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import com.lyrisoft.util.properties.PropertyException;
import com.lyrisoft.util.properties.PropertyTool;

/**
 * Handles sending and receiving messages over JMS
 *
 * TODO: connect should create an initial context and try to connect, and rotate
 */
public class Distributor implements MessageListener, Runnable {
    protected ChatServer _server;

    // queue for outbound messages
    protected List _queue = Collections.synchronizedList(new LinkedList());

    protected Hashtable _env;

    // our round-robin choices
    protected String[] _providerUrls;
    protected int _providerUrl = 0;

    protected Context _jndiContext;
    protected String _jmsUser;
    protected String _jmsPass;
    protected String _topicName;
    
    protected TopicPublisher _topicPublisher;
    protected TopicSession _topicSession;

    protected boolean _keepGoing = true;
    protected boolean _connected = false;

    private int _failedAttempts = 0; // how many attempts have failed so far

    public Distributor(ChatServer server, Properties p) throws PropertyException {
        _server = server;
        _topicName = PropertyTool.getString("jms.topic", p);
        String jndiInitialContextFactory = PropertyTool.getString("jndi.initial", p);
        _providerUrls = PropertyTool.getStringArray("jms.provider", p);
        _jmsUser = p.getProperty("jms.user");
        _jmsPass = p.getProperty("jms.pass");

        _env = new Hashtable();
        _env.put(Context.INITIAL_CONTEXT_FACTORY, jndiInitialContextFactory);
    }

    synchronized void createNewJndiContext() throws NamingException {
        if (_jndiContext != null) {
            _jndiContext.close();
        }
        _env.put (Context.PROVIDER_URL, getProvider());
        _jndiContext = new InitialContext(_env);
    }

    synchronized void rotateProviders() {
        _providerUrl++;
        if (_providerUrl >= _providerUrls.length) {
            _providerUrl = 0;
        }
    }
    
    synchronized String getProvider() {
        return _providerUrls[_providerUrl];
    }

	public Object jndiLookup(String name) throws NamingException {
        if (_jndiContext == null) {
            createNewJndiContext();
        }
        if (_failedAttempts == 0) {
            ChatServer.log("doing JNDI lookup of '" + name + "'");
        }
        Object obj = _jndiContext.lookup(name);
        return obj;
	}

    synchronized boolean isConnected() {
        return _connected;
    }

    synchronized void connect() throws JMSException, NamingException {
        if (_failedAttempts == 0) {
            ChatServer.log("Attempting to connect to JMS provider " + 
                           _providerUrls[_providerUrl]);
        }
        TopicConnectionFactory topicConnectionFactory = 
                (TopicConnectionFactory)jndiLookup("TopicConnectionFactory");
        TopicConnection topicConnection = null;
        try {
            if (_jmsUser == null) {
                topicConnection = topicConnectionFactory.createTopicConnection();
            } else {
                topicConnection = topicConnectionFactory.createTopicConnection(_jmsUser, _jmsPass);
            }
            Topic topic = (Topic)jndiLookup(_topicName);
            _topicSession = topicConnection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
            // subscriber
            TopicSubscriber topicSubscriber = _topicSession.createSubscriber(topic, null, true);
            topicSubscriber.setMessageListener(this);
            topicConnection.start(); // start the subscriber thread
            
            ChatServer.log("JMS Subscriber started.");
            
            // publisher
            _topicPublisher = _topicSession.createPublisher(topic);
            _connected = true;
            ChatServer.log("JMS Publisher initialized.");
            _server.distributorConnected(this);
        }
        catch (JMSException e) {
            // close conenction on error
            topicConnection.close();
            // and rethrow the exception up the stack
            throw e;
        }
    }

    public void run() {
        try {
            while (_keepGoing) {
                if (_connected) {
                    if (_queue.size() > 0) {
                        flushQueue();
                    }
                    Thread.sleep(25);
                } else {
                    try {
                        connect();
                        _failedAttempts = 0;
                        _jndiContext.close();
                        _jndiContext = null;
                    }
                    catch (JMSException e) {
                        if (_failedAttempts++ == 0) {
                            ChatServer.logError("Could not make JMS connection to " + 
                                                getProvider() + 
                                                " [" + e.getMessage() + "]");
                            ChatServer.logError("There will be no more JMS related log messages until I get a connection....");
                        }
                        rotateProviders();
                        Thread.sleep(30000);
                    }
                    catch (NamingException e) {
                        if (_failedAttempts++ == 0) {
                            ChatServer.logError("JNDI lookup failed.");
                            ChatServer.logError("There will be no more JMS related log messages until I get a connection....");
                        }
                        rotateProviders();
                        try {
                            createNewJndiContext();
                        }
                        catch (NamingException ex) {
                            if (_failedAttempts++ == 0) {
                                ChatServer.logError("Could not create initial JNDI context.");
                                ChatServer.logError("There will be no more JMS related log messages until I get a connection....");
                            }
                        }
                        Thread.sleep(30000);
                    }
                }
            }
        }
        catch (InterruptedException e) {
            ChatServer.log(e);
        }
    }

    void flushQueue() {
        while (_queue.size() > 0) {
            javax.jms.Message message = (javax.jms.Message)_queue.remove(0);
            try {
                if (ChatServer.DEBUG) {
                    try {
                        ChatServer.DEBUG("<< " + ((TextMessage)message).getText());
                    }
                    catch (JMSException e) {
                        e.printStackTrace();
                    }
                }
                _topicPublisher.publish(message);
            }
            catch (JMSException e) {
                ChatServer.log(e);
                _connected = false;
                _server.distributorDisconnected(this);
            }
        }
    }

    TextMessage createTextMessage() throws JMSException {
        try {
            return _topicSession.createTextMessage();
        }
        catch (JMSException e) {
            _connected = false;
            _server.distributorDisconnected(this);
            throw e;
        }
    }

    public void push(javax.jms.Message msg) {
        _queue.add(msg);
    }
    
    public void onMessage(javax.jms.Message message) {
        if (ChatServer.DEBUG) {
            try {
                ChatServer.DEBUG(">> " + ((TextMessage)message).getText());
                ChatServer.DEBUG(">>     origin = " + message.getStringProperty("origin"));
                ChatServer.DEBUG(">>     client = " + message.getStringProperty("client"));
            }
            catch (JMSException e) {}
        }
        _server.handleIncoming(message);
    }
    
}

