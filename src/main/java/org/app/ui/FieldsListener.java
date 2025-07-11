package org.app.ui;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class FieldsListener implements DocumentListener {

    private final MainFrame mainFrame;

    public FieldsListener(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        mainFrame.checkFields();
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        mainFrame.checkFields();
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        mainFrame.checkFields();
    }
}
