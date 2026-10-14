package ru.flashcards.telegram.bot.sheduler;

import java.util.concurrent.TimeUnit;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import ru.flashcards.telegram.bot.services.RandomNotificationService;
import ru.flashcards.telegram.bot.services.SpacedRepetitionNotificationService;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationManager {

    private final SpacedRepetitionNotificationService spacedRepetitionNotification;
    private final RandomNotificationService randomNotification;

    /**
     * The notification kinds are isolated: a failure of one of them does not cancel the other.
     */
    @Scheduled(fixedRate = 5, timeUnit = TimeUnit.HOURS)
    public void run() {
        try {
            randomNotification.send();
        } catch (RuntimeException e) {
            log.error("Random notifications failed", e);
        }

        try {
            spacedRepetitionNotification.send();
        } catch (RuntimeException e) {
            log.error("Spaced repetition notifications failed", e);
        }
    }
}
