package com.carnageblender.chat;

import java.applet.Applet;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Label;
import java.awt.List;
import java.awt.Panel;
import java.awt.TextArea;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.Hashtable;

public class Client extends Applet implements ItemListener {
	public static final Label inputLbl = new Label("Chat:");
	public static final Label pmInputLbl = new Label("PM:");
	public static final Label listLbl = new Label("PM to: ");
	public static final String version = "2.3.10";
	public static final Font smallFont = new Font("SansSerif", Font.PLAIN, 10);

	private static final boolean debug = true;
	// whether to print joined/left room msgs
	public static boolean SHOW_JOIN = true;

	// GUI stuff
	private Net net = null;
	private List list = null; // users
	private Panel textPanel = null;
	private TextField input = null;
	private TextField pmInput = null;
	private TextField pmTarget = null;
	private Thread watchdog;
	// these are public so appendLine callers can use 'em
	public TextArea globalText = null;
	public TextArea privateText = null;

	// internal data structures
	private Hashtable slashCommands = new Hashtable();
	private Hashtable slashDescriptions = new Hashtable();

	private String channel;
	private String user, password;
	private String host = null;
	private int port = 7777;

	public void init() {
		GridBagLayout panelGridBag = new GridBagLayout();
		textPanel = new Panel(panelGridBag);
		input = new TextField("");
		pmInput = new TextField("");
		pmTarget = new TextField("");
		list = new List();
		globalText = new TextArea(
			"Chat Blender " + version + " starting...",
			5,
			40,
			TextArea.SCROLLBARS_VERTICAL_ONLY);
		privateText = new TextArea(
			"Private messages:\nClick on the userlist to PM someone, or type his name in the PM box\ntype /help to list commands",
			5,
			40,
			TextArea.SCROLLBARS_VERTICAL_ONLY);

		GridBagLayout gridBag = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		setFont(smallFont);
		setLayout(gridBag);

		/* textpanel contains the textareas & inputs */
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 4.0;
		c.weighty = 1.0;
		c.gridwidth = 1;
		c.gridheight = 2;
		gridBag.setConstraints(textPanel, c);

		// globalText
		c.weightx = 2.0;
		c.gridwidth = 2;
		c.gridheight = 1;
		panelGridBag.setConstraints(globalText, c);
		globalText.setFont(new Font("SansSerif", Font.PLAIN, 12));
		globalText.setEditable(false);

		// privateText
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.weightx = 1.0;
		panelGridBag.setConstraints(privateText, c);
		privateText.setFont(smallFont);
		privateText.setEditable(false);

		// labels
		c.fill = GridBagConstraints.NONE;
		c.gridwidth = 1;
		c.weightx = 0;
		c.weighty = 0;
		panelGridBag.setConstraints(inputLbl, c);
		panelGridBag.setConstraints(pmInputLbl, c);
		gridBag.setConstraints(listLbl, c);

		// textfields
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1.0;
		panelGridBag.setConstraints(input, c);
		panelGridBag.setConstraints(pmInput, c);
		gridBag.setConstraints(pmTarget, c);
		input.setFont(smallFont);
		pmInput.setFont(smallFont);
		ActionListener inputListener = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				input_actionPerformed(ae);
				input.setText("");
			}
		};
		input.addActionListener(inputListener);
		pmInput.addActionListener(inputListener);

		// add to panel
		textPanel.add(globalText);
		textPanel.add(privateText);
		textPanel.add(inputLbl);
		textPanel.add(input);
		textPanel.add(pmInputLbl);
		textPanel.add(pmInput);

		/* user list & pm targets are just dropped in applet */
		c.fill = GridBagConstraints.BOTH;
		c.weighty = 1.0;
		c.gridwidth = GridBagConstraints.REMAINDER;
		gridBag.setConstraints(list, c);
		list.addItemListener(this);
		list.setFont(new Font("Monospaced", Font.PLAIN, 12));

		add(textPanel);
		add(list);
		add(listLbl);
		add(pmTarget);

		password = getParameter("password");

		// no sense in parameterizing this since java security model won't let us
		// use any host but the one the jar was d/l'd from anyway
		host = getCodeBase().getHost();
		System.out.println("host = " + host);
		if (getParameter("port") != null) {
			Integer j = new Integer(getParameter("port"));
			port = j.intValue();
		}

		user = getParameter("user");

		channel = getParameter("channel");

		registerSlashCommand("clear");
		registerSlashCommand("help");
		registerSlashCommand("ignore");
		registerSlashCommand("me");
		registerSlashCommand("join");
		registerSlashCommand("kick");
		registerSlashCommand("kill");
		registerSlashCommand("op");
		registerSlashCommand("rooms");
		registerSlashCommand("who");

		net = new Net(this);
		net.connect();

		// watchdog for disconnects
		watchdog = new WatchDog();
		watchdog.start();
	}

	public void setUser(String s) {
		user = s;
	}
	public String getUser() {
		return user;
	}
	public String getPassword() {
		return password;
	}
	public void setChannel(String s) {
		channel = s;
	}
	public String getChannel() {
		return channel;
	}

	public Socket getSocket() throws UnknownHostException, IOException {
		return new Socket(host, port);
	}

	public void input_actionPerformed(ActionEvent e) {
		String s = e.getActionCommand();
		if (s != null && s.length() != 0) {
			TextField source = (TextField)e.getSource();
			if (s.charAt(0) == '/') {
				int i = s.indexOf(' ');
				String cmd = (i == -1) ? s.substring(1) : s.substring(1, i);
				s = (i == -1) ? "" : s.substring(i + 1);
				handleSlashCommand(source, cmd, s);
			} else {
				if (e.getSource() == pmInput) {
					String t = pmTarget.getText();
					if (t.length() > 0) {
						privMsg(t, s);
					} else {
						appendLine("Error: no PM target specified", privateText);
					}
				} else {
					net.protocol.sayToRoom(channel, s);
					appendLine("<" + user + "> " + s);
				}
			}
			source.setText("");
		}
	}
	
	private void privMsg(String user, String what) {
		net.protocol.sayToUser(user, what);
		appendLine("=> " + user + " " + what, privateText);
		
	}

	/** user is leaving */
	public void userLeftChannel(String user) {
		unPrivate(user);
		try {
			list.remove(user); // throws if not found
			if (SHOW_JOIN && !isIgnored(user)) {
				appendLine(user + " left the room.");
			}
		} catch (Exception e) {
			// do nothing; this is expected if user leaves server from a room I'm not in
		}
	}

	/** if speaking to user in private, turn off private, inform client,
	return true
	 */
	public boolean unPrivate(String user) {
		debug("unPrivate: " + pmTarget.getText() + "/" + user);
		if (user != null
			&& user.compareTo(pmTarget.getText()) == 0)
		{
			list.deselect(list.getSelectedIndex());
			appendLine("No longer speaking in private.", privateText);
			pmTarget.setText("");
			return true;
		}
		return false;
	}

	// for user list
	public void itemStateChanged(ItemEvent e) {
		if (e.getStateChange() == ItemEvent.SELECTED) {
			if (!unPrivate(list.getSelectedItem())) {
				pmTarget.setText(list.getSelectedItem().toString());
			}
		}
	}

	public void update(Graphics g) {
		paint(g);
	}

	public void stop() {
		repaint();
	}

	public synchronized void destroy() {
		net.stopNicely();
		net = null;
		globalText = null;
		privateText = null;
		input = null;
		list = null;
		pmTarget.setText("");
	}

	public void addUser(String u) {
		addUser(u, false);
	}

	public void addUser(String u, boolean verbose) {
		// we need to check for already-present; for instance, 
		// someone else could join after we get ack_join but before we send /who
		if (!listContains(u)) {
			list.add(u);
			if (verbose && SHOW_JOIN && !isIgnored(u)) {
				appendLine(u + " has joined the room.");
			}
		}
	}

	private boolean listContains(String s) {
		int len = list.getItemCount();
		for (int i = 0; i < len; i++) {
			if (s.equals(list.getItem(i))) {
				return true;
			}
		}
		return false;
	}

	public void resetUserList() {
		// check for null; net.disconnect() calls this, but destroy()
		// may have nulled list by the time disconnect() gets that far
		if (list != null) {
			list.removeAll(); // reset users
		}
	}

	public void appendLine(String s) {
		appendLine(s, globalText);
	}

	public void appendLine(String s, TextArea area) // 
	{
		if (area != null) {
			area.append("\n" + s);
		} else {
			debug("null area trying to append " + s);
		}
	}

	public static final int CHANNEL = 1;
	public static final int PRIVATE = 2;
	public static final int EMOTE = 4;

	public void appendMsg(String from, String msg, int type) {
		if ((type & CHANNEL) != 0) {
			if (from.equals(user)) {
				return;
			}
		}
		TextArea target = (type & PRIVATE) != 0 ? privateText : globalText;
		if ((type & EMOTE) != 0) {
			appendLine("* " + from + " " + msg, target);
		} else {
			appendLine("<" + from + "> " + msg, target);
		}
	}

	public void stopNicely() {
		Net n = net;
		net = null; // stop watchdog
		n.stopNicely();
	}

	private class WatchDog extends Thread {
		// TIMEOUT needs to be tuned to be slightly longer than
		// irc server's timeout value
		private static final int TIMEOUT = 260000; // in ms

		public void run() {
			while (net != null) {
				if (System.currentTimeMillis() - net.last_sent > TIMEOUT) {
					net.reconnect("Ping timeout to server");
					// slow down reconnect attempts if they're failing
					net.last_sent = System.currentTimeMillis() - TIMEOUT / 2;
				}
				try {
					sleep(TIMEOUT / 20);
				} catch (InterruptedException e) {
				}
			}
			debug("Watchdog thread exiting");
		}
	}

	private void registerSlashCommand(String cmd) {
		try {
			String cmdMethod = cmd + "SlashCommand";
			String descMethod = cmd + "SlashDescription";
			Method m = null;
			try {
				m = Client.class.getMethod(
					cmdMethod,
					new Class[] {String.class});
			} catch (Exception e2) {
				m = Client.class.getMethod(
					cmdMethod,
					new Class[] {TextField.class, String.class});
			}
			slashCommands.put(cmd, m);
			slashDescriptions
				.put(cmd, Client.class.getMethod(descMethod, new Class[] {
			}));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void handleSlashCommand(TextField source, String cmd, String s) {
		Method m = (Method)slashCommands.get(cmd);
		if (m == null) {
			appendLine("/"
				+ cmd
				+ " is not a supported command.  /help to list commands.");
		} else {
			try {
				if (m.getParameterTypes().length == 1) {
					m.invoke(this, new Object[] {s});
				} else {
					m.invoke(this, new Object[] {source, s});
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void debug(String s) {
		if (debug) {
			System.out.println(s);
		}
	}

	/** 
	 *  COMMAND HANDLERS 
	 *  I'm afraid this isn't as elegant as defining a class to
	 *  handle slash commands and subclassing it for each...  unfortunately,
	 *  that takes 20% more space in the jar so I do it this way.
	 *  As you can see from registerSlashCommand, it is
	 *  _required_ that methods be of the form
	 *  cmd + SlashCommand, cmd + SlashDescription.
	 */

	public void helpSlashCommand(String s) {
		appendLine("Commands available:");
		for (Enumeration e = slashDescriptions.keys(); e.hasMoreElements();) {
			String cmd = (String) e.nextElement();
			Method m = (Method) slashDescriptions.get(cmd);
			try {
				String d = (String) m.invoke(this, new Object[] {
				});
				appendLine("/" + cmd + "\t\t" + d);
			} catch (Exception ex) {
			}
		}
	}
	public String helpSlashDescription() {
		return "lists available commands";
	}

	public void meSlashCommand(TextField source, String s) {
		if (source == input) {
			appendLine("* " + user + " " + s);
			net.protocol.emoteToRoom(channel, s);
		} else {
			appendLine("* " + user + " " + s, privateText);
			net.protocol.emoteToUser(pmTarget.getText(), s);
		}
	}
	public String meSlashDescription() {
		return "emotes.  Example: /me waves";
	}

	public void clearSlashCommand(String s) {
		boolean cleared = false;
		if (s.equalsIgnoreCase("private") || s.length() == 0) {
			privateText.setText("");
			cleared = true;
		}
		if (s.equalsIgnoreCase("public") || s.length() == 0) {
			globalText.setText("");
			cleared = true;
		}
		if (!cleared) {
			appendLine("Error: /clear [public|private]");
		}
		
	}
	public String clearSlashDescription() {
		return "clears windows.  /clear [public|private] for individual windows, or /clear alone for both.";
	}

	public void joinSlashCommand(String s) {
		net.protocol.partRoom(channel);
		resetUserList();
		net.protocol.joinRoom(s);
	}
	public String joinSlashDescription() {
		return "switches to target room";
	}

	public void kickSlashCommand(String s) {
		net.protocol.kick(channel, s);
	}
	public String kickSlashDescription() {
		return "boots target from room";
	}

	public void killSlashCommand(String s) {
		net.protocol.kill(s);
	}
	public String killSlashDescription() {
		return "boots target from server";
	}

	public void opSlashCommand(String s) {
		net.protocol.op(channel, s);
	}
	public String opSlashDescription() {
		return "grants op rights to target in this room";
	}

	public void roomsSlashCommand(String s) {
		net.protocol.requestRoomList();
	}
	public String roomsSlashDescription() {
		return "displays list of rooms";
	}

	public void whoSlashCommand(String s) {
		net.protocol.requestUsersInRoomList(s);
	}
	public String whoSlashDescription() {
		return "lists users in target room";
	}

	// need some client-side intelligence for ignore, so we can
	// not output a join message for ignorees.
	private Hashtable ignored = new Hashtable();
	
	public void addIgnored(String name) {
		ignored.put(name.toLowerCase(), new Boolean(true));
	}

	public void ignore(String name) {
		net.protocol.ignore(name, "");
		addIgnored(name);
	}
	public void unignore(String name) {
		net.protocol.unIgnore(name);
		ignored.remove(name.toLowerCase());
	}
	public boolean isIgnored(String name) {
		return ignored.containsKey(name.toLowerCase());
	}
	public Enumeration getIgnoredE() {
		return ignored.keys();
	}

	public void ignoreSlashCommand(String s) {
		if (s.length() > 0) {
			if (isIgnored(s)) {
				unignore(s);
			} else {
				ignore(s);
			}
		} else {
			appendLine("Ignoring:");
			for (Enumeration i = getIgnoredE(); i.hasMoreElements();) {
				String name = (String)i.nextElement();
				appendLine("\t" + name);
			}

		}
	}
	public String ignoreSlashDescription() {
		return "toggles ignore of target.  /ignore without target lists ignored users";
	}
}
