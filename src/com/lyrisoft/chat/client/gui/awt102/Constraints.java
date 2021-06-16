/*
 * Copyright (c) 2000 Lyrisoft Solutions, Inc.
 * Used by permission
 */
package com.lyrisoft.chat.client.gui.awt102;

import java.awt.GridBagConstraints;
import java.awt.Insets;

public class Constraints {
    public static GridBagConstraints create(int gridx, int gridy, int gridwidth,
                                            int gridheight, double weightx, double weighty,
                                            int anchor, int fill,
                                            Insets insets, int ipadx, int ipady)
    {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = gridx;
        gbc.gridy = gridy;
        gbc.gridwidth = gridwidth;
        gbc.gridheight = gridheight;
        gbc.weightx = weightx;
        gbc.weighty = weighty;
        gbc.anchor = anchor;
        gbc.fill = fill;
        gbc.insets = insets;
        gbc.ipadx = ipadx;
        gbc.ipady = ipady;

        return gbc;
    }
}
