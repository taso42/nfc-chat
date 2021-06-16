/*
 * Copyright (c) 2000 Lyrisoft Solutions, Inc.
 * Used by permission
 */
package com.lyrisoft.chat.client.gui.awt102;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Panel;

import com.lyrisoft.awt.HyperlinkReceiver;
import com.lyrisoft.awt.HyperlinkTextView;
import com.lyrisoft.awt.ScrollView;
import com.lyrisoft.awt.TextStyle;
import com.lyrisoft.awt.TextView;
import com.lyrisoft.chat.Translator;

/**
 * HyperlinkTextView subclass, specialized for NFC.
 */
public class MessageView extends Panel {
    protected TextView textView;
    protected ScrollView scroller;
    
    protected TextStyle normalStyle;
    protected TextStyle errorStyle;
    protected TextStyle userStyle;
    protected TextStyle privateStyle;

    public MessageView(HyperlinkReceiver receiver) {
        textView = new HyperlinkTextView(true, receiver);
        setBackground(Color.white);
        setLayout(new GridLayout(1, 1));
        scroller = new ScrollView(textView);
        scroller.resize(100, 300);
        setupTextStyles();
        add(scroller);
    }

    public TextStyle getNormalStyle() {
        return normalStyle;
    }

    public void setNormalStyle(TextStyle style) {
        normalStyle = style;
    }

    public TextStyle getErrorStyle() {
        return errorStyle;
    }

    public void setErrorStyle(TextStyle style) {
        errorStyle = style;
    }

    public TextStyle getUserStyle() {
        return userStyle;
    }

    public void setUserStyle(TextStyle style) {
        userStyle = style;
    }

    public TextStyle getPrivateStyle() {
        return privateStyle;
    }

    public void setPrivateStyle(TextStyle style) {
        privateStyle = style;
    }

    protected void setupTextStyles() {
        normalStyle = textView.getDefaultStyle();

        errorStyle = new TextStyle(normalStyle);
        errorStyle.setColor(Color.red);
        
        userStyle = new TextStyle(normalStyle);
        userStyle.setColor(Color.blue);

        privateStyle = new TextStyle(normalStyle);
        Font f = privateStyle.getFont();
        privateStyle.setFont(new Font(f.getName(), Font.ITALIC, f.getSize()));
        privateStyle.setColor(Color.red);
    }

    public void setFont(String name) {
        textView.setFont(name);
        setupTextStyles();
    }

    public void setFontSize(int size) {
        textView.setFontSize(size);
        setupTextStyles();
    }

    public void displayPrivateMessage(String user, String message) {
        textView.append(user, userStyle);
        textView.append(" " + Translator.getMessage("privately"), privateStyle);
        textView.append(" " + message, normalStyle);
        textView.append("\n");
    }

    public void displayMessage(String user, String message) {
        textView.append(user, userStyle);
        textView.append(": " + message, normalStyle);
        textView.append("\n");
    }

    public void displayMessage(String message) {
        textView.append(message, normalStyle);
        textView.append("\n");
    }

    public void displayError(String error) {
        textView.append(Translator.getMessage("error2") + ":", errorStyle);
        textView.append(" " + error, normalStyle);
        textView.append("\n");
    }

    public void displayPrivateEmote(String user, String message) {
        textView.append(Translator.getMessage("privately"), privateStyle);
        textView.append(" " + user + " " + message, normalStyle);
        textView.append("\n");
    }
}
