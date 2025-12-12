package org.example.dao;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.Properties;

public class DatabaseConnection {
    private static DatabaseConnection instance;
    private Connection connection;
    private Properties properties;

    private DatabaseConnection() {
        try {
            properties = new Properties();
            InputStream input = getClass().getClassLoader()
                    .getResourceAsStream("application.properties");
            if (input != null) {
                properties.load(input);
            } else {
                // Значения по умолчанию
                properties.setProperty("db.url", "jdbc:h2:./database/financy_db");
                properties.setProperty("db.username", "sa");
                properties.setProperty("db.password", "");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                String url = properties.getProperty("db.url");
                String username = properties.getProperty("db.username");
                String password = properties.getProperty("db.password");
                connection = DriverManager.getConnection(url, username, password);
            } catch (SQLException e) {
                System.err.println("Ошибка подключения к базе данных: " + e.getMessage());
                throw e;
            }
        }
        return connection;
    }

    public void initializeDatabase() {
        try (Connection conn = getConnection()) {
            // Читаем schema.sql из ресурсов
            InputStream inputStream = getClass().getClassLoader()
                    .getResourceAsStream("schema.sql");

            if (inputStream != null) {
                String schemaSql = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);

                // Разделяем SQL-запросы по точке с запятой
                String[] queries = schemaSql.split(";");

                try (Statement stmt = conn.createStatement()) {
                    for (String query : queries) {
                        if (!query.trim().isEmpty()) {
                            stmt.execute(query);
                        }
                    }
                    System.out.println("База данных успешно инициализирована");
                }
            }
        } catch (Exception e) {
            System.err.println("Ошибка инициализации базы данных: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                connection = null;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}