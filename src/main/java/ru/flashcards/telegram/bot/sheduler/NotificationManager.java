package ru.flashcards.telegram.bot.sheduler;

import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.flashcards.telegram.bot.services.RandomNotificationService;
import ru.flashcards.telegram.bot.services.SpacedRepetitionNotificationService;

@Component
@AllArgsConstructor
public class NotificationManager {

    private SpacedRepetitionNotificationService spacedRepetitionNotification;
    private RandomNotificationService randomNotification;

    @Scheduled(cron = "0 * * * * *")
    private void run(){
        randomNotification.send();
        spacedRepetitionNotification.send();
    }
}
