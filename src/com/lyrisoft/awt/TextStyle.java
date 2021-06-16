/*
 * Copyright (c) 2000 Lyrisoft Solutions, Inc.
 * Used by permission
 */
package com.lyrisoft.awt;

import java.awt.Color;
import java.awt.Font;

/**
 * Encapulates a Font and a Color
 */
public class TextStyle {
    private Font _font;
    private Color _color;

    /**
     * Construct a TextStyle that is a copy of the specified TextStyle
     */
    public TextStyle(TextStyle t) {
        _font = t._font;
        _color = t._color;
    }

    /**
     * Construct a TextStyle with the given font and color
     */ 
    public TextStyle(Font f, Color c) {
        _font = f;
        _color = c;
    }

    public Font getFont() {
        return _font;
    }

    public void setFont(Font f) {
        _font = f;
    }

    public Color getColor() {
        return _color;
    }

    public void setColor(Color c) {
        _color = c;
    }
}
