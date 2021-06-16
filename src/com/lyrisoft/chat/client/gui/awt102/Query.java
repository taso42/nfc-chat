/*
 * Copyright (c) 2000 Lyrisoft Solutions, Inc.
 * Used by permission
 */
package com.lyrisoft.chat.client.gui.awt102;

import java.awt.Button;
import java.awt.Choice;
import java.awt.Event;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Label;
import java.awt.Panel;
import java.awt.Rectangle;
import java.awt.TextField;

import com.lyrisoft.chat.Translator;
import com.lyrisoft.chat.client.gui.IQuery;
import com.lyrisoft.chat.client.gui.IQueryCallback;

public class Query extends Frame implements IQuery {
    private Choice choice;
    private TextField textField;
    private Button btnOk;
    private Button btnCancel;
    private boolean canceled = false;

    private IQueryCallback _callback;
    private int _callbackId;
    private Frame _parent;
    

    public Query(Frame owner, 
                 String title, 
                 String choiceLabel, String[] choices, 
                 boolean showTextField, String textFieldLabel) 
    {
//        super(owner, title, false);
        super(title);
        _parent = owner;
        int y = 0;
        GridBagLayout gridBag = new GridBagLayout();
        setLayout(gridBag);

        Label label = new Label(title);
        gridBag.setConstraints(label, 
                               Constraints.create(0, y++, 2, 1, 0.0, 0.0,
                                                  GridBagConstraints.WEST, GridBagConstraints.NONE,
                                                  new Insets(0, 0, 0, 0), 0, 0));
        add(label);

        if (choiceLabel != null && choices != null) {
            choice = new Choice();
            for (int i=0; i < choices.length; i++) {
                choice.addItem(choices[i]);
            }
            
            label = new Label(choiceLabel);
            gridBag.setConstraints(label, 
                                   Constraints.create(0, y, 1, 1, 0.0, 0.0,
                                                      GridBagConstraints.WEST, GridBagConstraints.NONE,
                                                      new Insets(0, 0, 0, 0), 0, 0));
            add(label);
            
            
            gridBag.setConstraints(choice, 
                                   Constraints.create(1, y++, 1, 1, 0.0, 0.0,
                                                  GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                                                      new Insets(0, 0, 0, 0), 0, 0));
            add(choice);
        }

        if (showTextField) {
            textField = new TextField(40);

            if (textFieldLabel != null) {
                label = new Label(textFieldLabel);
                gridBag.setConstraints(label, 
                                       Constraints.create(0, y, 1, 1, 0.0, 0.0,
                                                          GridBagConstraints.WEST, GridBagConstraints.NONE,
                                                          new Insets(0, 0, 0, 0), 0, 0));
                gridBag.setConstraints(textField, 
                                       Constraints.create(1, y++, 1, 1, 1.0, 0,
                                                          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                                                          new Insets(2, 2, 2, 2), 0, 4));
                add(label);
            } else {
                gridBag.setConstraints(textField, 
                                       Constraints.create(0, y++, 2, 1, 1.0, 0,
                                                          GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                                                          new Insets(2, 2, 2, 2), 0, 4));
            }
            add(textField);
        }
        
        Panel p = new Panel();
        p.setLayout(new FlowLayout());
        btnOk = new Button(Translator.getMessage("label.ok"));
        btnCancel = new Button(Translator.getMessage("label.cancel"));
        p.add(btnOk);
        p.add(btnCancel);
        gridBag.setConstraints(p, 
                               Constraints.create(0, y++, 2, 1, 1.0, 0,
                                                 GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                                                 new Insets(0, 0, 0, 0), 0, 0));
        add(p);

        pack();
//        center();
//        show();
    }

    public void setCallbackParams(int id, IQueryCallback cb) {
        _callback = cb;
        _callbackId = id;
    }

    public void setDefaultSelection(String s) {
        choice.select(s);
    }

    public String getSelection() {
        if (choice != null) {
            return choice.getSelectedItem();
        } else {
            return null;
        }
    }

    public String getText() {
        if (textField != null) {
            return textField.getText();
        } else {
            return null;
        }
    }

    public boolean getCanceled() {
        return canceled;
    }

    public boolean action(Event e, Object arg) {
        if (e.target == btnCancel) {
            canceled = true;
            hide();
            return true;
        } else if (e.id == Event.ACTION_EVENT) {
            if (e.target == textField || e.target == btnOk) {
                hide();
                if (_callback != null ) {
                    _callback.handleQuery(_callbackId, this);
                } 
                return true;
            }
            (new Throwable()).printStackTrace();
        }
        return super.action(e, arg);
    }

    public boolean handleEvent(Event e) {
        if (e.target == this && e.id == Event.WINDOW_DESTROY) {
            canceled = true;
            hide();
            return true;
        }
        return super.handleEvent(e);
    }

    public void center() {
        Rectangle pr = _parent.bounds();
        Rectangle r = bounds();
        
        move(Math.max(0, (pr.width - r.width) / 2 + pr.x), Math.max(0, (pr.height - r.height) / 2 + pr.y));
    }
}



