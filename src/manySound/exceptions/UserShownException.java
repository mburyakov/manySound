package manySound.exceptions;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public abstract class UserShownException extends Exception {

    public abstract String getMessage();

    public abstract String getHeader();

    public void showMessage() {
        final JFrame messageFrame = new JFrame(getHeader());
        JLabel messageLabel = new JLabel(getMessage());
        messageFrame.getContentPane().setLayout(new GridLayout(2, 1));
        JPanel messageLabelPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        messageLabelPanel.add(messageLabel);
        messageFrame.getContentPane().add(new JScrollPane(messageLabelPanel));
        JPanel okButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton okButton = new JButton("OK");
        okButtonPanel.add(okButton);
        messageFrame.getContentPane().add(okButtonPanel);
        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                messageFrame.setVisible(false);
            }
        });
        okButton.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    messageFrame.setVisible(false);
                }
            }
        });
        messageFrame.setSize(400, 200);
        messageFrame.setVisible(true);
        okButton.requestFocusInWindow();
    }
}
