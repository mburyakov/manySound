package manySound;

import manySound.exceptions.UnknownSQLException;
import manySound.exceptions.UserAlreadyExistsException;
import manySound.exceptions.UserShownException;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Vector;
import java.util.regex.Pattern;

public class DatabaseChanger {
    private static DatabaseChanger ourInstance = new DatabaseChanger();

    public static DatabaseChanger getInstance() {
        return ourInstance;
    }

    private Connector connector;
    public boolean debug;

    private DatabaseChanger() {
        try {
            connector = new Connector("jdbc:mysql://localhost/manysound?user=manysound");
        } catch (ClassNotFoundException | AbstractMethodError | InstantiationException | IllegalAccessException e) {
            die(e);
        }
    }

    public Connector getConnector() {
        return connector;
    }

    public void sleep() {
        if (debug) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                die(e);
            }
        }
    }

    public Vector<Vector<String>> fetchMeetings(String login) throws UnknownSQLException {
        Connection connection = null;
        try {
            connection = connector.getConnection(true);
            PreparedStatement statement = connection.prepareStatement("CALL `view_meetings(?)`");
            statement.setString(1, login);
            ResultSet resultSet = statement.executeQuery();
            Vector<Vector<String>> meetingVector = new Vector<>();
            while (resultSet.next()) {
                Vector<String> meeting = new Vector<>();
                meeting.set(0,resultSet.getString("name"));
                meeting.set(0,resultSet.getString("description"));
                meetingVector.add(meeting);
            }
            connection.close();
            sleep();
            return meetingVector;
        } catch (SQLException e) {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException ignored) {
            }
            throw new UnknownSQLException(e);
        }
    }

    public void addUser(String login) throws UserShownException {
        Connection connection = null;
        try {
            connection = connector.getConnection(true);
            PreparedStatement addStatement = connection.prepareStatement("CALL `insert_new_user`(?)");
            addStatement.setString(1, login);
            addStatement.execute();
            connection.close();
            sleep();
        } catch (SQLException e) {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException ignored) {
            }
            if (e.getErrorCode()==1062) {
                throw new UserAlreadyExistsException(login);
            }
            throw new UnknownSQLException(e);
        }
    }

    public ArrayList<String> fetchAllUsers() throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException, UnknownSQLException {
        Connection connection = null;
        try {
            connection = connector.getConnection(true);
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("CALL `select_all_users`");
            ArrayList<String> userList = new ArrayList<>();
            while (resultSet.next()) {
                userList.add(resultSet.getString("login"));
            }
            connection.close();
            sleep();
            return userList;
        } catch (SQLException e) {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException ignored) {

            }
            throw new UnknownSQLException(e);
        }
    }

    public void resetDatabase() throws IOException, ClassNotFoundException, IllegalAccessException, InstantiationException, UnknownSQLException {
        Connection connection = null;
        try {
            Connector rootConnector = new Connector("jdbc:mysql://localhost?user=root");
            connection = rootConnector.getConnection(false);
            Statement statement = connection.createStatement();
            FileInputStream resetFile = new FileInputStream("sql/create.sql");
            Scanner scanner = new Scanner(resetFile);
            scanner.useDelimiter(Pattern.compile("(;;)"));
            while (scanner.hasNext()) {
                String str = scanner.next();
                statement.addBatch(str);
                //System.out.println(str + "\n_____________________");
            }
            scanner.close();
            statement.executeBatch();
            connection.commit();
            connection.close();
            sleep();
        } catch (SQLException e) {
            try {
                if (connection != null) {
                    connection.rollback();
                    connection.close();
                }
            } catch (SQLException ignored) {

            }
            throw new UnknownSQLException(e);
        }
    }

    public boolean tryLogin(String login) throws ClassNotFoundException, IllegalAccessException, InstantiationException, UnknownSQLException {
        Connection connection = null;
        try {
            connection = connector.getConnection(true);
            PreparedStatement checkStatement = connection.prepareStatement("SELECT `try_login`(?)");
            checkStatement.setString(1, login);
            ResultSet resultSet = checkStatement.executeQuery();
            resultSet.next();
            boolean result = resultSet.getBoolean(1);
            connection.close();
            sleep();
            return result;
        } catch (SQLException e) {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException ignored) {

            }
            throw new UnknownSQLException(e);
        }
    }

    public static void die(Throwable e) {
        e.printStackTrace();
        System.exit(1);
    }

}
