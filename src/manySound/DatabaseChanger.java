package manySound;

import manySound.exceptions.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
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
        } catch (SQLException e) {
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

    public void log(String s) {
        if (debug) {
            System.out.println(s);
        }
    }

     public Vector<Vector<String>> fetchMeetings(String login) throws UnknownSQLException {
        Connection connection = null;
        try {
            connection = connector.getConnection(true);
            PreparedStatement statement = connection.prepareStatement("CALL `view_meetings`(?)");
            statement.setString(1, login);
            ResultSet resultSet = statement.executeQuery();
            Vector<Vector<String>> meetingVector = new Vector<>();
            while (resultSet.next()) {
                Vector<String> meeting = new Vector<>();
                meeting.add(resultSet.getString("id_meeting"));
                meeting.add(resultSet.getString("name"));
                meeting.add(resultSet.getString("description"));
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

    class MeetingProps {
        String name;
        String description;
        boolean isOwner;
    }

    public MeetingProps getMeetingProps(int meeting, String login) throws UnknownSQLException, MeetingNotFoundException {
        Connection connection = null;
        try {
            connection = connector.getConnection(true);
            PreparedStatement statement = connection.prepareStatement("CALL `view_meeting`(?,?)");
            log("getMeetingProps(" + meeting + ", " + login + ")");
            statement.setInt(1, meeting);
            statement.setString(2, login);
            ResultSet resultSet = statement.executeQuery();
            if (! resultSet.next()) {
                throw new MeetingNotFoundException(meeting);
            }
            MeetingProps props = new MeetingProps();
            props.name = resultSet.getString("name");
            props.description = resultSet.getString("description");
            props.isOwner = resultSet.getBoolean("is_owner");
            connection.close();
            sleep();
            return props;
        } catch (SQLException e) {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException ignored) {
            }
            throw new UnknownSQLException(e);
        } catch (MeetingNotFoundException e) {
            try {
                connection.close();
            } catch (SQLException ignored) {
            }
            throw e;
        }
    }

    class InstrumentProps {
        String description;
        int type;
    }

    public InstrumentProps getInstrumentProps(int instrument, String login) throws UnknownSQLException, MeetingNotFoundException {
        Connection connection = null;
        try {
            connection = connector.getConnection(true);
            PreparedStatement statement = connection.prepareStatement("CALL `view_instrument`(?)");
            log("getInstrumentProps(" + instrument + ", " + login + ")");
            statement.setInt(1, instrument);
            ResultSet resultSet = statement.executeQuery();
            resultSet.next();
            InstrumentProps props = new InstrumentProps();
            props.description = resultSet.getString("description");
            props.type = resultSet.getInt("id_instrument_type");
            connection.close();
            sleep();
            return props;
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

    class InstrumentList {
        Vector<Integer> ids;
        Vector<String> descriptions;
    }

    public InstrumentList fetchInstruments(int meeting, String login) throws UnknownSQLException {
        Connection connection = null;
        try {
            connection = connector.getConnection(true);
            PreparedStatement statement = connection.prepareStatement("CALL `view_instruments`(?,?)");
            log("fetchInstruments(" + meeting + ", " + login + ")");
            statement.setInt(1, meeting);
            statement.setString(2, login);
            ResultSet resultSet = statement.executeQuery();
            InstrumentList ans = new InstrumentList();
            ans.descriptions = new Vector<>();
            ans.ids = new Vector<>();
            while (resultSet.next()) {
                ans.ids.add(resultSet.getInt("id_instrument"));
                ans.descriptions.add(resultSet.getString("description"));
            }
            connection.close();
            sleep();
            return ans;
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

    public Vector<String> fetchRecipients(int meeting, String login, int instrument) throws UnknownSQLException {
        Connection connection = null;
        try {
            connection = connector.getConnection(true);
            PreparedStatement statement = connection.prepareStatement("CALL `view_recipients`(?,?,?)");
            log("fetchRecipients(" + meeting + ", " + login + ", " + instrument + ")");
            statement.setInt(1, meeting);
            statement.setString(2, login);
            statement.setInt(3,instrument);
            ResultSet resultSet = statement.executeQuery();
            Vector<String> ans = new Vector<>();
            while (resultSet.next()) {
                ans.add(resultSet.getString("login"));
            }
            connection.close();
            sleep();
            return ans;
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
        addUser(login, "12345");
    }

    public void addUser(String login, String password) throws UserShownException {
        Connection connection = null;
        try {
            connection = connector.getConnection(true);
            PreparedStatement addStatement = connection.prepareStatement("CALL `insert_new_user`(?,?)");
            addStatement.setString(1, login);
            addStatement.setString(2, password);
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

    public void addMeeting(String currentUserName, String name) throws UserShownException {
        Connection connection = null;
        try {
            connection = connector.getConnection(true);
            PreparedStatement addStatement = connection.prepareStatement("CALL `insert_new_meeting`(?,?)");
            addStatement.setString(1, currentUserName);
            addStatement.setString(2, name);
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
                throw new SomethingAlreadyExistsException(name);
            }
            throw new UnknownSQLException(e);
        }
    }

    public ArrayList<String> fetchAllUsers() throws UnknownSQLException {
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

    public static void setStringArg(PreparedStatement statement, int argPos, String arg) throws SQLException {
        if (argPos>0) {
            statement.setString(argPos, arg);
        }
    }

    public static void setIntArg(PreparedStatement statement, int argPos, int arg) throws SQLException {
        if (argPos>0) {
            statement.setInt(argPos, arg);
        }
    }

    public int getInstrumentType(int instrument) throws UnknownSQLException {
        Connection connection = null;
        int ans;
        try {
            connection = connector.getConnection(true);
            PreparedStatement statement = connection.prepareStatement("SELECT `instr_type`(?)");
            statement.setInt(1, instrument);
            ResultSet resultSet = statement.executeQuery();
            resultSet.next();
            ans = resultSet.getInt(1);
            connection.close();
            sleep();
        } catch (SQLException e) {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException ignored) {
            }
            throw new UnknownSQLException(e);
        }
        return ans;
    }

    public void applyInstrument(String author, int meeting, int instrument, String[] recipients) throws UnknownSQLException {
        Connection connection = null;
        try {
            log(author + " called " + Arrays.toString(recipients));
            connection = connector.getConnection(true);
            PreparedStatement statement = connection.prepareStatement("CALL view_script(?)");
            statement.setInt(1, getInstrumentType(instrument));
            ResultSet resultSet = statement.executeQuery();
            resultSet.next();
            String query = resultSet.getString("script");
            String argPos = resultSet.getString("script_arg");
            Scanner argsPosScanner = new Scanner(argPos);
            argsPosScanner.useDelimiter(Pattern.compile("[,;\n ]"));
            PreparedStatement statement1 = connection.prepareStatement(query);

            setStringArg(statement1, argsPosScanner.nextInt(), author);
            setIntArg(statement1, argsPosScanner.nextInt(), meeting);
            int i = 0;
            while (argsPosScanner.hasNextInt()) {
                setStringArg(statement1, argsPosScanner.nextInt(), recipients[i++]);
            }

            statement1.execute();
            connection.close();
            sleep();
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
                log(str + ";;");
                statement.addBatch(str);
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

    public boolean tryLogin(String login, String password) throws ClassNotFoundException, IllegalAccessException, InstantiationException, UnknownSQLException {
        Connection connection = null;
        try {
            connection = connector.getConnection(true);
            PreparedStatement checkStatement = connection.prepareStatement("SELECT `try_login`(?,?)");
            checkStatement.setString(1, login);
            checkStatement.setString(2, password);
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