/**
 * Copyright (c) 2000 Lyrisoft Solutions, Inc.
 * Used by permission
 */
package com.lyrisoft.awt;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.util.Enumeration;
import java.util.Vector;

/**
 * A TextRun represents some text with a specified TextStyle.  TextRuns
 * know how to word-wrap and how to render themselves onto Graphics objects.
 */
public class TextRun {
    private String _text;
    private TextStyle _style;
    private char[] _chars;

    private FontMetrics _fm;
    private int _fmMaxDescent;
    private int _fmMaxAscent;
    private int _fmLeading;
    private int _fmHeight;
    private int[] _fmWidths;

    private Vector _substrings;
    private boolean _newLineFixed;

    private Rectangle _boundingRect = new Rectangle(0, 0, 0, 0);

    /**
     * Create a new text run with the given text and style.
     */
    public TextRun(String text, TextStyle style) {
        _text = text;
        _style = style;

        _fm = Toolkit.getDefaultToolkit().getFontMetrics(style.getFont());
        _fmMaxDescent = _fm.getMaxDescent();
        _fmMaxAscent = _fm.getMaxAscent();
        _fmLeading = _fm.getLeading();
        _fmHeight = _fm.getHeight();
        _fmWidths = _fm.getWidths();
        _chars = _text.toCharArray();

        _substrings = new Vector();
    }

    public TextStyle getStyle() {
        return _style;
    }

    public String getText() {
        return _text;
    }

    /**
     * Change our font.
     */
    public void setFont(Font f) {
        _style.setFont(f);
    }

    /**
     * Return the smallest Rectangle that contains this whole TextRun.
     * Prepare <b>must</b> be called before this will return anything
     * accurate, because the width is needed.
     */
    public Rectangle getBoundingRect() {
        return _boundingRect;
    }

    /**
     * Find the index of last last character of the substring that can be drawn before
     * having to wrap.
     *
     * @param begin the offset of the first character
     * @param xOff the X position that the substring starts at
     * @param xMax the wrapping point
     * @return the index of the last character that can be drawn before wrapping
     */
    int findEnd(int begin, int xOff, int xMax) {
        int end = begin;
        int width = xOff;
        while (end != _chars.length) {
            if (width + _fmWidths[_chars[end]] <= xMax &&
                _chars[end] != '\n')
            {
                width += _fmWidths[_chars[end]];
                end++;
            } else {
                break;
            }
        }

        if (end == _chars.length) {
            return end;
        }

        if (_chars[end] == '\n') {
            _newLineFixed = true;
            return end;
        }

        // back up to the last space
        int oldEnd = end;
        while (end > begin) {
            if (_chars[end-1] == ' ') {
                return end; // here it is
            } else {
                end--;
            }
        }
        return xOff == 0 ? oldEnd : end;
    }

    /**
     * Prepares this run of text for word-wrapped stylized display,
     * breaking it down into discrete Substrings that can be drawn
     * easily.
     *
     * @param xMax the wrapping point (width of the view)
     * @param preferredSize is filled in based xMax
     * @param the Point contains the x, y offset that this TextRun starts at.
     *                  The values in p get replaced with the Point that the
     *                  next TextRun should begin at.
     */
    void prepare(int xMax, Dimension preferredSize, Point p) {
        _substrings.removeAllElements();

        _boundingRect.x = 0;
        _boundingRect.y = p.y;

        int x = p.x;
        int y = p.y;
        if (xMax <= 0) {
            xMax = 600; 
        }

        int yOff = y + _fmMaxAscent;
        int xOff = x;

        int begin = 0;
        int len = _chars.length;
        int end = 0;

        int width = 0;
        do {
            if (_chars[begin] == '\n') {
                begin++;
                if (!_newLineFixed) {
                    yOff += _fmHeight;
                    xOff = 0;
                } else {
                    _newLineFixed = false;
                }
                if (begin == _chars.length-1) {
                    break;
                }
            }
            end = findEnd(begin, xOff, xMax);
            width = xOff + _fm.charsWidth(_chars, begin, end-begin);

            _substrings.addElement(new Substring(begin, end-begin, xOff, yOff));

            xOff = 0;
            if (end != len) {
                yOff += _fmHeight;
                begin = end;
            }
        } while (end != len);

        preferredSize.width = xMax;
        preferredSize.height = yOff - y + _fmMaxDescent;
        p.x = width;
        p.y = yOff - _fmMaxAscent;

        _boundingRect.width = preferredSize.width;
        _boundingRect.height = preferredSize.height;
    }

    /**
     * Check if this TextRun contains the specified point.  This class assumes
     * that the y coordinate has already been translated
     */
    public boolean contains(int x, int y, int yTranslation) {
        if (_boundingRect.inside(x, y+yTranslation)) {
            Substring target = null;
            for (Enumeration e = _substrings.elements(); e.hasMoreElements(); ) {
                Substring sub = (Substring)e.nextElement();
                if (x >= sub.xOff && y+yTranslation >= sub.yOff - _fmHeight) {
                    target = sub;
                }
            }
            
            if (target != null &&
                x < (target.xOff + _fm.stringWidth(new String(_chars, target.begin, target.length))))
            {
                return true;
            }

        }
        return false;
    }


    /**
     * Draw this run of text on the screen, starting at the y Offset specified.
     * Prepare must have been called first, or else nothing will draw
     *
     * @param g the graphics context to draw on
     */
    void draw(Graphics g, int yOff) {
        g.setColor(_style.getColor());
        g.setFont(_style.getFont());

        for (Enumeration e = _substrings.elements(); e.hasMoreElements(); ) {
            Substring substring = (Substring)e.nextElement();
            g.drawChars(_chars, substring.begin, substring.length, substring.xOff, substring.yOff + yOff);
        }
    }

    public String toString() {
        return "TextRun[\"" + _text + "\"]; Bounds = " + _boundingRect;
    }
}

/**
 * Represents a substring that is ready to be drawn.
 * Has an x and y screen offset (xOff and yOff), an offset
 * into the chars array (begin) and length.
 */
class Substring {
    public int begin;
    public int length;
    public int xOff;
    public int yOff;

    /**
     * @param begin starting index into the chars array
     * @param length length of the substring
     * @param xOff xOffset to start drawing
     * @param yOff yOffset to start drawing
     */
    public Substring(int begin, int length, int xOff, int yOff) {
        this.begin = begin;
        this.length = length;
        this.xOff = xOff;
        this.yOff = yOff;
    }
}
