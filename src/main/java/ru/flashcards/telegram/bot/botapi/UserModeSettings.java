package ru.flashcards.telegram.bot.botapi;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class UserModeSettings {
    private final Map<Long, UserMode> modeMap = new ConcurrentHashMap<>();

    /**
     * Sets the user mode for a specific chat ID.
     * @param chatId The chat ID.
     * @param userMode The user mode to set.
     */
    public void setMode(Long chatId, UserMode userMode) {
        modeMap.put(chatId, userMode);
    }

    /**
     * Retrieves the user mode for a specific chat ID.
     * @param chatId The chat ID.
     * @return The user mode associated with the chat ID, or null if no mapping exists.
     */
    public UserMode getMode(Long chatId) {
        return modeMap.get(chatId);
    }

    /**
     * Removes the user mode for a specific chat ID.
     * @param chatId The chat ID.
     * @return The previous user mode associated with the chat ID, or null if there was no mapping.
     */
    public UserMode removeMode(Long chatId) {
        return modeMap.remove(chatId);
    }
}
