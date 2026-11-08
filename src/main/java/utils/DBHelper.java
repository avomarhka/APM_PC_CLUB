package utils;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DBHelper {
    private static String dbUrl;
    private static String dbName;
    private static Connection connection;

    static {
        Properties props = new Properties();
        try (InputStream is = DBHelper.class.getResourceAsStream("/config.properties")) {
            if (is != null) {
                props.load(is);
                dbUrl = props.getProperty("db.url");
                dbName = props.getProperty("db.name");
            } else {
                System.err.println("Файл config.properties не найден!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean initConnection(String user, String password) {
        try {
            connection = DriverManager.getConnection(dbUrl + dbName, user, password);
            return true;
        } catch (SQLException e) {
            System.err.println("Ошибка аутентификации: " + e.getMessage());
            return false;
        }
    }

    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                throw new SQLException("Соединение не установлено. Вызовите initConnection().");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return connection;
    }

    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("Соединение с БД закрыто.");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}