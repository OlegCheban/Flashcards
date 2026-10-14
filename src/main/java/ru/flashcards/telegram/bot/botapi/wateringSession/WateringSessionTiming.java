package ru.flashcards.telegram.bot.botapi.wateringSession;

import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * When the current watering session question was sent to every user.
 */
@Component
@RequiredArgsConstructor
public class WateringSessionTiming {
    private final Map<Long, Instant> questionStartTimes = new ConcurrentHashMap<>();
    private final Clock clock;

    public void start(Long chatId) {
        questionStartTimes.put(chatId, Instant.now(clock));
    }

    /**
     * An unknown start time (e.g. lost on restart) is treated as answered in time.
     */
    public boolean isAnsweredInTime(Long chatId, int replyTimeSeconds) {
        Instant startTime = questionStartTimes.get(chatId);
        return startTime == null || !Instant.now(clock).isAfter(startTime.plusSeconds(replyTimeSeconds));
    }

    public void stop(Long chatId) {
        questionStartTimes.remove(chatId);
    }
}
