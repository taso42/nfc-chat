package com.carnageblender.chat;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class Net extends Thread implements Runnable {
	/** Are we connected or not? */
	public Boolean connected = new Boolean(false);
	private Socket socket;
	private Thread thread;
	private BufferedWriter os;
	private BufferedReader is;

	// allows Protocol to know whether to print "Joined" msg
	// we want reconnects to be nearly invisible so we set this to false then
	// if we allow user to join other channels in the future, set it to
	// true when doing that.
	public boolean show_join = true;

	public long last_sent = 0;
	public Client client;
	public Protocol protocol;

	// allows disconnect() to wait for run() to finish
	// this is desireable b/c disconnect() is often followed by connect()
	// which creates a new thread and runs it
	private boolean running;

	public Net(Client client) {
		this.client = client;
		protocol = new Protocol(this);
	}

	/** Start a thread. */
	public void start() {
		if (thread == null) {
			thread = new Thread(this);
			thread.start();
		}
	}

	/** The main loop. */
	public void run() {
		String buf;

		running = true;
		while (Thread.currentThread() == thread) {
			try {
				// got to use ready so it doesn't block and if owner
				// tries to kill us, we will exit properly
				synchronized (connected) {
					if (connected.booleanValue() && is.ready()) {
						buf = is.readLine();
						protocol.parse(buf);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				reconnect("Connection lost");
			}
			try {
				sleep(50);
			} catch (InterruptedException e) {
			}
		}
		running = false;
		client.debug("Net run loop exiting");
	}

	// prevent Client and myself from both reconnect()ing at once
	// don't just make method synchronized; if one reconnect
	// call happens while another is in progress, we don't want it
	// to wait and then execute, we want it to exit immediately
	private Boolean reconnecting = new Boolean(false);

	public void reconnect(String msg) {
		synchronized (reconnecting) {
			if (reconnecting.booleanValue() || thread == null)
				return;
			reconnecting = new Boolean(true);
		}

		client.appendLine(
			"("
				+ msg
				+ ".  Reconnecting.  You mave have missed a few seconds of messaging.)");
		client.debug("\t" + msg + ".  Reconnecting");
		disconnect(false);
		show_join = false;
		connect();

		synchronized (reconnecting) {
			reconnecting = new Boolean(false);
		}
	}

	/** Connect to the server. */
	public synchronized void connect() {
		synchronized (connected) {
			if (connected.booleanValue())
				return;

			try {
				socket = client.getSocket();
			} catch (UnknownHostException e) {
				client.appendLine("UnknownHost: " + e);
				return;
			} catch (IOException e) {
				client.appendLine("Error creating Socket: " + e);
				return;
			}

			try {
				os = new BufferedWriter(
						new OutputStreamWriter(socket.getOutputStream()));
				is = new BufferedReader(
						new InputStreamReader(socket.getInputStream()));
				last_sent = System.currentTimeMillis();
			} catch (IOException e) {
				client.appendLine("Error creating streams: " + e);
				return;
			}

			if (os != null && is != null) {
				connected = new Boolean(true);
				notify();

				start(); // does own check for already valid thread
			}
		}
		protocol.signon(client.getUser(), client.getPassword());
	}

	/** Send a message to the server. */
	public void send(String data) {
		try {
			while (!connected.booleanValue()) {
				Thread.sleep(5);
			}
			// FIXME connected could change to false before next lines execute
			// but, can't sync on connected here b/c disconnect() calls us
			os.write(data + "\r\n");
			os.flush();
			last_sent = System.currentTimeMillis();
			client.debug("sent: " + data);
		} catch (Exception e) {
			client.debug("Error: " + e);
			reconnect("Could not send data");
		}
	}

	// on the other hand if disconnect is called by our run() method,
	// there's no reason to restart the thread, so we allow killThread = false
	private synchronized void disconnect(boolean killThread) {
		synchronized (connected) {
			if (!connected.booleanValue())
				return;
			protocol.Signoff();

			os = null;
			is = null;
			try {
				socket.close();
			} catch (IOException e) {
			} // no msg to user by design
			connected = new Boolean(false);
		}

		client.resetUserList();

		if (killThread) {
			client.debug("\tSetting thread = null");
			thread = null;
		}
	}

	public void stopNicely() {
		disconnect(true);
		protocol = null;
	}
}
