package ru.example.emailparser;


import ru.example.emailparser.parser.ImapJob;
import ru.example.emailparser.scheduler.JobScheduler;

import java.util.Scanner;

public class EmailParserApp {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Выберите job:");
        System.out.println("1 - IMAP");
        System.out.println("2 - SMTP");

        int jobType = scanner.nextInt();
        scanner.nextLine();  // Consume newline

        if (jobType == 1) {
            System.out.println("Укажите адрес почты:");
            String email = scanner.nextLine();
            System.out.println("Укажите логин:");
            String login = scanner.nextLine();
            System.out.println("Укажите пароль:");
            String password = scanner.nextLine();

            // Запуск Job IMAP по расписанию
            JobScheduler.scheduleImapJob(email, login, password);

            // Запуск Job IMAP в отдельном потоке
            ImapJob imapJob = new ImapJob(email, login, password);
            Thread imapThread = new Thread(imapJob);
            imapThread.start();
        } else if (jobType == 2) {
            System.out.println("Укажите id или название файла:");
            String fileName = scanner.nextLine();
            System.out.println("Укажите адрес почты:");
            String recipient = scanner.nextLine();
            // Логика для SMTP (еще не реализована)
        } else {
            System.out.println("Неверная команда.");
        }

        scanner.close();
    }

}
