package ru.example.emailparser.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DatabaseManager {
    private static final String DB_URL = ""; // URL к базе данных
    private static final String DB_USER = ""; // замените на ваше имя пользователя
    private static final String DB_PASSWORD = ""; // замените на ваш пароль

    // Метод для сохранения учетных данных
    public void saveCredentials(String mail, String login, String password) {
        String sql = "INSERT INTO credentials (mail, login, pass) VALUES (?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, mail);
            pstmt.setString(2, login);
            pstmt.setString(3, password);
            pstmt.executeUpdate();

            System.out.println("Учетные данные успешно сохранены!");
        } catch (SQLException e) {
            System.err.println("Ошибка при сохранении учетных данных: " + e.getMessage());
        }
    }

    // Метод для сохранения информации о файлах
    public void saveFileInfoToDatabase(int userId, String fileName) {
        String sql = "INSERT INTO file_records (user_id, date, name_of_files) VALUES (?, CURRENT_TIMESTAMP, ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setString(2, fileName);
            pstmt.executeUpdate();

            System.out.println("Информация о файле успешно сохранена!");
        } catch (SQLException e) {
            System.err.println("Ошибка при сохранении информации о файле: " + e.getMessage());
        }
    }
}

