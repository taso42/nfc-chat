/*
 * Copyright (c) 2000 Lyrisoft Solutions, Inc.
 * Used by permission
 */
package com.lyrisoft.awt;

import java.awt.Color;
import java.awt.Container;
import java.awt.Event;
import java.awt.Frame;

/**
 * Subclass of TextView that handles hyperlinks.
 */
public class HyperlinkTextView extends TextView {
    private HyperlinkReceiver _hyperlinkReceiver;
    private Frame _frame;

    /**
     * @param autoScroll whether or not to always scroll to the bottom
     * @param receiver the object to notify when hyperlink is clicked
     */
    public HyperlinkTextView(boolean autoScroll, HyperlinkReceiver receiver) {
        super(autoScroll);
        _hyperlinkReceiver = receiver;
    }

    /**
     * helper method used to find the beginning index of an email address
     * @param s the string
     * @param idx the index of the '@' sign
     */
    static int findStartOfEmail(String s, int idx) {
        char[] chars = s.toCharArray();
        for (int i=idx; i >= 0; i--) {
            char c = chars[i];
            if ( Character.isLetterOrDigit(c)
                 || ( i>0 && ( c == '-' || c == '.' || c == '_' ) ) )
            {
                continue;
            } else {
                return i+1;
            }
        }
        return 0;
    }

    /**
     * helper method that finds the end of a hyperlink (or email address)
     * @param s the string
     * @param start the url's starting index
     */
    static int findEndOfHref(String s, int start) {
        char[] chars = s.toCharArray();
        int end = s.length();
        for (int i=start; i < end; i++) {
	    char c = chars[i];
            if (Character.isLetterOrDigit(c))
            {
                continue;
            };
            switch (c) {
            case '.':
            case '?':
            case '&':
            case '=':
            case '-':
            case '_':
            case '/':
            case '#':
            case ':':
            case '~':
            case '%':
            case '+':
                continue;
            default:
                return i;
            }
        }
        return end;
    }

    public void append(String s, TextStyle style) {
        boolean appendHttp = false;
        int lIndex = s.indexOf("<a href=\"");
        int hIndex = s.indexOf("http://");
        if (hIndex == -1) {
            hIndex = s.indexOf("www.");
            appendHttp = true;
        }
        int eIndex = s.indexOf("@");
        if (lIndex >= 0) {
            super.append(s.substring(0, lIndex), style);
            int endHrefIndex = s.indexOf("\">");
            int endLinkIndex = s.indexOf("</a>");
            if ((endHrefIndex > lIndex) & (endLinkIndex > endHrefIndex)){
               int endIndex = endLinkIndex+4;
               int startTextIndex = endHrefIndex+2;
               int startHrefIndex = lIndex+9;
               String href = s.substring(startHrefIndex, endHrefIndex);
               String text = s.substring(startTextIndex, endLinkIndex);
               TextStyle linkStyle = new TextStyle(_defaultStyle);
               linkStyle.setColor(Color.blue);
               HyperlinkTextRun run = new HyperlinkTextRun(text, href, linkStyle);
               super.append(run);
               if (endIndex != s.length()) {
                  append(s.substring(endIndex, s.length()), style);
               }
            }
        } else if (hIndex >= 0) {
            super.append(s.substring(0, hIndex), style);
            int endIndex = findEndOfHref(s, hIndex);
            TextStyle linkStyle = new TextStyle(_defaultStyle);
            linkStyle.setColor(Color.blue);
            String href = s.substring(hIndex, endIndex);
            HyperlinkTextRun run;
            if (appendHttp) {
                run = new HyperlinkTextRun(href, "http://" + href, linkStyle);
            } else {
                run = new HyperlinkTextRun(href, href, linkStyle);
            }
            super.append(run);
            if (endIndex != s.length()) {
                append(s.substring(endIndex, s.length()), style);
            }
        } else if (eIndex >= 0) {
            int start =  findStartOfEmail(s, eIndex-1);
            super.append(s.substring(0, start));
            int endIndex = findEndOfHref(s, eIndex+1);
            TextStyle linkStyle = new TextStyle(_defaultStyle);
            linkStyle.setColor(Color.blue);
            String addr = s.substring(start, endIndex);
            HyperlinkTextRun run = new HyperlinkTextRun(addr, "mailto:" + addr, linkStyle);
            super.append(run);
            if (endIndex != s.length()) {
                append(s.substring(endIndex, s.length()), style);
            }
        } else {
            super.append(s, style);
        }
    }

    /**
     * Get the TextRun at location (x, y)
     */
    synchronized TextRun getRun(int x, int y) {
        if (_firstRun == -1) {
            return null;   
        }
        
        int len = _runs.size();
        for (int i = _firstRun; i < len; i++) {
            TextRun run = (TextRun)_runs.elementAt(i);
            
            if (run.contains(x, y, _yTranslation)) {
                return run;
            }
        }
        return null;
    }

    /**
     * Overridden so we can (sneakily) get a handle to parent Frame
     * object, so that we can change the cursor on it, when a hyperlink 
     * is moused-over.
     */
    public void addNotify() {
        super.addNotify();
        _frame = getFrame();
    }

    /**
     * Try to walk up the container tree and get the Frame at the very
     * top.  If a Frame is not found, null is returned.
     */
    Frame getFrame() {
        Container c = getParent();
        do {
            if (c instanceof Frame) {
                return (Frame)c;
            } else {
                c = c.getParent();
            }
        } while (c != null);
        return null;
    }

    /**
     * Set the cursor based on the what is underneath the mouse.  If
     * the mouse is over a HyperlinkTextRun, the cursor is changed to
     * a hand.  Otherwise, the default cursor is shown.
     */
    void setCursor(int x, int y) {
        TextRun run = getRun(x, y);
        if (run == null) {
            _frame.setCursor(Frame.DEFAULT_CURSOR);
        } else {
            if (run instanceof HyperlinkTextRun) {
                _frame.setCursor(Frame.HAND_CURSOR);
            } else {
                _frame.setCursor(Frame.DEFAULT_CURSOR);
            }
        }
    }
    
    /**
     * We catch MOUSE_DOWN and MOUSE_MOVE events to handle clicks on
     * hyperlinks and to determine that the cursor should look like.
     */
    public boolean handleEvent(Event e) {
        if (e.id == Event.MOUSE_DOWN) {
            TextRun run = getRun(e.x, e.y);
            if (run == null) {
                return true;
            }
            if (run instanceof HyperlinkTextRun) {
                _hyperlinkReceiver.handleHyperlink(((HyperlinkTextRun)run).getHref());
            }
            return true;
        }
        if (e.id == Event.MOUSE_MOVE) {
            setCursor(e.x, e.y);
            return true;
        }
        return super.handleEvent(e);
    }
}
