package ru.flashcards.telegram.bot.botapi.wateringSession;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class WateringSessionTiming {
    private final ConcurrentHashMap<Long, LocalDateTime> sessionMap = new ConcurrentHashMap<>();

    /**
     * Sets the start date and time for a specific chat session.
     * @param chatId The chat ID.
     * @param localDateTime The start date and time.
     * @return The previous start date and time associated with the chat ID, or null if there was no mapping.
     */
    public LocalDateTime setStartDateTime(Long chatId, LocalDateTime localDateTime) {
        return sessionMap.put(chatId, localDateTime);
    }

    /**
     * Retrieves the start date and time for a specific chat session.
     * @param chatId The chat ID.
     * @return The start date and time associated with the chat ID, or null if no mapping exists.
     */
    public LocalDateTime getStartDateTime(Long chatId) {
        return sessionMap.get(chatId);
    }
}
