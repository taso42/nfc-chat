/*
 * Copyright (c) 2000 Lyrisoft Solutions, Inc.
 * Used by permission
 */
package com.lyrisoft.chat.client;

import java.applet.Applet;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import com.lyrisoft.chat.Translator;
import com.lyrisoft.chat.client.gui.IChatGUIFactory;
import com.lyrisoft.chat.client.gui.IChatRoom;
import com.lyrisoft.chat.server.local.CommandProcessorLocal;

/**
 * Applet wrapper for the Client.
 */
public class ChatClientApplet extends Applet {
    protected IChatGUIFactory _guiFactory;
    protected IChatClient _client;
    protected String _host;
    protected int _port = -1;
    protected Component _currentComponent;
    protected UserCommands _userCommands;
    protected CommandProcessorLocal _commandProcessor;
    protected String  _room; // optional initial room to join
    protected String  _autoLogin;
    protected boolean _keepAlive = false;

    protected String _tunnelReadUrl = null;
    protected String _tunnelWriteUrl = null;

    protected boolean _tunnelOnly = false;

    protected void readParams() {
        _host = getParameter("host");
        if (_host == null) {
            _host = getCodeBase().getHost();
        }
        String sPort = getParameter("port");
        if (sPort != null) {
            _port = Integer.parseInt(sPort);
        }
        String guiFactoryName = getParameter("guiFactory");
        if (guiFactoryName == null) {
            guiFactoryName = "com.lyrisoft.chat.client.gui.awt102.AppletGUIFactory";
        }
        try {
            installGuiFactory(guiFactoryName);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error installing guiFactory: " + guiFactoryName);
        }

        _room = getParameter("autojoin");
        _autoLogin = getParameter("autologin");
        String sKeepAlive = getParameter("keepalive");
        if (sKeepAlive != null) {
            _keepAlive = Boolean.valueOf(sKeepAlive).booleanValue();
        }
        _tunnelReadUrl = fixUrl(getParameter("tunnelRead"));
        _tunnelWriteUrl = fixUrl(getParameter("tunnelWrite"));

        String sTunnelOnly = getParameter("tunnelOnly");
        if (sTunnelOnly != null) {
            _tunnelOnly = Boolean.valueOf(sTunnelOnly).booleanValue();
        } else {
            _tunnelOnly = false;
        }
        
        initTranslator();
        initCommandProcessor();
        initUserCommands();
    }

    void initTranslator() {
        String props = getParameter("messages");
        if (props == null) {
            props = "messages.properties";
        }
        Properties p = getProperties(props);
        Translator.init(p);
    }

    void initUserCommands() {
        Properties p = getProperties("userCommands.properties");
        if (p != null) {
            _userCommands = new UserCommands(p);
        } else {
            throw new RuntimeException("Failed to load userCommands.properties");
        }
    }

    void initCommandProcessor() {
        Properties p = getProperties("commandProcessors.properties");
        if (p != null) {
            _commandProcessor = new CommandProcessorLocal(p);
        } else {
            throw new RuntimeException("Failed to load commandProcessors.properties");
        }
    }
    
    String fixUrl(String url) {
        
        if (url == null || url.indexOf(":/") > 0)
            return url;

        java.net.URL base = getCodeBase();
        String newUrl = null;
        if(base.getPort() != -1)
            newUrl = base.getProtocol()+"://"+base.getHost()+":"+base.getPort()+url;
        else
            newUrl = base.getProtocol()+"://"+base.getHost()+url;
        System.err.println("new Url = " + newUrl);
        return newUrl;
    }

