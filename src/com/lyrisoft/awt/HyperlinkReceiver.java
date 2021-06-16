/*
 * Copyright (c) 2000 Lyrisoft Solutions, Inc.
 * Used by permission
 */
package com.lyrisoft.awt;

/**
 * Callback interface for objects interested in receiving hyperlink events.
 */
public interface HyperlinkReceiver {
    /**
     * Called when somebody wants us to handle a link
     * @param link a URL
     */
    public void handleHyperlink(String link);
}
