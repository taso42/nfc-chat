package com.lyrisoft.chat.client.gui.jfc;

import java.applet.Applet;
import java.applet.AudioClip;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Hashtable;
import java.util.Properties;

import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

import com.lyrisoft.chat.client.ChatClientApplet;
import com.lyrisoft.chat.client.gui.ChatGUI;
import com.lyrisoft.chat.client.gui.IChatClientInputReceiver;
import com.lyrisoft.chat.client.gui.IChatGUIFactory;
import com.lyrisoft.chat.client.gui.IChatRoom;
import com.lyrisoft.chat.client.gui.IConsole;
import com.lyrisoft.chat.client.gui.ILogin;
import com.lyrisoft.chat.client.gui.IPrivateChatRoom;
import com.lyrisoft.chat.client.gui.IQuery;
import com.lyrisoft.chat.server.local.IChatServer;

public class SwingGuiFactory implements IChatGUIFactory {
    private ChatGUI _mainGui;
    private IChatClientInputReceiver _inputReceiver;
    private IChatServer _server;
    private Hashtable _attributes;
    private boolean _playSounds = true;

    private Applet _applet;
    private URL _docBase;
    private JFrame _outterFrame;

    private Hashtable _clips;

    public SwingGuiFactory() {
        _clips = new Hashtable();
        _attributes = new Hashtable();
        _outterFrame = createOutterFrame("NFC Chat");
        _outterFrame.setSize(640, 480);
        _outterFrame.show();

        _outterFrame.addWindowListener(
            new WindowAdapter() {
                    public void windowClosing(WindowEvent e) {
                        if (_server != null && _server.isConnected()) {
                            _server.signOff();
                        }
                        if (_applet == null) {
                            System.exit(0);
                        }
                    }
                }
        );
    }

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

