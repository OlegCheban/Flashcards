package ru.flashcards.telegram.bot.botapi;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class UserMessageTypeBufferTest {

    @Test
    void returnsCommandWhenRequestIsAbsent() {
        UserMessageTypeBuffer buffer = new UserMessageTypeBuffer();

        assertNull(buffer.getEntityId(1L));
        assertEquals(MessageType.COMMAND, buffer.getMessageType(1L));
    }

    @Test
    void storesPendingRequestByChatId() {
        UserMessageTypeBuffer buffer = new UserMessageTypeBuffer();

        buffer.putRequest(1L, 10L, MessageType.CHANGE_TRANSLATION);

        assertEquals(10L, buffer.getEntityId(1L));
        assertEquals(MessageType.CHANGE_TRANSLATION, buffer.getMessageType(1L));
    }

    @Test
    void replacesPendingRequestForSameChatId() {
        UserMessageTypeBuffer buffer = new UserMessageTypeBuffer();

        buffer.putRequest(1L, 10L, MessageType.CHANGE_TRANSLATION);
        buffer.putRequest(1L, 20L, MessageType.CHANGE_TRANSLATION);

        assertEquals(20L, buffer.getEntityId(1L));
    }

    @Test
    void removesPendingRequest() {
        UserMessageTypeBuffer buffer = new UserMessageTypeBuffer();

        buffer.putRequest(1L, 10L, MessageType.CHANGE_TRANSLATION);
        buffer.removeRequest(1L);

        assertNull(buffer.getEntityId(1L));
        assertEquals(MessageType.COMMAND, buffer.getMessageType(1L));
    }
}
