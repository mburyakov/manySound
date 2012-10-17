package manySound;

import manySound.exceptions.CannotLoginException;
import manySound.exceptions.NotifyException;
import manySound.exceptions.UnknownSQLException;
import manySound.exceptions.UserShownException;

import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Vector;

public class Main {

    static JFrame createMainFrame() {
        DatabaseChanger.getInstance().debug = true;
        JFrame mainFrame = new JFrame("Database");
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainFrame.getContentPane().add(mainPanel);
        JTabbedPane tabs = new JTabbedPane();
        mainPanel.add(tabs);

        //usersPanel
        JPanel usersPanel = new JPanel(new BorderLayout());
        final UserListModel userListModel = new UserListModel();
        final JList<String> userList = new JList<>(userListModel);
        final Runnable userListUpdateRunnable = new Runnable() {
            @Override
            public void run() {
                try {
                    final ArrayList<String> al = DatabaseChanger.getInstance().fetchAllUsers();
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            userListModel.update(al);
                        }
                    });
                } catch (IOException | InstantiationException | IllegalAccessException | ClassNotFoundException e) {
                    DatabaseChanger.die(e);
                } catch (UnknownSQLException e) {
                    e.showMessage();
                }
            }
        };

        new Thread(userListUpdateRunnable).start();
        JScrollPane userListScrollPane = new JScrollPane(userList);
        final JTextField loginTextField = new JTextField();
        JButton addUserButton = new JButton("Add");
        final Runnable addUserWorker = new Runnable() {
            @Override
            public void run() {
                final String userName = loginTextField.getText();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            DatabaseChanger.getInstance().addUser(userName);
                            userListUpdateRunnable.run();
                        } catch (UserShownException e) {
                            e.showMessage();
                        }
                    }
                }).start();
            }
        };
        addUserButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addUserWorker.run();
            }
        });
        loginTextField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addUserWorker.run();
                loginTextField.setText("");
            }
        });
        usersPanel.add(userListScrollPane, BorderLayout.CENTER);
        JPanel addUserPanel = new JPanel(new GridLayout(2, 1, 10, 10));
        usersPanel.add(addUserPanel, BorderLayout.SOUTH);
        addUserPanel.add(loginTextField);
        addUserPanel.add(addUserButton);
        tabs.addTab("Users", usersPanel);

        //resetPanel
        JPanel resetPanel = new JPanel();
        final Button resetButton = new Button("Reset");
        resetPanel.add(resetButton);
        final Runnable resetWorker = new Runnable() {
            @Override
            public void run() {
                resetButton.setEnabled(false);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            DatabaseChanger.getInstance().resetDatabase();
                        } catch (InstantiationException | ClassNotFoundException | IllegalAccessException | IOException e1) {
                            DatabaseChanger.die(e1);
                        } catch (UnknownSQLException e) {
                            e.showMessage();
                        }
                        userListUpdateRunnable.run();
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                resetButton.setEnabled(true);
                            }
                        });
                    }
                }).start();
            }
        };
        resetButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                resetWorker.run();
            }
        });
        resetButton.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    resetWorker.run();
                }
            }
        });
        tabs.addTab("Reset", resetPanel);

        //loginPanel

        final UserSession[] currentSession = {null};

        JPanel loginPanel = new JPanel(new GridLayout(2, 1));
        final JLabel[] currentLoggedLabel = {new JLabel(MessageGenerator.loggedOn(null))};
        final JButton logoutButton = new JButton("Logout");
        logoutButton.setEnabled(false);
        JPanel logoutPanel = new JPanel(new FlowLayout());
        logoutPanel.add(currentLoggedLabel[0]);
        logoutPanel.add(logoutButton);
        loginPanel.add(logoutPanel);
        JPanel performLoginPanel = new JPanel(new FlowLayout());
        loginPanel.add(performLoginPanel);
        final ComboBoxModel<String> selectLoginComboBoxModel = new SelectLoginComboBoxModel(userListModel);
        final JComboBox<String> selectLoginComboBox = new JComboBox<>(selectLoginComboBoxModel);
        performLoginPanel.add(selectLoginComboBox);
        final JButton performLoginButton = new JButton("Login");
        performLoginPanel.add(performLoginButton);
        final Runnable performLoginWorker = new Runnable() {
            @Override
            public void run() {
                final String loginUserName = selectLoginComboBox.getSelectedItem().toString();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        final UserSession newSession = UserSession.tryLogin(loginUserName);
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                if (newSession == null) {
                                    new CannotLoginException(loginUserName).showMessage();
                                } else {
                                    currentSession[0] = newSession;
                                    performLoginButton.setEnabled(false);
                                    logoutButton.setEnabled(true);
                                    currentLoggedLabel[0].setText(MessageGenerator.loggedOn(currentSession[0]));
                                    try {
                                        createSelectMeetingFrame(currentSession[0].getMeetingsList(), currentSession[0]);
                                    } catch (UnknownSQLException e) {
                                        e.showMessage();
                                    }
                                }
                            }

                        });
                    }
                }).start();
            }
        };
        final Runnable logoutWorker = new Runnable() {
            @Override
            public void run() {
                currentSession[0] = null;
                performLoginButton.setEnabled(true);
                logoutButton.setEnabled(false);
                currentLoggedLabel[0].setText(MessageGenerator.loggedOn(currentSession[0]));
            }
        };
        performLoginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performLoginWorker.run();
            }
        });
        logoutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                logoutWorker.run();
            }
        });

        tabs.addTab("Login", loginPanel);


        mainFrame.setSize(new Dimension(500, 300));
        mainFrame.setLocation(0, 0);
        mainFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        mainFrame.setVisible(true);
        loginTextField.requestFocusInWindow();
        return mainFrame;
    }

    public static JFrame createSelectMeetingFrame(Vector<Vector<String>> meetingCollection, final UserSession userSession) {
        final JFrame selectMeetingFrame = new JFrame("Select meeting");
        JPanel selectMeetingPanel = new JPanel(new BorderLayout());
        selectMeetingFrame.getContentPane().add(selectMeetingPanel);
        final JTable meetingTable = new JTable(meetingCollection, UserSession.meetingRowNames);
        meetingTable.addMouseListener(new MouseInputAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Point p = e.getPoint();
                int row = meetingTable.rowAtPoint(p);
                int col = meetingTable.columnAtPoint(p);
                new NotifyException("Selected " + meetingTable.getValueAt(row, 0)).showMessage();
            }
        });
        selectMeetingPanel.add(meetingTable, BorderLayout.CENTER);

        JPanel addMeetingPanel = new JPanel(new BorderLayout());
        final JTextField meetingNameField = new JTextField();
        addMeetingPanel.add(meetingNameField, BorderLayout.CENTER);
        final JButton addMeetingButton = new JButton("Add");
        final Runnable addMetingWorker = new Runnable() {
            @Override
            public void run() {
                final String userName = userSession.getUserName();
                final String meetingName = meetingNameField.getText();
                selectMeetingFrame.setVisible(false);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            DatabaseChanger.getInstance().addMeeting(userName, meetingName);
                        } catch (UserShownException e) {
                            e.showMessage();
                        } finally {
                            SwingUtilities.invokeLater(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        createSelectMeetingFrame(userSession.getMeetingsList(), userSession);
                                    } catch (UnknownSQLException e) {
                                        e.showMessage();
                                    }
                                }
                            });
                        }
                    }
                }).start();
            }
        };
        addMeetingButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addMetingWorker.run();
            }
        });
        addMeetingPanel.add(addMeetingButton, BorderLayout.EAST);
        selectMeetingPanel.add(addMeetingPanel, BorderLayout.SOUTH);

        selectMeetingFrame.setSize(500, 300);
        selectMeetingFrame.setLocation(10, 10);
        selectMeetingFrame.setVisible(true);
        return selectMeetingFrame;
    }

    public static void main(String[] args) {
        try {
            DatabaseChanger.getInstance().resetDatabase();
        } catch (IOException | ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            DatabaseChanger.die(e);
        } catch (UnknownSQLException e) {
            e.showMessage();
        }
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createMainFrame();
            }
        });
    }
}