package manySound;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Connector {

    String connectionURL;

    public Connector(String url) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        connectionURL = url;
        init();
    }

    public Connector() throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        this("jdbc:mysql://localhost/test");
    }

    public Connection getConnection(boolean autoCommit) throws SQLException {
        System.out.println(connectionURL);
        Connection connection = DriverManager.getConnection(connectionURL);
        connection.setAutoCommit(autoCommit);
        return connection;
    }

    /*public Statement getStatement(boolean autoCommit) throws SQLException {
        Connection connection = getConnection(autoCommit);
        return connection.createStatement();
    }*/

    public static void main(String[] args) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        Connection conn = null;
        Connector connector = new Connector();
        try {
            conn = connector.getConnection(true);
            System.out.println("Database connection established");
        } catch (Exception e) {
            System.err.println("Cannot connect to database server");
            e.printStackTrace();
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                    System.out.println("Database connection terminated");
                } catch (Exception ignored) {
                }
            }
        }
    }

    public static void init() throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        Class.forName("com.mysql.jdbc.Driver").newInstance();
    }
}
