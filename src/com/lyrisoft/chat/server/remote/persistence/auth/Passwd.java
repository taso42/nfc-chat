/*
 * Copyright (c) 2000 Lyrisoft Solutions, Inc.
 * Used by permission
 */
package com.lyrisoft.chat.server.remote.persistence.auth;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;

import jfd.jcrypt;

import com.lyrisoft.chat.server.remote.AccessDenied;
import com.lyrisoft.chat.server.remote.ChatServer;
import com.lyrisoft.util.io.ResourceException;
import com.lyrisoft.util.io.ResourceLoader;

/*
 * Modified 2/26/2003 by Jonathan Ellis
 * - rereading file into memory is nice b/c it allows
 * users to be manually added externally while server is running,
 * but it's prohibitively slow past a few thousand users.
 * changed to only read once.  Now that PasswdAuthenticator
 * will auto-add users, the old feature really isn't necessary.
 * - removed some unnecessary synchronization.  Remember
 * Hashtable synchronizes automatically.
 */

/**
 * Represents a unix-style passwd file with three colon-delimited fields:
 * <li>userId (String)
 * <li>access level (int)
 * <li>crypted password (String)
 */
public class Passwd {
    private static Hashtable records = null;
    private static boolean _warned = false;
    
    public static boolean isExistingUser(String userId)
    {
    	return records.containsKey(userId.toLowerCase());
    }
    
    /**
     * Gets a PasswdRecord.
     *
     * @param userId the user Id
     * @param password the password in plaintext
     * @return the PasswdRecord or null if the user was not found
     * @throw AccessDenied if the user was found, but his password did not match the contents
     *                        of the passwd file.
     */
    public static final PasswdRecord getRecord(String userId, String password)
        throws IOException, AccessDenied
    {
        PasswdRecord r = (PasswdRecord)records.get(userId.toLowerCase());
        if (r == null) {
            return null;
        }
        if (password == null) {
            password = "";
        }
        if (password.length() < 2) {
            ChatServer.log("Access denied: " + userId);
            throw new AccessDenied(userId);
        }
        String salt = r.passwd.substring(0, 2);
        if (jcrypt.crypt(salt, password).equals(r.passwd)) {
            return r;
        } else {
            ChatServer.log("Access denied: " + userId);
            throw new AccessDenied(userId);
        }
    }

    /**
     * Write a PasswdRecord to the passwd file.
     * If an line already existed for the user specified, it gets overwritten, otherwise,
     * it is appended.
     *
     * @param r the record to write
     */
    public static final void writeRecord(PasswdRecord r) throws IOException {
    	// todo: this is horribly inefficient; let's just append the new record!
        records.put(r.userId.toLowerCase(), r);
        save();
    }

    /**
     * Write a new entry to the passwd file.  If an entry already exists for the given userId,
     * it gets overwritten, otherwise, it is appended.  The password specified here gets encrypted.
     *
     * @param userId the user Id
     * @param access the access level
     * @param passwd the plaintext password that will get encrypted
     */
    public static final void writeRecord(String userId, int access, String passwd)
        throws IOException
    {
        PasswdRecord r = new PasswdRecord(userId, access, passwd);
        String salt = String.valueOf(System.currentTimeMillis());
        int len = salt.length();
        salt = salt.substring(len-2, len);
        r.passwd = jcrypt.crypt(salt, passwd);
        writeRecord(r);
    }

    /**
     * Save the in-memory Hashtable of PasswdRecords out to disk.
     */
    private synchronized static final void save() throws IOException {
        PrintWriter out = null;

        try {
            out = new PrintWriter(new FileWriter(getPasswdFileName()));
            for (Enumeration e = records.elements(); e.hasMoreElements(); ) {
                saveRecord(out, (PasswdRecord)e.nextElement());
            }
        }
        finally {
            if (out != null) {
                out.close();
            }
        }
    }

    /**
     * Load the whole passwd file into memory, as a Hashtable of PasswdRecords
     */
	static {
        records = new Hashtable();

        BufferedReader reader = null;
        int lineno = 1;
        try {
            InputStream is = ResourceLoader.getResource("conf/nfc.passwd");
            reader = new BufferedReader(new InputStreamReader(is));
            String s;
            while ((s = reader.readLine()) != null) {
                PasswdRecord r = parseRecord(s);
                records.put(r.userId.toLowerCase(), r);
            }
            lineno++;
        }
        catch (ResourceException e) {
            if (!_warned) {
                ChatServer.logError("Warning: passwd file not found.");
                _warned = true;
            }
        }
        catch (Exception e) {
            ChatServer.logError("Error reading passwd file, line " + lineno);
            ChatServer.log(e);
        }
        finally {
            if (reader != null) {
            	try {
	                reader.close();
            	} catch (Exception e) {
            		ChatServer.log(e);
            	}
            }
        }
    }

    /**
     * Parse a line from the file into a PasswdRecord object
     *
     * @param line the raw colon-delimited line
     * @return PasswdRecord the newly created PasswdRecord
     */
    private static final PasswdRecord parseRecord(String line) throws Exception {
        StringTokenizer st = new StringTokenizer(line, ":");
        if (st.countTokens() < 3) {
            throw new Exception("Not enough tokens");
        }
        return new PasswdRecord(st.nextToken(), Integer.parseInt(st.nextToken()), st.nextToken());
    }

    /**
     * Save an individual PasswdRecord out to a PrintWriter, as colon-delimited strings.
     *
     * @param writer the PrintWriter to write on
     * @param record the record to write
     */
    private static final void saveRecord(PrintWriter writer, PasswdRecord record) throws IOException {
        writer.println(record.userId + ":" + record.access + ":" + record.passwd);
    }

    /**
     * The constant String PASSWD_FILE gets set from this method.  The system property
     * "NFC_HOME" is read for bootstrapping purposes.  If that variable is not set, a
     * RuntimeException is thrown.
     *
     * @return the passwd file as a filename valid on the host OS.
     */
    private static final String getPasswdFileName() {
        StringBuffer sb = new StringBuffer();
        String NFC_HOME = System.getProperty("NFC_HOME");
        if (NFC_HOME == null) {
            System.err.println("Passwd: Warning! Property NFC_HOME not set.");
            return "";
        }
        sb.append(NFC_HOME);

        if (NFC_HOME.charAt(NFC_HOME.length() - 1) != File.separatorChar) {
            sb.append(File.separator);
        }
        sb.append("conf");
        sb.append(File.separator);
        sb.append("nfc.passwd");

        return sb.toString();
    }

	public static void main(String[] args) {
		try {
			writeRecord(args[0], Integer.parseInt(args[1]), args[2]);
		}
		catch (IOException e) {
			System.err.println("An I/O error occurred: " + e.getMessage());
		}
		catch (Exception e) {
			showUsageAndExit();
		}
	}

	private static final void showUsageAndExit() {
		System.err.println("Passwd Program.  Adds new line to the passwd file, encrypting the password.");
		System.err.println("usage: java com.lyrisoft.chat.server.remote.auth.Passwd " +
						   "[user id] [access level] [password]");
		System.exit(1);
	}

}
