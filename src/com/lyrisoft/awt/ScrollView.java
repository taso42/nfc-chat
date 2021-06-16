/*
 * Copyright (c) 2000 Lyrisoft Solutions, Inc.
 * Used by permission
 */
package com.lyrisoft.awt;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Event;
import java.awt.Panel;
import java.awt.Scrollbar;

/**
 * A Scrollable View that a TextView can be placed in.
 */
public class ScrollView extends Panel {
    TextView _view;
    Scrollbar _scrollbar;
    static boolean WORKAROUND_ENABLED;

    /**
     * Work-around for the scrolling inconsitency.
     * Details on http://developer.java.sun.com/developer/bugParade/bugs/4070498.html
     */
    static {
        WORKAROUND_ENABLED = ((new Scrollbar(Scrollbar.HORIZONTAL,20,10,0,20)).getValue() == 20);
//        System.err.println("Scrollbar WORKAROUND_ENABLED = " + WORKAROUND_ENABLED);
    }

    /**
     * Construct a ScrollView that will scroll a TextView
     */
    public ScrollView(TextView view) {
        _view = view;
        _scrollbar = new Scrollbar(Scrollbar.VERTICAL);
        _scrollbar.setLineIncrement(10);
        _scrollbar.setBackground(Color.lightGray);
        setLayout(new BorderLayout());
        add("Center", view);
        add("East", _scrollbar);

        view.setScrollView(this);
    }

    /**
     * This method does what its name implies.
     */
    public void scrollToBottom() {
        int max = _scrollbar.getMaximum();
        _scrollbar.setValue(max);
        scroll();
    }

    /**
     * Notification that the bounds of my view object changed.
     * Adjust the ScrollBar appropriately.
     */
    public void boundsChanged() {
        int viewableHeight = _view.size().height;
        int fullHeight = _view.preferredSize().height;

        if (WORKAROUND_ENABLED) {
            fullHeight -= viewableHeight;
        }
        _scrollbar.setValues(_scrollbar.getValue(), viewableHeight, 0, fullHeight);
        scroll();
    }

    /**
     * "scroll" the TextView to the location that the scrollbar is indicating
     */
    void scroll() {
        _view.setYTranslation(_scrollbar.getValue());
        _view.repaint();
    }

    /**
     * Catch events from the ScrollBar.  Upon doing so, call scroll()
     */
    public boolean handleEvent(Event event) {
        if (event.target == _scrollbar) {
            scroll();
            return true;
        } else {
            return super.handleEvent(event);
        }
    }
}
