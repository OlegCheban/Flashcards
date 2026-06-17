package ru.flashcards.telegram.bot.botapi;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class UserMessageTypeBuffer {
    private static final long MAX_PENDING_REQUESTS = 10_000;
    private static final Duration REQUEST_TTL = Duration.ofMinutes(30);

    private final Cache<Long, PendingMessageRequest> buffer = Caffeine.newBuilder()
            .maximumSize(MAX_PENDING_REQUESTS)
            .expireAfterWrite(REQUEST_TTL)
            .build();

    public void putRequest(Long chatId, Long entityId, MessageType messageType) {
        buffer.put(chatId, new PendingMessageRequest(entityId, messageType));
    }

    public Long getEntityId(Long chatId) {
        var request = buffer.getIfPresent(chatId);
        return request != null ? request.entityId() : null;
    }

    public MessageType getMessageType(Long chatId) {
        var request = buffer.getIfPresent(chatId);
        return request != null ? request.messageType() : MessageType.COMMAND;
    }

    public void removeRequest(Long chatId) {
        buffer.invalidate(chatId);
    }

    private record PendingMessageRequest(Long entityId, MessageType messageType) {
    }
}
