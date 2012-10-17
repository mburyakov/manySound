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
        JPanel messagePanel = new JPanel(new BorderLayout());
        messageFrame.getContentPane().add(messagePanel);
        JTextArea messageTextArea = new JTextArea(getMessage());
        messageTextArea.setLineWrap(true);
        messageTextArea.setWrapStyleWord(true);
        messageTextArea.setEditable(false);
        messageTextArea.setBackground(messageFrame.getBackground());
        messagePanel.add(new JScrollPane(messageTextArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.CENTER);
        JPanel okButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton okButton = new JButton("OK");
        okButtonPanel.add(okButton);
        messagePanel.add(okButtonPanel, BorderLayout.SOUTH);
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
