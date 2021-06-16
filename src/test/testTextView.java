package test;

import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Color;
import java.awt.Event;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;

import com.lyrisoft.awt.ScrollView;
import com.lyrisoft.awt.TextStyle;
import com.lyrisoft.awt.TextView;

public class testTextView extends Applet implements Runnable {
    private boolean _keepgoing;
    private TextView tv;
    private Button button;

    public void init() {
        tv = new TextView(true);
        ScrollView sv = new ScrollView(tv);
        Font f = new Font("Dialog", Font.PLAIN, 12);

        setLayout(new GridLayout(1, 1));
        add(sv);

        TextStyle normal = new TextStyle(f, Color.black);
        TextStyle red = new TextStyle(f, Color.red);
        TextStyle blue = new TextStyle(f, Color.blue);
        TextStyle green = new TextStyle(f, Color.green);
        TextStyle magenta = new TextStyle(f, Color.magenta);

        tv.append("[START]", normal);
        tv.append("This here is a very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very very  long line.",
                  normal);
        for (int i=0; i < 50; i++) {
            tv.append(i++ + ". The quick brown fox jumped over the lazy, lazy dog.", red);
            tv.append(i++ + ". The quick brown fox jumped over the lazy, lazy dog.", blue);
            tv.append(i++ + ". The quick brown fox jumped over the lazy, lazy dog.", green);
            tv.append(i++ + ". The quick brown fox jumped over the lazy, lazy dog.", magenta);
        }
        tv.append("[END]", normal);

        button = new Button("Start AutoScroll");
        setLayout(new BorderLayout());
        add("Center", sv);
        add("South",  button);
    }

    public void run() {
        while (_keepgoing) {
            tv.append("-- MARK --\n");
            try {
                Thread.sleep(1500);
            }
            catch (InterruptedException e) {}
        }
    }

    public boolean action(Event e, Object arg) {
        if (e.target == button) {
            if (arg.equals("Start AutoScroll")) {
                _keepgoing = true;
                (new Thread(this)).start();
                button.setLabel("Stop AutoScroll");
            } else {
                _keepgoing = false;
                button.setLabel("Start AutoScroll");
            }
            return true;
        }
        return super.action(e, arg);
    }

    public void start() {
    }

    public void stop() {
        _keepgoing = false;
    }

    public static void main(String[] args) {
        Frame f = new Frame();
        f.setLayout(new GridLayout());
        Applet a = new testTextView();
        a.init();
        f.add(a);
        f.resize(500, 70);
        a.start();
        f.show();
    }
}
