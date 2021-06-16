/*
 * Copyright (c) 2000 Lyrisoft Solutions, Inc.
 * Used by permission
 */
package com.lyrisoft.chat.client.gui.awt102;

import java.awt.CheckboxMenuItem;
import java.awt.Event;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.Toolkit;

import com.lyrisoft.chat.Translator;
import com.lyrisoft.chat.client.gui.ChatGUI;
import com.lyrisoft.chat.client.gui.IChatClientInputReceiver;
import com.lyrisoft.chat.client.gui.IChatGUIFactory;
import com.lyrisoft.chat.client.gui.IChatRoom;
import com.lyrisoft.chat.server.local.IChatServer;

/**
 * A JDK 1.0.2 compliant Frame subclass that contains a ChatPanel.
 * All the IChatRoom methods are delegated to the contained ChatPanel.
 */
public class ChatRoom extends Frame implements IChatRoom {
    protected EnhancedChatPanel _chatPanel;
    protected String _room;
    protected ChatGUI _mainGUI;
    protected IChatServer _server;
    protected IChatGUIFactory _factory;

    protected Menu fileMenu;
    protected Menu fontStyleMenu;
    protected Menu fontSizeMenu;
    protected Menu helpMenu;

    protected CheckboxMenuItem miButtonsOn = 
        new CheckboxMenuItem(Translator.getMessage("label.on"));
    protected CheckboxMenuItem miButtonsOff = 
        new CheckboxMenuItem(Translator.getMessage("label.off"));

    protected CheckboxMenuItem[] fontSizeMenuItems;
    protected CheckboxMenuItem[] fontStyleMenuItems;

    public ChatRoom(String room, String title, IChatGUIFactory factory, ChatGUI mainGui,
                    IChatClientInputReceiver inputReceiver, IChatServer server)
    {
        _factory = factory;
        _server = server;
        setTitle(title);
        _mainGUI = mainGui;
        _room = room;
        resize(600, 400);
        _chatPanel = createPanel(factory, room, title, inputReceiver);
        setLayout(new GridLayout(1, 1));
        add(_chatPanel);
        setMenuBar(createMenuBar());
    }

    public ChatRoom(String room, IChatGUIFactory factory, ChatGUI mainGui,
                    IChatClientInputReceiver inputReceiver, IChatServer server)
    {
        this(room, room, factory, mainGui, inputReceiver, server);
    }

    protected EnhancedChatPanel createPanel(IChatGUIFactory factory, String name, 
                                            String title, 
                                            IChatClientInputReceiver inputReceiver)
    {
        return new EnhancedChatPanel(factory, name, title, inputReceiver);
    }

    public void show() {
        super.show();
        _chatPanel.requestFocus();
        _mainGUI.hideLogin();
    }

    protected MenuBar createMenuBar() {
        MenuBar mb = new MenuBar();
        fileMenu = new Menu(Translator.getMessage("label.file"));
        fileMenu.add(new MenuItem(Translator.getMessage("label.close")));
        mb.add(fileMenu);

        Menu m = createFontMenu();
        mb.add(m);

        m = _chatPanel.createActionMenu();
        mb.add(m);

        m = createOptionsMenu();
        mb.add(m);

        helpMenu = new Menu(Translator.getMessage("label.help"));
        helpMenu.add(new MenuItem(Translator.getMessage("label.about")));
        mb.setHelpMenu(helpMenu);

        return mb;
    }

    protected Menu createOptionsMenu() {
        Menu buttonBar = new Menu(Translator.getMessage("label.buttonbar"));
        buttonBar.add(miButtonsOn);
        miButtonsOn.setState(true);
        buttonBar.add(miButtonsOff);

        Menu m = new Menu(Translator.getMessage("label.options"));
        m.add(buttonBar);
        return m;
    }

