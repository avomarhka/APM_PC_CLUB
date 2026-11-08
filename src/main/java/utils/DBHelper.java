package utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DBHelper {
    private static final Logger logger = LoggerFactory.getLogger(DBHelper.class);
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
                logger.debug("Загружены настройки подключения (url, name)");
            } else {
                logger.error("Файл config.properties не найден");
            }
        } catch (Exception e) {
            logger.error("Ошибка загрузки config.properties", e);
        }
    }

    public static boolean initConnection(String user, String password) {
        String fullUrl = dbUrl + dbName;
        logger.info("Подключение к {} пользователем {}", fullUrl, user);
        try {
            connection = DriverManager.getConnection(fullUrl, user, password);
            logger.info("Соединение установлено");
            return true;
        } catch (SQLException e) {
            logger.error("Ошибка подключения к базе данных", e);
            return false;
        }
    }

    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                throw new SQLException("Соединение не установлено. Вызовите initConnection().");
            }
        } catch (SQLException e) {
            logger.error("Соединение с базой данных недоступно", e);
        }
        return connection;
    }

    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                logger.info("Соединение закрыто");
            } catch (SQLException e) {
                logger.error("Ошибка закрытия соединения", e);
            }
        }
    }
}
