package com.lyrisoft.chat.client.gui.jfc;

import java.awt.Color;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Position;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import com.lyrisoft.chat.client.gui.IMessageWindow;

public class SimpleTextPanel extends JPanel implements IMessageWindow {
    private JTextPane _textArea;
    private Position _endPosition;

    private SimpleAttributeSet _userAttributes;
    private SimpleAttributeSet _normalAttributes;
    private SimpleAttributeSet _privateAttributes;
    private SimpleAttributeSet _privateTextAttributes;
    private SimpleAttributeSet _errorAttributes;

    public SimpleTextPanel() {
        _textArea = new JTextPane();
        _textArea.setEditable(false);
        setLayout(new java.awt.GridLayout());
        
        JScrollPane scroller = new JScrollPane(_textArea, 
                                               ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                                               ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        add(scroller);
        Document doc = _textArea.getStyledDocument();
        _endPosition = doc.getEndPosition();
        AttributeSet baseAttributes = doc.getDefaultRootElement().getAttributes();

        _normalAttributes = new SimpleAttributeSet(baseAttributes);
        _normalAttributes.addAttribute(StyleConstants.Foreground, Color.black);
        _normalAttributes.addAttribute(StyleConstants.FontFamily, "SansSerif");

        _userAttributes = new SimpleAttributeSet(_normalAttributes);
        _userAttributes.addAttribute(StyleConstants.Foreground, Color.blue);

        _errorAttributes = new SimpleAttributeSet(_normalAttributes);
        _errorAttributes.addAttribute(StyleConstants.Foreground, Color.red);
        _errorAttributes.addAttribute(StyleConstants.Bold, new Boolean(true));

        _privateAttributes = new SimpleAttributeSet(_normalAttributes);
        _privateAttributes.addAttribute(StyleConstants.Foreground, Color.red);

        _privateTextAttributes = new SimpleAttributeSet(_normalAttributes);
        _privateTextAttributes.addAttribute(StyleConstants.Foreground, Color.black);
        _privateTextAttributes.addAttribute(StyleConstants.Italic, new Boolean(true));
    }

    public void displayPrivateMessage(String user, String message) {
        append(user, _userAttributes);
        append(" (privately)", _privateAttributes);
        append(": " + message + "\n", _privateTextAttributes);
    }

    public void displayPrivateEmote(String user, String message) {
        append("(privately) ", _privateAttributes);
        append(user, _userAttributes);
        append(" " + message + "\n", _privateTextAttributes);
    }

    public void displayMessage(String user, String message) {
        append(user, _userAttributes);
        append(": " + message + "\n", _normalAttributes);
    }
    
    public void displayMessage(String message) {
        append(message + "\n", _normalAttributes);
    }

    public void displayError(String error) {
        append(error + "\n", _errorAttributes);
    }

    void append(String text, AttributeSet attributes) { 
        Document doc = _textArea.getDocument();
        try {
            doc.insertString(_endPosition.getOffset()-1, text, attributes);
            _textArea.setCaretPosition(_endPosition.getOffset()-1);
        }
        catch (BadLocationException e) {
            throw new RuntimeException(e.toString());
        }
    }
}
