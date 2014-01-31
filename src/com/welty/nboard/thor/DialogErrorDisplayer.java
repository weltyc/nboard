package com.welty.nboard.thor;

import javax.swing.*;

public class DialogErrorDisplayer implements ErrorDisplayer {
    @Override public void notify(String operation, String error) {
        JOptionPane.showMessageDialog(null, error, "Error while " + operation, JOptionPane.ERROR_MESSAGE);
    }
}
