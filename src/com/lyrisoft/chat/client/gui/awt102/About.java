/*
 * Copyright (c) 2000 Lyrisoft Solutions, Inc.
 * Used by permission
 */
package com.lyrisoft.chat.client.gui.awt102;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Event;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Panel;
import java.awt.Rectangle;

import com.lyrisoft.awt.HyperlinkReceiver;
import com.lyrisoft.awt.HyperlinkTextRun;
import com.lyrisoft.awt.HyperlinkTextView;
import com.lyrisoft.awt.TextStyle;
import com.lyrisoft.awt.TextView;
import com.lyrisoft.chat.ICommands;
import com.lyrisoft.chat.Translator;
import com.lyrisoft.chat.client.gui.IChatClientInputReceiver;

public class About extends Dialog implements HyperlinkReceiver {
    private Button ok = new Button(Translator.getMessage("label.ok"));
    private HyperlinkTextView textView;
    private IChatClientInputReceiver _inputReceiver;

    public About(Frame owner, IChatClientInputReceiver inputReceiver) {
        super(owner, Translator.getMessage("label.about.nfc"), true);
        _inputReceiver = inputReceiver;
        setLayout(new BorderLayout());

        textView = new HyperlinkTextView(true, this);

        TextStyle linkStyle = new TextStyle(TextView.DEFAULT_FONT, Color.blue);
        textView.append(new HyperlinkTextRun("NFC Chat", "http://nfcchat.sourceforge.net/",
                                              linkStyle));
        textView.append("\n");
        textView.append("Copyright (c) 2000 Lyrisoft Solutions Inc.\n");
        textView.append(" \n");
        textView.append("Licensed under the ");
        textView.append(new HyperlinkTextRun("LGPL", "http://www.gnu.org/copyleft/lesser.txt",
                                             linkStyle));
        textView.append("\n");

        add("Center", textView);

        Panel p = new Panel();
        p.setLayout(new FlowLayout());
        p.add(ok);
        add("South", p);

        textView.resize(320, 200);
        pack();
        center();
        show();
    }

    public void handleHyperlink(String link) {
        _inputReceiver.inputEvent("AboutBox", ICommands.HYPERLINK + " " + link);
    }

    public boolean action(Event e, Object arg) {
        if (e.target == ok) {
            hide();
            return true;
        }
        return super.action(e, arg);
    }

    public static void main(String[] args) {
        Frame f = new Frame();
        About a = new About(f, null);
    }

    public void center() {
        Rectangle pr = getParent().bounds();
        Rectangle r = bounds();
        
        move(Math.max(0, (pr.width - r.width) / 2 + pr.x), Math.max(0, (pr.height - r.height) / 2 + pr.y));
    }

}
