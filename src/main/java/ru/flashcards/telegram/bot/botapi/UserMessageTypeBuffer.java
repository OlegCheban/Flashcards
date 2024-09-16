package ru.flashcards.telegram.bot.botapi;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class UserMessageTypeBuffer {
    private final Map<Long, Map<Long, MessageType>> buffer = new ConcurrentHashMap<>();

    public void putRequest(Long chatId, Long entityId, MessageType messageType) {
        buffer.computeIfAbsent(chatId, k -> new HashMap<>()).put(entityId, messageType);
    }

    public Long getEntityId(Long chatId) {
        var map = buffer.get(chatId);
        return (map != null && !map.isEmpty()) ? map.keySet().iterator().next() : null;
    }

    public MessageType getMessageType(Long chatId) {
        var map = buffer.get(chatId);
        if (map != null && !map.isEmpty()) {
            return map.values().iterator().next();
        }
        return MessageType.COMMAND;
    }

    public void removeRequest(Long chatId) {
        buffer.remove(chatId);
    }
}
