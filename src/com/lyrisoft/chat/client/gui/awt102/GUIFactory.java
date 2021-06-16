/*
 * Copyright (c) 2000 Lyrisoft Solutions, Inc.
 * Used by permission
 */
package com.lyrisoft.chat.client.gui.awt102;

import java.applet.Applet;
import java.awt.Component;
import java.awt.Frame;
import java.awt.Image;
import java.awt.Toolkit;
import java.io.File;
import java.util.Hashtable;

import com.lyrisoft.chat.client.gui.ChatGUI;
import com.lyrisoft.chat.client.gui.IChatClientInputReceiver;
import com.lyrisoft.chat.client.gui.IChatGUIFactory;
import com.lyrisoft.chat.client.gui.IChatRoom;
import com.lyrisoft.chat.client.gui.IConsole;
import com.lyrisoft.chat.client.gui.ILogin;
import com.lyrisoft.chat.client.gui.IPrivateChatRoom;
import com.lyrisoft.chat.client.gui.IQuery;
import com.lyrisoft.chat.server.local.IChatServer;
 
/**
 * An IGUIFactory implementation that creates AWT 1.0.2 compliant widgets.
 */
public class GUIFactory implements IChatGUIFactory {
    protected ChatGUI _mainGui;
    protected IChatServer _server;
    protected IChatClientInputReceiver _inputReceiver;
    protected Applet _dummyApplet = new Applet();
    protected boolean _playSounds = true;
    protected Hashtable _attributes = new Hashtable();
    
    protected ConsoleFrame _console;

    private static String getNFC_HOME() {
        String s = System.getProperty("NFC_HOME");
        if (s == null) {
            System.err.println("Property NFC_HOME not set.  Aborting.");
            System.exit(1);
            return null;
        } else {
            return s;
        }
    }

    void init() {
        String NFC_HOME = System.getProperty("NFC_HOME");
        if (NFC_HOME == null) {
            System.err.println("Property NFC_HOME not set.  Aborting.");
            System.exit(1);
        }
    }

    public void setMainGui(ChatGUI mainGui) {
        _mainGui = mainGui;
    }

    public ChatGUI getMainGui() {
        return _mainGui;
    }



    public void setInputReceiver(IChatClientInputReceiver inputReceiver) {
        _inputReceiver = inputReceiver;
    }

    public void setChatServer(IChatServer server) {
        _server = server;
    }

    public IChatRoom createChatRoom(String name)
    {
        return new ChatRoom(name, this, _mainGui, _inputReceiver, _server);
    }

    public IPrivateChatRoom createPrivateChatRoom(String name) {
        return new PrivateChatRoom(name, this, _mainGui, _inputReceiver, _server);
    }

    public ILogin createLoginDialog() {
        return new LoginDialog(this, _inputReceiver);
    }

    public IConsole createConsole() 
    {
        if (_console == null) {
            _console = new ConsoleFrame(_server, _inputReceiver, _mainGui, this);
        }
        return _console;
    }
    
    public IQuery createQuery(String title, 
                              String choiceLabel, String[] choices, 
                              boolean showTextField, String textFieldLabel) 
    {
        return new Query((Frame)_mainGui.getStatusGui(), title, choiceLabel, choices, showTextField, textFieldLabel);
    }

    public Component createAboutDialog() {
        return new About((Frame)_mainGui.getStatusGui(), _inputReceiver);
    }

    public void hide(IChatRoom room) {
        room.hide();
        if ( room instanceof ChatRoom ) {
            ((ChatRoom)room).dispose();
        }
    }

    public void show(IChatRoom room) {
        room.show();
    }

    public void hide(ILogin login) {
        login.hide();
        if (login instanceof LoginDialog) {
            ((LoginDialog)login).dispose();
        } 
    }

    public void show(ILogin login) {
        login.show();
    }

    public void show(IConsole c) {
        c.show();
    }

    public void hide(IConsole c) {
        c.hide();
    }

    public void show(IPrivateChatRoom room) {
        ((PrivateChatRoom)room).show();
    }

    public void hide(IPrivateChatRoom room) {
        if ( room instanceof PrivateChatRoom ) {
            ((PrivateChatRoom)room).hide();
            ((PrivateChatRoom)room).dispose();
        }
    }

    // noop
    public void setApplet(com.lyrisoft.chat.client.ChatClientApplet a) {
    }

/*
    public Properties getProperties(String name) {
        String file = getNFC_HOME() + File.separator + "web" + File.separator + "resources" + 
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
*/
    public Image getImage(String name) {
        String file = getNFC_HOME() + File.separator + "web" + File.separator + "resources" + 
                File.separator + name;
//        System.err.println("file = " + file);
        Image i = Toolkit.getDefaultToolkit().getImage(file);
        try {
            java.awt.MediaTracker _mediaTracker = new java.awt.MediaTracker((Frame)_mainGui.getStatusGui());
            _mediaTracker.addImage(i, 0);
            _mediaTracker.waitForAll();
            return i;
        }
        catch (Exception e) { 
            e.printStackTrace();
            return null;
        }
    }

    // shit!  we can't do this as a standalone app in JDK1.0.2.  What the hell were they thinking???
    public void playAudioClip(String name) {
    }

    public void playSounds(boolean b) {
        _playSounds = b;
    }

    public boolean getPlaySounds() {
        return _playSounds;
    }

/*    public AudioClip getAudioClip(String name) {
        String s = getNFC_HOME() + File.separator + "adm" + File.separator + "resources" + 
                File.separator + name;
        java.io.File f = new File(s);
        System.err.println("absol path = " + f.getAbsolutePath());
        try {
            java.net.URL url = new java.net.URL("file:" + f.getAbsolutePath());
            AudioClip clip = _dummyApplet.getAudioClip(url);
            return clip;
        }
        catch (java.net.MalformedURLException e) {
            return null;
        }
        catch (Exception e) { 
            e.printStackTrace();
            return null;
        }
    }
*/

    public void setAttribute(String name, Object value) {
        _attributes.put(name, value);
    }

    public Object getAttribute(String name) {
        return _attributes.get(name);
    }
}
