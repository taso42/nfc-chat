/*
 * Copyright (c) 2000 Lyrisoft Solutions, Inc.
 * Used by permission
 */
package com.lyrisoft.awt;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.Enumeration;
import java.util.Vector;

/**
 * An alternative TextView (non-editable) that allows font colors, sytles,
 * and word-wrap.
 *
 * Works in JDK1.0.2
 */
public class TextView extends Canvas {
    public static Font DEFAULT_FONT = new Font("Dialog", Font.PLAIN, 10);
    protected Vector _runs;
    protected TextStyle _defaultStyle;

    private Image _image;
    private Graphics _graphics;
    private Dimension _bufferSize = new Dimension(0, 0);
    private int _fullHeight;
    private ScrollView _scrollView;
    protected int _yTranslation = 0;

    private Point _nextDrawPoint = new Point(0, 0);
    private Dimension _scratchDimension = new Dimension(0, 0);

    private boolean _autoScrolling;

    /* index of the first viewable TextRun */
    protected int _firstRun = 0;

    /**
     * Construct a new scrollview
     * @param autoScrolling indicates whether this TextView should automatically 
     * scroll to the bottom when new text is added
     */
    public TextView(boolean autoScrolling) {
        _runs = new Vector();
        setFont(DEFAULT_FONT);
        _autoScrolling = autoScrolling;
        reshape(0, 0, 1024, 768); // i'm pretty sure we need this to force a valid layout
        _defaultStyle = new TextStyle(getFont(), Color.black);
    }

    /**
     * Construct a new scrollview that does not auto-scroll
     */
    public TextView() {
        this(false);
    }

    /**
     * Get the default TextStyle
     */
    public TextStyle getDefaultStyle() {
        return _defaultStyle;
    }

    /**
     * Called by the ScrollView
     */
    void setScrollView(ScrollView sv) {
        _scrollView = sv;
    }

    /**
     * Start using a new font (by name)
     */
    public void setFont(String name) {
        Font oldFont = getFont();
        Font newFont = new Font(name, oldFont.getStyle(), oldFont.getSize());
        setFont(newFont);
    }

    /**
     * Start using a new font size
     */
    public void setFontSize(int size) {
        Font oldFont = getFont();
        Font newFont = new Font(oldFont.getName(), oldFont.getStyle(), size);
        setFont(newFont);
    }

    /**
     * Start using a new font (by font object)
     */
    public void setFont(Font f) {
        super.setFont(f);
        _defaultStyle = new TextStyle(f, getForeground());
    }

    /**
     * Append a string using the given TextStyle
     */
    public void append(String s, TextStyle style) {
        if (s.length() == 0) {
            return;
        }
        TextRun run = new TextRun(s, style);
        append(run);
    }

    /**
     * Append a TextRun object
     */
    public synchronized void append(TextRun run) {
        _runs.addElement(run);
        prepare(run);
        if (_scrollView != null) {
            _scrollView.boundsChanged();
            if (_autoScrolling) {
                _scrollView.scrollToBottom();
            }
        }
        repaint();
    }

    /**
     * Append a string in the default style
     */
    public void append(String s) {
        append(s, _defaultStyle);
    }

    /**
     * The the Y translation, or offset value
     */
    public void setYTranslation(int y) {
        _yTranslation = y;
    }

    /**
     * overridden to just call paint(g)
     */
    public void update(Graphics g) {
        paint(g);
    }

    /**
     * just called renderViewable(g)
     */
    public void paint(Graphics g) {
        renderViewable(g);
    }

    /**
     * Overridden so that we force a scroll to the very bottom, if auto-scrolling 
     * is enabled
     */
    public void show() {
        if (_autoScrolling) {
            _scrollView.scrollToBottom();
        }
    }

    /**
     * Overridden.  In addition to calling super.reshape(), we
     * re-render our contents if the width has changed.
     */
    public void reshape(int x, int y, int w, int h) {
        boolean reRender = size().width != w;
        super.reshape(x, y, w, h);
        if (!createBuffer()) {
            return;
        }
        if (reRender) {
            prepareAll();
        }
        if (_scrollView != null) {
            _scrollView.boundsChanged();
            if (_autoScrolling) {
                _scrollView.scrollToBottom();
            }
        }
        repaint();
    }

    /**
     * Create an off-screen drawing buffer
     * @return true on success, false on failure
     */
    boolean createBuffer() {
        Dimension size = size();
        if (size.width <= 0 || size.height <= 0) {
            return false;
        }
        _image = createImage(size.width, size.height);
        if (_image == null) {
            return false;
        }

        // dispose old graphics if we have one
        if (_graphics != null) {
            _graphics.dispose();
        }
        _graphics = _image.getGraphics();
        return true;
    }

    /**
     * The preferredSize we return is the size we would have to be to
     * fully render our contents given our current width.
     */
    public Dimension preferredSize() {
        return new Dimension(size().width, _fullHeight);
    }

    /**
     * Prepare a TextRun to be displayed.  This means, if
     * word-wrapping is necessary, the TextRun will break itself
     * down into individual lines.
     */
    void prepare(TextRun run) {
        int oldY = _nextDrawPoint.y;
        run.prepare(size().width, _scratchDimension, _nextDrawPoint);
        _fullHeight = oldY + _scratchDimension.height;
    }

    /**
     * Prepares all of the TextRuns for rendering.  
     */
    synchronized void prepareAll() {
        _nextDrawPoint.x = 0;
        _nextDrawPoint.y = 0;
        int width = size().width;
        for (Enumeration e = _runs.elements(); e.hasMoreElements(); ) {
            TextRun run = (TextRun)e.nextElement();
            prepare(run);
        }
    }

    /**
     * Render ourselves onto the graphics object.  Only the visible
     * portion is rendered.
     */
    synchronized void renderViewable(Graphics g) {
        Dimension size = size();

        if (_graphics == null) {
            if (!createBuffer()) {
                return;
            }
        }
        _graphics.setColor(getBackground());
        _graphics.fillRect(0, 0, size.width, size.height);


        // Iterate through all the runs of text.
        TextRun run = null;
        _firstRun = -1;
        int i=0;
        for (Enumeration e = _runs.elements(); e.hasMoreElements(); ) {
            run = (TextRun)e.nextElement();

            // get the bounding rectangle of the next run
            Rectangle r = run.getBoundingRect();

            // Is the TextRun inside the viewable range?
            if (r.y -_yTranslation + r.height >= 0) {
                if (_firstRun == -1) {
                    _firstRun = i;
                }
                // Is the TextRun below the screen?
                if (r.y - _yTranslation > size.height) {
                    // Yes, we're done
                    break;
                }

                // Yes, draw it.
                run.draw(_graphics, -_yTranslation);
            }
            i++;
        }
        g.drawImage(_image, 0, 0, this);
    }
}
