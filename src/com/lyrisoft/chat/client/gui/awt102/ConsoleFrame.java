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

import com.lyrisoft.chat.Translator;
import com.lyrisoft.chat.client.gui.ChatGUI;
import com.lyrisoft.chat.client.gui.IChatClientInputReceiver;
import com.lyrisoft.chat.client.gui.IChatGUIFactory;
import com.lyrisoft.chat.client.gui.IConsole;
import com.lyrisoft.chat.client.gui.IQuery;
import com.lyrisoft.chat.server.local.IChatServer;

public class ConsoleFrame extends Frame implements IConsole {
    protected ChatGUI _mainGUI;
    private Console _console;
    private IChatServer _server;
    private IChatGUIFactory _factory;

    private CheckboxMenuItem miSoundOn = 
        new CheckboxMenuItem(Translator.getMessage("label.on"));
    private CheckboxMenuItem miSoundOff = 
        new CheckboxMenuItem(Translator.getMessage("label.off"));
    private MenuItem miExit = 
        new MenuItem(Translator.getMessage("label.exit"));
    private MenuItem miAbout = 
        new MenuItem(Translator.getMessage("label.about"));

    public ConsoleFrame(IChatServer server, IChatClientInputReceiver receiver, ChatGUI mainGUI, 
                        IChatGUIFactory f) 
    {
        setTitle(Translator.getMessage("label.console"));
        _mainGUI = mainGUI;
        _server = server;
        _factory = f;
        _console = new Console(server, receiver, f);
        setLayout(new GridLayout(1, 1));
        add(_console);
        setMenuBar(createMenuBar());
        pack();
    }

    public MenuBar createMenuBar() {
        Menu file = new Menu(Translator.getMessage("label.file"));
        file.add(miExit);
        Menu options = new Menu(Translator.getMessage("label.options"));
        Menu sounds = new Menu(Translator.getMessage("label.sound"));
        miSoundOn = new CheckboxMenuItem(Translator.getMessage("label.on"));
        if (_factory.getPlaySounds()) {
            miSoundOn.setState(true);
        }
        sounds.add(miSoundOn);
        if (miSoundOff == null) {
            if (!_factory.getPlaySounds()) {
                miSoundOn.setState(true);
            }
        }
        sounds.add(miSoundOff);
        options.add(sounds);
        Menu help = new Menu(Translator.getMessage("label.help"));
        help.add(miAbout);

        MenuBar mb = new MenuBar();
        mb.add(file);
        mb.add(options);
        mb.setHelpMenu(help);
        return mb;
    }

    public void addRoom(String room, String count, boolean inform) {
        _console.addRoom(room, count, inform);
    }

    public void addUser(String user, boolean inform) {
        _console.addUser(user, inform);
    }

    public void removeUser(String user, boolean inform) {
        _console.removeUser(user, inform);
    }

    public void removeRoom(String room, boolean inform) {
        _console.removeRoom(room, inform);
    }


    public void clearRooms() {
        _console.clearRooms();
    }

    public void clearUsers() {
        _console.clearUsers();
    }

    public boolean handleEvent(Event e) {
        if (e.target == this && e.id == Event.WINDOW_DESTROY) {
            _server.signOff();
            hide();
            return true;
        } else if (e.id == Event.GOT_FOCUS) {
            _mainGUI.setStatusGui(this);
            return true;
        }
        return super.handleEvent(e);
    }

    public void displayPrivateMessage(String user, String message) {
        _console.displayPrivateMessage(user, message);
    }

    public void displayMessage(String user, String message) {
        _console.displayMessage(user, message);
    }

    public void displayMessage(String message) {
        _console.displayMessage(message);
    }

    public void displayError(String error) {
        _console.displayError(error);
    }

    public void displayPrivateEmote(String user, String message) {
        _console.displayPrivateEmote(user, message);
    }

    public boolean action(Event e, Object arg) {
        if (e.target == miSoundOn) {
            _factory.playSounds(true);
            miSoundOff.setState(false);
            return true;
        }
        
        if (e.target == miSoundOff) {
            _factory.playSounds(false);
            miSoundOn.setState(false);
            return true;
        }
        
        if (e.target == miAbout) {
            _factory.createAboutDialog();
            return true;
        }

        if (e.target == miExit) {
            IQuery q = _factory.createQuery(Translator.getMessage("confirm.exit"), 
                                            null, null, false, null);
            q.show();
            if (!q.getCanceled()) {
                _server.signOff();
            }
            return true;
        }

        return super.action(e, arg);
    }

    public void show() {
        super.show();
//        _mainGUI.hideLogin();
    }

    public void hide() {
        super.hide();
        _console.pleaseStop();
    }
}

