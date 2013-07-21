package manySound;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Connector {

    String connectionURL;
    static final String DEFAULT_URL = "jdbc:mysql://localhost/test";
    //static final String DEFAULT_URL = "jdbc:sqlserver://localhost/test";

    public Connector(String url) throws SQLException {
        connectionURL = url;
        init();
    }

    public Connector() throws SQLException {
        this(DEFAULT_URL);
    }

    public Connection getConnection(boolean autoCommit) throws SQLException {
        System.out.println("Conecting to \"" + connectionURL + "\"");
        Connection connection = DriverManager.getConnection(connectionURL);
        connection.setAutoCommit(autoCommit);
        return connection;
    }

    public static void main(String[] args) throws SQLException {
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

    public static void init() throws SQLException {
        //DriverManager.registerDriver(new com.microsoft.sqlserver.jdbc.SQLServerDriver());
        DriverManager.registerDriver(new com.mysql.jdbc.Driver());
    }
}