package ru.example.emailparser.database;

import javax.mail.internet.MimeUtility;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DatabaseManager {
    private static final String DB_URL = ""; // URL к базе данных
    private static final String DB_USER = ""; // замените на ваше имя пользователя
    private static final String DB_PASSWORD = ""; // замените на ваш пароль

    // Метод для получения ID пользователя, если учетные данные существуют
    public int getUserId(String mail, String login, String password) {
        String sql = "SELECT id FROM credentials WHERE mail = ? AND login = ? AND pass = ?";
        System.out.println("Попытка получить ID пользователя по почте: " + mail);
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, mail);
            pstmt.setString(2, login);
            pstmt.setString(3, password);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                System.out.println("Пользователь найден, ID: " + rs.getInt("id"));
                return rs.getInt("id");  // Возвращаем ID пользователя, если учетные данные существуют
            } else {
                System.out.println("Пользователь не найден.");
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при получении ID пользователя: " + e.getMessage());
        }
        return -1;  // Если пользователя не существует
    }

    // Метод для сохранения учетных данных и возврата userId
    public int saveCredentialsAndGetUserId(String mail, String login, String password) {
        int userId = getUserId(mail, login, password);
        if (userId == -1) {  // Если пользователь не существует, создаем его
            String sql = "INSERT INTO credentials (mail, login, pass) VALUES (?, ?, ?)";
            System.out.println("Пользователь не найден. Вставляем нового пользователя: " + mail);
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                 PreparedStatement pstmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

                pstmt.setString(1, mail);
                pstmt.setString(2, login);
                pstmt.setString(3, password);

                int affectedRows = pstmt.executeUpdate();
                if (affectedRows > 0) {
                    System.out.println("Учетные данные успешно вставлены!");
                    ResultSet rs = pstmt.getGeneratedKeys();
                    if (rs.next()) {
                        userId = rs.getInt(1);  // Возвращаем сгенерированный ID нового пользователя
                        System.out.println("Сгенерированный ID нового пользователя: " + userId);
                    }
                } else {
                    System.out.println("Ошибка: учетные данные не вставлены.");
                }
            } catch (SQLException e) {
                System.err.println("Ошибка при сохранении учетных данных: " + e.getMessage());
            }
        } else {
            System.out.println("Пользователь с такими учетными данными уже существует.");
        }
        return userId;  // Возвращаем ID существующего или нового пользователя
    }

    // Метод для сохранения информации о файлах
    public void saveFileInfoToDatabase(int userId, String fileName) {
        // Проверка на существование userId
        if (userId <= 0) {
            System.err.println("Ошибка: недействительный ID пользователя.");
            return;
        }

        // Декодирование и очистка имени файла
        String decodedFileName = decodeFileName(fileName);
        String sanitizedFileName = sanitizeFileName(decodedFileName);

        // Генерация уникального имени файла
        String uniqueFileName = generateUniqueFileName(sanitizedFileName);

        String sql = "INSERT INTO file_records (user_id, date, name_of_files) VALUES (?, CURRENT_TIMESTAMP, ?)";
        System.out.println("Сохраняем информацию о файле: " + uniqueFileName + " для пользователя с ID: " + userId);

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setString(2, uniqueFileName);
            pstmt.executeUpdate();

            System.out.println("Информация о файле успешно сохранена!");
        } catch (SQLException e) {
            System.err.println("Ошибка при сохранении информации о файле: " + e.getMessage());
        }
    }

    // Метод для декодирования имени файла
    public String decodeFileName(String fileName) {
        try {
            // Декодируем имя файла
            String decodedFileName = MimeUtility.decodeText(fileName);
            return decodedFileName;
        } catch (UnsupportedEncodingException e) {
            System.err.println("Ошибка при декодировании имени файла: " + e.getMessage());
            return fileName; // В случае ошибки возвращаем оригинальное имя
        }
    }

    // Пример метода генерации уникального имени файла
    private String generateUniqueFileName(String fileName) {
        String uniqueFileName = fileName;
        int counter = 1;
        String sql = "SELECT COUNT(*) FROM file_records WHERE name_of_files = ?";

        // Проверка существования файла с таким именем
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            while (true) {
                pstmt.setString(1, uniqueFileName);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    // Если файл с таким именем существует, добавляем счетчик
                    uniqueFileName = fileName + "(" + counter + ")";
                    counter++;
                } else {
                    break; // Если файла нет, выходим из цикла
                }
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при проверке существования файла: " + e.getMessage());
        }
        return uniqueFileName;
    }

    public String sanitizeFileName(String fileName) {
        return fileName.replaceAll("[\\\\/:*?\"<>|]", "_");
    }
}
