/*
 * Copyright (c) 2000 Lyrisoft Solutions, Inc.
 * Used by permission
 */
package com.lyrisoft.chat.client.gui.awt102;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Panel;

public class FixedPanel extends Panel {
    private Dimension _size;

    public FixedPanel(Component child, int width, int height) {
        super();
        setLayout(new GridLayout(1, 1));
        add(child);
        _size = new Dimension(width, height);
    }

    public Dimension preferredSize() {
        return _size;
    }

    public Dimension minimumSize() {
        return _size;
    }
}
