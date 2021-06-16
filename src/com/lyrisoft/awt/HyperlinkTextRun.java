/**
 * Copyright (c) 2000 Lyrisoft Solutions, Inc.
 * Used by permission
 */
package com.lyrisoft.awt;

/**
 * A HyperlinkTextRun is just a TextRun that has a URL associated with it.
 */
public class HyperlinkTextRun extends TextRun {
    protected String _href;

    /**
     * Construct a HyperlinkTextRun with the given text, url, and style
     */
    public HyperlinkTextRun(String text, String href, TextStyle style) {
        super(text, style);
        _href = href;
    }

    /**
     * Get the href, or URL, associated with this textrun
     */
    public String getHref() {
        return _href;
    }

}