    public java.awt.Component createAboutDialog() {
        return null;
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
    
    // Login 
    public ILogin createLoginDialog() {
        LoginPanel lp = new LoginPanel(_inputReceiver);
        JInternalFrame frame = new JInternalFrame("NFC Login");
        moveWindow(frame);
        frame.setContentPane(lp);
        lp.setContainer(frame);
        frame.pack();
        _outterFrame.getLayeredPane().add(frame);
        return lp;
    }

    public void show(ILogin login) { 
        ((LoginPanel)login).getContainer().setVisible(true);
    }

    public void hide(ILogin login) {
        System.err.println("hide login..");
        ((LoginPanel)login).getContainer().setVisible(false);
    }

    // Console
    public IConsole createConsole() {
        final Console console = new Console(_server, _inputReceiver, this);
        JInternalFrame frame = new JInternalFrame("Chat Console", true, false, true, true);
        moveWindow(frame);
        frame.setContentPane(console);
        frame.addInternalFrameListener(
            new InternalFrameAdapter() {
                    public void internalFrameActivated(InternalFrameEvent e) {
                        _mainGui.setStatusGui(console);
                    }
                }
        );
        console.setContainer(frame);
        frame.pack();
        _outterFrame.getLayeredPane().add(frame);
        return console;
    }

    public void show(IConsole console) {
        ((Console)console).getContainer().setVisible(true);
    }

    public void hide(IConsole console) {
        ((Console)console).getContainer().setVisible(false);
    }

    // ChatRoom
    public IChatRoom createChatRoom(final String name) {
        final ChatPanel panel = new ChatPanel(this, name, _inputReceiver, _server);
        final JInternalFrame frame = new JInternalFrame(name, true, true, true, true);
        moveWindow(frame);
        frame.addInternalFrameListener(
            new InternalFrameAdapter() {
                    public void internalFrameClosing(InternalFrameEvent e) {
                        _server.partRoom(name);
                    }

                    public void internalFrameActivated(InternalFrameEvent e) {
                        _mainGui.setStatusGui(panel);
                    }
                }
        );
        frame.setContentPane(panel);
        panel.setContainer(frame);
        frame.setSize(400, 300);
        _outterFrame.getLayeredPane().add(frame);
        return panel;
    }

    public void show(IChatRoom room) {
        ((ChatPanel)room).getContainer().setVisible(true);
    }

    public void hide(IChatRoom room) {
        ((ChatPanel)room).getContainer().setVisible(false);
    }

    // Private Chat
    public IPrivateChatRoom createPrivateChatRoom(String name) {
        final PrivateChatPanel panel = new PrivateChatPanel(name, this, _mainGui, _inputReceiver, _server);
        JInternalFrame frame = new JInternalFrame("Private chat with " + name, true, true, true, true);
        moveWindow(frame);
        frame.setContentPane(panel);
        panel.setContainer(frame);
        frame.setSize(300, 200);
        frame.addInternalFrameListener(
            new InternalFrameAdapter() {
                    public void internalFrameClosing(InternalFrameEvent e) {
                        _mainGui.closePrivateChatRoom(panel.getName());
                    }
                }
        );
        _outterFrame.getLayeredPane().add(frame);
        return panel;
    }
    
    public void show(IPrivateChatRoom room) {
        ((PrivateChatPanel)room).getContainer().setVisible(true);
    }

    public void hide(IPrivateChatRoom room) {
        ((PrivateChatPanel)room).getContainer().setVisible(false);
    }
    
    public IQuery createQuery(String title, String choiceLabel, String[] choices, 
                              boolean showTextField, String textFieldLabel) 
    {
        return null;
    }

    public void setApplet(ChatClientApplet a) {
        _applet = a;
        try {
            _docBase = new URL(a.getCodeBase(), "resources/");
        }
        catch (MalformedURLException e) {
            return;
        }
//        _mediaTracker = new java.awt.MediaTracker(a);
    }

    public Properties getProperties(String name) {
        if (_docBase == null) {
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
        } else {
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
    }

    public Image getImage(String name) {
        return null;
    }

    public void playAudioClip(String name) {
        if (!_playSounds) {
            return;
        }

        AudioClip clip = (AudioClip)_clips.get(name);
        if (clip != null) {
            clip.play();
            return;
        }

        try {
            if (_docBase == null) {
                if (_applet == null) {
                    String nfcHome = System.getProperty("NFC_HOME");
                    if (nfcHome == null) {
                        System.err.println("Warning:  NFC_HOME is not set and Applet is not set. " +
                                           "Cannot play audio.");
                        // we gave it our best shot.  oh well
                        return;
                    }
                    java.io.File f = new java.io.File(nfcHome);
                    String sUrl = "file:///" + f.getAbsolutePath() + File.separator + 
                            "web" + File.separator +
                            "resources" + File.separator;
                    _docBase = new URL(sUrl);
                } else {
                    _docBase = new URL(_applet.getDocumentBase(), "resources/");
                }
            }
            URL url = new URL(_docBase, name);
            clip = Applet.newAudioClip(url);
            _clips.put(name, clip);
            clip.play();
        }
        catch (MalformedURLException e) {
            System.err.println("Malformed URL");
            e.printStackTrace();
        }
    }

    public void playSounds(boolean b) {
        _playSounds = b;
    }

    public boolean getPlaySounds() {
        return _playSounds;
    }

    public void setAttribute(String name, Object value) {
        _attributes.put(name, value);
    }

    public Object getAttribute(String name) {
        return _attributes.get(name);
    }

    JFrame createOutterFrame(String title) {
        JFrame f = new JFrame(title);
        JDesktopPane desktop = new JDesktopPane();
        f.setLayeredPane(desktop);
        f.setJMenuBar(createMenuBar());
        return f;
    }

    public static void tweakTextFieldSize(JTextField tf) {
        Dimension m = tf.getMaximumSize();
        Dimension p = tf.getPreferredSize();
        tf.setMaximumSize(new Dimension(m.width, p.height));
        tf.setMinimumSize(new Dimension(400, p.height));
        tf.setPreferredSize(new Dimension(400, p.height));
    }

    JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu("File");
        JMenuItem item = new JMenuItem("Exit");
        item.addActionListener(
            new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        if (_applet == null) {
                            System.exit(0);
                        } else {
                            _server.signOff();
                            _outterFrame.hide();
                        }
                    }
                }
        );
        menu.add(item);

        item = new JMenuItem("New room");
        item.addActionListener(
            new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        String roomName = JOptionPane.showInputDialog("Please enter the name of a room to join");
                        if (roomName == null) {
                            return;
                        }
                        if (_server != null && _server.isConnected()) {
                            _server.joinRoom(roomName, null);
                        }
                    }
                }
        );
        menu.add(item);
        menuBar.add(menu);
        return menuBar;
    }

    void moveWindow(Component c) {
        c.move(0, _outterFrame.getJMenuBar().getHeight());
    }

    JPopupMenu createUserPopupMenu(final String user) {
        JPopupMenu menu = new JPopupMenu();
        JMenuItem item = new JMenuItem("/ping " + user);
        menu.add(item);
        item.addActionListener(
            new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        _server.sendPing(user, String.valueOf(System.currentTimeMillis()));
                    }
                }
        );
        
        item = new JMenuItem("/whois " + user);
        menu.add(item);
        item.addActionListener(
            new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        _server.requestUserInfo(user);
                    }
                }
        );
        return menu;
    }

    JPopupMenu createRoomPopupMenu(final String room) {
        JPopupMenu menu = new JPopupMenu();
        JMenuItem item = new JMenuItem("/join " + room);
        menu.add(item);
        item.addActionListener(
            new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        _server.joinRoom(room, null);
                    }
                }
        );

        return menu;
    }
}
