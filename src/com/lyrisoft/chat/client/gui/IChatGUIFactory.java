/*
 * Copyright (c) 2000 Lyrisoft Solutions, Inc.
 * Used by permission
 */
package com.lyrisoft.chat.client.gui;

import java.awt.Image;

import com.lyrisoft.chat.client.ChatClientApplet;
import com.lyrisoft.chat.server.local.IChatServer;

/**
 * Interface for objects that construct GUI objects.  GUI objects are never instantiated
 * directly; they always get created via an instance of this interface and are used through
 * their interfaces.  This allows various GUI implementations to be written, plugged in, and
 * used.
 *
 * To create a new GUI, implement all of the interfaces in this package.
 * @see IChatRoom
 * @see IConsole
 * @see ILogin
 * @see IYesNo
 */
public interface IChatGUIFactory {
    public java.awt.Component createAboutDialog();

    public void setMainGui(ChatGUI mainGui);
    public ChatGUI getMainGui();
    public void setInputReceiver(IChatClientInputReceiver inputReceiver);
    public void setChatServer(IChatServer server);
    
    /**
     * Construct an ILogin component
     * @param inputReceiver the object that will receive input
     */
    public ILogin createLoginDialog();

    /**
     * Construct an IChatRoom component.
     * @param name the name of the chat room
     * @param mainGui the ChatGui component
     * @param inputReceiver the object that will receive input
     * @param server the client-side view of the server
     */
    public IChatRoom createChatRoom(String name);

    public IPrivateChatRoom createPrivateChatRoom(String name);
    
    /**
     * Construct an IConsole component
     * @param inputReceiver the object that will receive input
     * @param mainGui the ChatGui component
     * @param server the client-side view of the server
     */
    public IConsole createConsole();

    public IQuery createQuery(String title, String choiceLabel, String[] choices, 
                              boolean showTextField, String textFieldLabel);

    public void show(IPrivateChatRoom room);
    public void hide(IPrivateChatRoom room);
    

    /**
     * Show and IChatRoom
     */
    public void show(IChatRoom room);

    public void hide(IChatRoom room);


    /**
     * Hide an ILogin
     */
    public void hide(ILogin login);

    /**
     * Show an ILogin
     */
    public void show(ILogin login);

    /**
     * Hide an IConsole
     */
    public void hide(IConsole rl);

    /**
     * Show an IConsole
     */
    public void show(IConsole rl);

    /**
     * It's ok to ignore this method unless you're implementing a factory made for Applets.
     */
    public void setApplet(ChatClientApplet a);

    public Image getImage(String name);

    /**
     * This method probably belongs elsewhere...  it's here for now because
     * the GUIFactory implementations know how to fetch resources from the resources 
     * dir...
     */
//    public Properties getProperties(String name);

    public void playAudioClip(String name);
    public void playSounds(boolean b);
    public boolean getPlaySounds();

    public void setAttribute(String name, Object value);
    public Object getAttribute(String name);
}