    void installGuiFactory(String guiFactoryName) throws Exception {
        Color bgColor = null;
        Color errorColor = null;
        Color userColor = null;
        try {
            String sbgColor = getParameter("bgColor");
            if (sbgColor != null) {
                bgColor = new Color(Integer.parseInt(sbgColor, 16));
            }
        }
        catch (NumberFormatException e) {}
        
        try {
            String serrorColor = getParameter("errorColor");
            if (serrorColor != null) {
                errorColor = new Color(Integer.parseInt(serrorColor, 16));
            }
        }
        catch (NumberFormatException e) {}

        try {
            String suserColor = getParameter("userColor");
            if (suserColor != null) {
                userColor = new Color(Integer.parseInt(suserColor, 16));
            }
        }
        catch (NumberFormatException e) {}
        
        System.err.println("Installing GUI Factory: " + guiFactoryName);
        _guiFactory = (IChatGUIFactory)Class.forName(guiFactoryName).newInstance();
        if (bgColor != null) {
            _guiFactory.setAttribute("bgColor", bgColor);
        }
        if (userColor != null) {
            _guiFactory.setAttribute("userColor", userColor);
        }
        if (errorColor != null) {
            _guiFactory.setAttribute("errorColor", errorColor);
        }
        
        _guiFactory.setApplet(this);
    }

    /**
     * Reads the following applet parameters:
     * <ul>
     * <li>port - the port to connect to
     * <li>guiFactory - the classname of a GUIFactory (defaults to awt102.AppletGUIFactory)
     * </ul>
     *
     * host is implicit.  It is gotten from the codeBase.
     */
    public void init() {
        readParams();
        
        setLayout(new GridLayout(1, 1));
    }

    public boolean getKeepAlive() {
        return _keepAlive;
    }

    protected IChatClient createClient(String readUrl, String writeUrl) {
        return new Client(readUrl, writeUrl);
    }

    protected IChatClient createClient(String host, int port) {
        return new Client(host, port);
    }

    protected IChatClient createClient(String host, int port, String readUrl, String writeUrl) {
        return new Client(host, port, readUrl, writeUrl);
    }

    /**
     * Create a new Client instance and call setRunningAsAppet(true) on it.
     */
    public void start() {
        if (_tunnelOnly) {
            _client = createClient(_tunnelReadUrl, _tunnelWriteUrl);
        } else {
            if (_tunnelReadUrl != null && _tunnelWriteUrl != null) {
                _client = createClient(_host, _port, _tunnelReadUrl, _tunnelWriteUrl);
            } else {
                _client = createClient(_host, _port);
            }
        }
        _client.setAttribute("guiFactory", _guiFactory);
        _client.setAttribute("userCommands", _userCommands);
        _client.setAttribute("commandProcessor", _commandProcessor);
        _client.setApplet(this);
        _client.setInitialRoom(_room);
        _client.init();

        // we need to display the login screen even if there's an autologin,
        // because there might be a login error...
        _client.showLogin(); 
        if (_autoLogin != null) {
            _client.getServerInterface().signOn(_autoLogin, null);
        } 
    }

    /**
     * Sign the user off
     */
    public void stop() {
        if (_client != null) {
            if (!_keepAlive) {
                _client.getServerInterface().signOff();
            }
        }
    }

    /**
     * Replace whatever is showing with a new component
     */
    public synchronized void setView(Component c) {
        if (_currentComponent != null) {
            if (_currentComponent instanceof IChatRoom) {
                IChatRoom room = (IChatRoom)_currentComponent;
                _client.getServerInterface().partRoom(room.getName());
            }
            removeView(_currentComponent);
        } 
        add(c);
        _currentComponent = c;
        
        validate();
    }

    /**
     * Remove a view
     */
    public synchronized void removeView(Component c) {
        Component[] children = getComponents();
        if (children.length > 0 && children[0] == c) {
            remove(c);
            _currentComponent = null;
        } 
    }

    /**
     * Load a properties file from the "standard place"
     */
    public Properties getProperties(String name) {
        InputStream is = null;
        try {
            URL docBase = new URL(getCodeBase(), "resources/");
            URL propsUrl = new URL(docBase, name);
            is = propsUrl.openStream();
            Properties p = new Properties();
            p.load(is);
            return p;
        }
        catch (Exception e) {}
        finally {
            if (is != null) {
                try {
                    is.close();
                }
                catch (Exception e) {}
            }
        }
        return null;
    }
}
