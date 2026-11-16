package ru.flashcards.telegram.bot.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.ai.retry.TransientAiException;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import com.fasterxml.jackson.databind.ObjectMapper;

import ru.flashcards.telegram.bot.botapi.CallbackDataCodec;
import ru.flashcards.telegram.bot.botapi.keyboard.InlineKeyboardFactory;
import ru.flashcards.telegram.bot.db.NotificationsDao;
import ru.flashcards.telegram.bot.db.dto.UserFlashcardPushMono;

class RandomNotificationServiceTest {
    private static final String TEXT_WITHOUT_CONTEXT =
            "*leverage* /ˈlevərɪdʒ/ 📌\n\nuse something to maximum advantage";

    private final NotificationsDao notificationsDao = mock(NotificationsDao.class);
    private final SendMessageService sendMessageService = mock(SendMessageService.class);
    private final FlashcardsContextService contextService = mock(FlashcardsContextService.class);
    private final RandomNotificationService service = new RandomNotificationService(notificationsDao,
            sendMessageService, contextService, new InlineKeyboardFactory(new CallbackDataCodec(new ObjectMapper())));

    @Test
    void appendsAiContextForPremiumUser() {
        when(notificationsDao.getUserFlashcardsRandomNotification()).thenReturn(List.of(flashcard(true)));
        when(contextService.generateContext("leverage")).thenReturn("A story");

        service.send();

        assertEquals(TEXT_WITHOUT_CONTEXT + "\n\nA story", sentText());
        verify(notificationsDao).updatePushTimestamp(10L);
    }

    @Test
    void doesNotCallAiForRegularUser() {
        when(notificationsDao.getUserFlashcardsRandomNotification()).thenReturn(List.of(flashcard(false)));

        service.send();

        assertEquals(TEXT_WITHOUT_CONTEXT, sentText());
        verifyNoInteractions(contextService);
        verify(notificationsDao).updatePushTimestamp(10L);
    }

    @Test
    void sendsNotificationWithoutContextWhenAiIsRateLimited() {
        when(notificationsDao.getUserFlashcardsRandomNotification()).thenReturn(List.of(flashcard(true)));
        when(contextService.generateContext("leverage")).thenThrow(new TransientAiException("HTTP 429"));

        service.send();

        assertEquals(TEXT_WITHOUT_CONTEXT, sentText());
        verify(notificationsDao).updatePushTimestamp(10L);
    }

    private static UserFlashcardPushMono flashcard(boolean isPremium) {
        return new UserFlashcardPushMono(10L, "leverage", "use something to maximum advantage", 1L, "ˈlevərɪdʒ",
                isPremium);
    }

    private String sentText() {
        ArgumentCaptor<SendMessage> sent = ArgumentCaptor.forClass(SendMessage.class);
        verify(sendMessageService).send(sent.capture());
        return sent.getValue().getText();
    }
}
