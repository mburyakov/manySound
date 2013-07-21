package manySound;

import manySound.exceptions.CannotLoginException;
import manySound.exceptions.UnknownSQLException;
import manySound.exceptions.UserShownException;

import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Vector;

public class Main {

    static JFrame mainFrame;

    static JPanel containerPanel;

    static JPanel getContainerPanel() {
        if (mainFrame==null) {
            getMainFrame();
        }
        return containerPanel;
    }

    static ArrayList<JPanel> panelList = new ArrayList<>();

    static JFrame getMainFrame() {
        if (mainFrame == null) {
            mainFrame = createMainFrame();
            return mainFrame;
        } else {
            return mainFrame;
        }
    }

    static JFrame createMainFrame() {
        JFrame mainFrame = new JFrame("Database");
        mainFrame.setSize(new Dimension(500, 500));
        mainFrame.setLocation(0, 0);
        mainFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        JPanel rootPanel = new JPanel(new BorderLayout());
        JPanel northPanel = new JPanel();
        mainFrame.getContentPane().add(rootPanel);
        rootPanel.add(northPanel, BorderLayout.NORTH);
        containerPanel = new JPanel();
        rootPanel.add(containerPanel, BorderLayout.CENTER);
        JButton backButton = new JButton("<= Back");
        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                popFromFrame();
            }
        });
        northPanel.add(backButton);
        return mainFrame;
    }

    static JPanel createMainPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
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

        final JPanel logPanel = new JPanel(new CardLayout());
        JPanel loginPanel = new JPanel(new GridLayout(3, 1));
        final JTextField loginField = new JTextField();
        final JTextField passwordField = new JPasswordField();
        final JButton performLoginButton = new JButton("Login");
        loginPanel.add(loginField);
        loginPanel.add(passwordField);
        loginPanel.add(performLoginButton);
        logPanel.add(loginPanel, "1");

        JPanel logoutPanel = new JPanel(new FlowLayout());
        final JLabel[] currentLoggedLabel = {new JLabel(MessageGenerator.loggedOn(null))};
        final JButton logoutButton = new JButton("Logout");
        logoutPanel.add(currentLoggedLabel[0]);
        logoutPanel.add(logoutButton);
        logPanel.add(logoutPanel, "2");


        //loginPanel.add(logoutPanel);
        //JPanel performLoginPanel = new JPanel(new FlowLayout());
        //loginPanel.add(performLoginPanel);
        //final ComboBoxModel<String> selectLoginComboBoxModel = new SelectLoginComboBoxModel(userListModel);
        //final JComboBox<String> selectLoginComboBox = new JComboBox<>(selectLoginComboBoxModel);
        //performLoginPanel.add(selectLoginComboBox);

        //performLoginPanel.add(performLoginButton);
        final Runnable performLoginWorker = new Runnable() {
            @Override
            public void run() {
                final String loginUserName = loginField.getText();
                final String loginPassword = passwordField.getText();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        final UserSession newSession = UserSession.tryLogin(loginUserName, loginPassword);
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                if (newSession == null) {
                                    new CannotLoginException(loginUserName).showMessage();
                                } else {
                                    currentSession[0] = newSession;
                                    currentLoggedLabel[0].setText(MessageGenerator.loggedOn(currentSession[0]));
                                    ((CardLayout) logPanel.getLayout()).show(logPanel, "2");
                                    try {
                                        addToFrame(createSelectMeetingPanel(currentSession[0].getMeetingsList(), currentSession[0]));
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
                currentLoggedLabel[0].setText(MessageGenerator.loggedOn(currentSession[0]));
                ((CardLayout) logPanel.getLayout()).show(logPanel, "1");
            }
        };
        performLoginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performLoginWorker.run();
            }
        });
        loginField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performLoginWorker.run();
                loginField.setText("");
                passwordField.setText("");
            }
        });
        passwordField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performLoginWorker.run();
                loginField.setText("");
                passwordField.setText("");
            }
        });
        logoutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                logoutWorker.run();
            }
        });

        tabs.addTab("Login", logPanel);

        loginTextField.requestFocusInWindow();
        return mainPanel;
    }

    public static JPanel createSelectMeetingPanel(Vector<Vector<String>> meetingCollection, final UserSession userSession) {
        JPanel selectMeetingPanel = new JPanel(new BorderLayout());
        final JTable meetingTable = new JTable(meetingCollection, UserSession.meetingRowNames);
        //meetingTable.getV
        meetingTable.setCellEditor(null);
        meetingTable.addMouseListener(new MouseInputAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Point p = e.getPoint();
                int row = meetingTable.rowAtPoint(p);
                int col = meetingTable.columnAtPoint(p);
                int id = new Integer(meetingTable.getValueAt(row, 0).toString());
                try {
                    Meeting selected = new Meeting(id, userSession);
                    addToFrame(createMeetingPanel(selected));
                } catch (UserShownException e1) {
                    e1.showMessage();
                }
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
                                        popFromFrame();
                                        addToFrame(createSelectMeetingPanel(userSession.getMeetingsList(), userSession));
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
        meetingNameField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addMetingWorker.run();
                meetingNameField.setText("");
            }
        });
        addMeetingPanel.add(addMeetingButton, BorderLayout.EAST);
        selectMeetingPanel.add(addMeetingPanel, BorderLayout.SOUTH);

        return selectMeetingPanel;
    }

    public static JPanel createMeetingPanel(final Meeting meeting) {
        JPanel meetingPanel = new JPanel(new BorderLayout());

        //JTextArea description = new JTextArea(meeting.getDescription());
        //meetingPanel.add(description, BorderLayout.NORTH);


        try {
            final DatabaseChanger.InstrumentList instrumentList = meeting.getInstrumentList();
            JPanel instrumentsPanel = new JPanel(new GridLayout(3,1));
            final JComboBox<String> instruments = new JComboBox<>(instrumentList.descriptions);
            instrumentsPanel.add(instruments);
            meetingPanel.add(instrumentsPanel, BorderLayout.CENTER);
            final JComboBox<String> recipients = new JComboBox<>();
            JButton applyButton = new JButton("Apply");

            applyButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    final String recipient = recipients.getItemAt(recipients.getSelectedIndex());
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                DatabaseChanger.getInstance().applyInstrument(meeting.getUserSession().getUserName(), meeting.getMeetingId(), instrumentList.ids.get(instruments.getSelectedIndex()), new String[]{recipient});
                            } catch (UnknownSQLException e1) {
                                e1.showMessage();
                            }
                        }
                    }).start();

                }
            });


            instrumentsPanel.add(recipients);
            instrumentsPanel.add(applyButton);
            instruments.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    int index = instruments.getSelectedIndex();
                    final int id = instrumentList.ids.get(index);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            final Vector<String> recipientList;
                            try {
                                recipientList = meeting.getRecipientList(id);
                                SwingUtilities.invokeLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        recipients.removeAllItems();
                                        for (String s : recipientList) {
                                            recipients.addItem(s);
                                        }
                                    }
                                });
                            } catch (UnknownSQLException e1) {
                                e1.showMessage();
                            }
                        }
                    }).start();

                }
            });
        } catch (UnknownSQLException e) {
            e.showMessage();
        }



        /*if (meeting.isOwner()) {
            JPanel ownerPanel = new JPanel();
            JButton groupsButton = new JButton("groups");
            ownerPanel.add(groupsButton);
            meetingPanel.add(ownerPanel, BorderLayout.SOUTH);
        }*/

        return meetingPanel;
    }

    public static void addToFrame(JPanel panel) {
        if (panelList.isEmpty()) {
            getMainFrame().setVisible(true);
        }
        panelList.add(panel);
        getContainerPanel().removeAll();
        getContainerPanel().add(panel);
        getMainFrame().revalidate();
        getMainFrame().repaint();
    }

    public static void popFromFrame() {
        if (panelList.isEmpty()) {
            return;
        }
        panelList.remove(panelList.size()-1);
        getContainerPanel().removeAll();
        if (panelList.isEmpty()) {
            getMainFrame().setVisible(false);
            getMainFrame().dispose();
        } else {
            getContainerPanel().add(panelList.get(panelList.size() - 1));
            mainFrame.revalidate();
            mainFrame.repaint();
        }
    }

    public static void main(String[] args) {
        DatabaseChanger.getInstance().debug = false;
        if (DatabaseChanger.getInstance().debug) {
            try {
                DatabaseChanger.getInstance().resetDatabase();
            } catch (IOException | ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                DatabaseChanger.die(e);
            } catch (UnknownSQLException e) {
                e.showMessage();
            }
        }
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                getMainFrame();
                addToFrame(createMainPanel());
            }
        });
    }
}