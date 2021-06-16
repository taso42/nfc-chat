/*
 * Copyright (c) 2000 Lyrisoft Solutions, Inc.
 * Used by permission
 */
package com.lyrisoft.chat.client.gui.awt102;

import java.applet.AudioClip;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.TextArea;
import java.awt.Toolkit;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import com.lyrisoft.chat.Translator;
import com.lyrisoft.chat.client.ChatClientApplet;
import com.lyrisoft.chat.client.gui.IChatGUIFactory;
import com.lyrisoft.chat.client.gui.IConsole;
import com.lyrisoft.chat.client.gui.ILogin;

/**
 * Subclass of GUIFactory specifically for Applets.
 * Overrides some methods so that Panels end up being displayed in the applet
 * rather than in a new window.
 */
public class AppletGUIFactory extends GUIFactory implements IChatGUIFactory {
    protected ChatClientApplet _applet;
    private TextArea _textArea;
    private URL _docBase;
    private MediaTracker _mediaTracker;

    public void setApplet(ChatClientApplet a) {
        _applet = a;
        try {
            _docBase = new URL(a.getCodeBase(), "resources/");
        }
        catch (MalformedURLException e) {
            return;
        }
        _mediaTracker = new java.awt.MediaTracker(a);
    }

    /**
     * Overridden.  Calls super.createConsole() to create the actual console.  Then,
     * puts a note inside the Applet that says "The Chat Program is running in another
     * window.. yadda yadda yadda...".  Finally returns the IConsole object
     */
    public IConsole createConsole() 
    {
        IConsole console = super.createConsole();
        showTextArea();
        return console;
    }

    public ILogin createLoginDialog() {
        LoginPanel lp = new LoginPanel(this, _inputReceiver);
        _applet.setView(lp);
        return lp;
    }

    public void hide(ILogin login) {
        _applet.removeView((LoginPanel)login);
    }

    public void show(ILogin login) {
        _applet.setView((LoginPanel)login);
    }

    public void show(IConsole c) {
        showTextArea();
        super.show(c);
    }

    private void showTextArea() {
        if (_textArea == null) {
            _textArea = new TextArea();
            _textArea.setEditable(false);
        }
        _textArea.setText(Translator.getMessage("chat.running1"));
        _textArea.appendText("\n\n");
        if (!_applet.getKeepAlive()) {
            _textArea.appendText(Translator.getMessage("chat.running2") + "\n");
            _textArea.appendText(Translator.getMessage("chat.running3") + "\n");
        }
        _applet.setView(_textArea);
    }

    public Properties getProperties(String name) {
        InputStream is = null;
        try {
            URL propsUrl = new URL(_docBase, name);
            is = propsUrl.openStream();
            Properties p = new Properties();
            p.load(is);
            return p;
        }
        catch (Exception e) {
        }
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

    public Image getImage(String name) {
        try {
            URL url = new URL(_docBase, name);
            Image i = Toolkit.getDefaultToolkit().getImage(url);
            _mediaTracker.addImage(i, 0);
            _mediaTracker.waitForAll();
            return i;
        }
        catch (Exception e) { 
            e.printStackTrace();
            return null;
        }
    }

    public void playAudioClip(String name) {
        if (_playSounds) {
            AudioClip clip = _applet.getAudioClip(_docBase, name);
            clip.play();
        } 
    }
}

