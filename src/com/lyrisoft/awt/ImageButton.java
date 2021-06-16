package com.lyrisoft.awt;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.Graphics;
import java.awt.Image;

/**
 * A button that displays an image.  When being moused-over, a yellow
 * border appears around the button.
 */
public class ImageButton extends Canvas {
    private Image image;
    private String command;
    private boolean selected = false;
    private Dimension size;

    /**
     * Create a new ImageButton with the specified image and command.
     */
    public ImageButton(Image img, String command) {
        super();
        this.image = img;
        this.command = command;
        if (image == null) {
            size = new Dimension(0, 0);
        } else {
            size = new Dimension(image.getWidth(this), image.getHeight(this));
        }
    }

    /**
     * returns the minimum size
     */
    public Dimension preferredSize() {
        return minimumSize();
    }

    /**
     * returns our predetermined size - the size of the image we're
     * displaying.  if the image was null, then our minimum size is 0
     * by 0.
     */
    public Dimension minimumSize() {
        return size;
    }

    /**
     * calls paint(g)
     */
    public void update(Graphics g) {
        paint(g);
    }

    /**
     * paint the image, and if the button is "selected" (being moused
     * over), a border is drawn.
     */
    public void paint(Graphics g) {
        g.drawImage(image, 0, 0, this);
        if (selected) {
            g.setColor(Color.yellow);
            g.drawRect(0, 0, image.getWidth(this)-1, image.getHeight(this)-1);
        }
    }

    /**
     * Catching MOUSE_ENTER and MOUSE_EXIT events to set or unset the
     * selected state.  Catching MOUSE_UP events to trigger the
     * command.  A command is triggered by setting e.arg to our
     * command, and then calling super.handleEvent(e)
     */
    public boolean handleEvent(Event e) {
        if (e.id == Event.MOUSE_UP) {
            if (selected) {
                e.id = Event.ACTION_EVENT;
                e.arg = command;
            }
        }
        if (e.id == Event.MOUSE_ENTER) {
            selected = true;
            repaint();
            return true;
        }
        if (e.id == Event.MOUSE_EXIT) {
            selected = false;
            repaint();
            return true;
        }
        return super.handleEvent(e);
    }
}