    protected Menu createFontMenu() {
        fontSizeMenu = new Menu(Translator.getMessage("label.size"));
        fontSizeMenuItems = new CheckboxMenuItem[18];
        for (int i=6; i < 24; i++) {
            CheckboxMenuItem cb = new CheckboxMenuItem(String.valueOf(i));
            fontSizeMenu.add(cb);
            if (i == 10) {
                cb.setState(true);
            }
            fontSizeMenuItems[i-6] = cb;
        }
        fontStyleMenu = new Menu(Translator.getMessage("label.style"));
        
        String[] fonts = Toolkit.getDefaultToolkit().getFontList();
        fontStyleMenuItems = new CheckboxMenuItem[fonts.length];
        for (int i=0; i < fonts.length; i++) {
            CheckboxMenuItem cb = new CheckboxMenuItem(fonts[i]);
            fontStyleMenu.add(cb);
            if ("Dialog".equalsIgnoreCase(fonts[i])) {
                cb.setState(true);
            }
            fontStyleMenuItems[i] = cb;
        }
        Menu m = new Menu(Translator.getMessage("label.font"));
        m.add(fontSizeMenu);
        m.add(fontStyleMenu);
        return m;
    }

    public String getName() {
        return _room;
    }

    public boolean handleEvent(Event e) {
        if (e.target == this && e.id == Event.WINDOW_DESTROY) {
            _server.partRoom(_room);
        } else if (e.id == Event.GOT_FOCUS) {
            _mainGUI.setStatusGui(this);
        }
        return super.handleEvent(e);
    }

    public void setFont(String name) {
        _chatPanel.setFont(name);
    }

    public void setFontSize(int size) {
        _chatPanel.setFontSize(size);
    }

    public void handleMenuEvent(Event e, Object arg0) {
        if (arg0 == null) {
            return;
        }
        String arg = arg0.toString();
        if (Translator.getMessage("label.close").equals(arg)) {
            _server.partRoom(_room);
            return;
        } else if (Translator.getMessage("label.about").equals(arg)) {
            _factory.createAboutDialog();
            return;
        }

        if (e.target == miButtonsOn) {
            _chatPanel.showBigButtons(true);
            miButtonsOn.setState(true);
            miButtonsOff.setState(false);
            return;
        }
        if (e.target == miButtonsOff) {
            _chatPanel.showBigButtons(false);
            miButtonsOn.setState(false);
            miButtonsOff.setState(true);
            return;
        }
        
        if (e.target instanceof CheckboxMenuItem) {
            arg = ((CheckboxMenuItem)e.target).getLabel();
            try {
                int size = Integer.parseInt(arg);
                setFontSize(size);
                checkboxSelect(fontSizeMenuItems, arg);
                return;
            }
            catch (NumberFormatException ex) {
            }
            String font = arg;
            setFont(arg);
            checkboxSelect(fontStyleMenuItems, arg);
            return;
        }
        
        // this doesn't feel like it's a good thing, but it works..
        _chatPanel.action(e, arg);
    }


    void checkboxSelect(CheckboxMenuItem[] items, String arg) {
        for (int i=0; i < items.length; i++) {
            if (!items[i].getLabel().equals(arg)) {
                items[i].setState(false);
            } else {
                items[i].setState(true);
            } 
        }
    }

    public boolean action(Event e, Object arg) {
        if (e.target instanceof MenuItem) {
            handleMenuEvent(e, arg);
            return true;
        }

       return super.action(e, arg);
    }

    public void displayPrivateMessage(String user, String message) {
        _chatPanel.displayPrivateMessage(user, message);
    }

    public void displayPrivateEmote(String user, String message) {
        _chatPanel.displayPrivateEmote(user, message);
    }

    public void displayMessage(String user, String message) {
        _chatPanel.displayMessage(user, message);
    }

    public void displayMessage(String message) {
        _chatPanel.displayMessage(message);
    }

    public void displayError(String error) {
        _chatPanel.displayError(error);
    }

    public void setUserList(String[] users) {
        _chatPanel.setUserList(users);
    }

    public void userJoinedRoom(String user) {
        _chatPanel.userJoinedRoom(user);
    }

    public void userPartedRoom(String user, boolean signoff) {
        _chatPanel.userPartedRoom(user, signoff);
    }
}


