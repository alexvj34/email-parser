package ru.example.emailparser.scheduler;

import ru.example.emailparser.parser.ImapJob;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.HashMap;
import java.util.Map;

public class JobScheduler {

    public static void scheduleImapJob(String email, String login, String password) {
        try {
            // Создаем JobDetail, указываем класс задачи (ImapJob)
            JobDetail jobDetail = JobBuilder.newJob(ImapJobQuartzWrapper.class)
                    .withIdentity("imapJob", "group1")
                    .build();

            // Передаем параметры в JobDataMap
            jobDetail.getJobDataMap().put("email", email);
            jobDetail.getJobDataMap().put("login", login);
            jobDetail.getJobDataMap().put("password", password);

            // Настраиваем триггер с cron-выражением для запуска каждые 10 минут
            Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity("imapTrigger", "group1")
                    .withSchedule(CronScheduleBuilder.cronSchedule("0 0/10 * * * ?"))
                    .build();

            // Настройка планировщика задач (Scheduler)
            Scheduler scheduler = new StdSchedulerFactory().getScheduler();
            scheduler.start();
            scheduler.scheduleJob(jobDetail, trigger);

            System.out.println("Job 1 - IMAP успешно запланирована!");

        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }

    public class ImapJobQuartzWrapper implements Job {

        @Override
        public void execute(JobExecutionContext context) throws JobExecutionException {
            // Получаем данные из JobDataMap
            String email = context.getJobDetail().getJobDataMap().getString("email");
            String login = context.getJobDetail().getJobDataMap().getString("login");
            String password = context.getJobDetail().getJobDataMap().getString("password");

            // Запускаем ImapJob
            ImapJob imapJob = new ImapJob(email, login, password);
            imapJob.run();
        }
    }
}
