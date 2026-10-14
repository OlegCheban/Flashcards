package ru.flashcards.telegram.bot.services.pronunciation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.telegram.telegrambots.meta.api.methods.send.SendAudio;
import org.telegram.telegrambots.meta.api.objects.Audio;
import org.telegram.telegrambots.meta.api.objects.Message;

import ru.flashcards.telegram.bot.exception.PronunciationUnavailableException;
import ru.flashcards.telegram.bot.services.SendMessageService;

class PronunciationServiceTest {
    private static final long CHAT_ID = 1L;
    private static final byte[] WAV = {1};
    private static final byte[] MP3 = {2};

    private final SpeechSynthesisClient speechSynthesisClient = mock(SpeechSynthesisClient.class);
    private final Mp3Encoder mp3Encoder = mock(Mp3Encoder.class);
    private final SendMessageService sendMessageService = mock(SendMessageService.class);
    private final PronunciationAudioCache cache = new PronunciationAudioCache();
    private final PronunciationService service =
            new PronunciationService(speechSynthesisClient, mp3Encoder, cache, sendMessageService);

    @BeforeEach
    void setUp() {
        when(speechSynthesisClient.synthesize("Intrinsic")).thenReturn(WAV);
        when(mp3Encoder.encode(WAV)).thenReturn(MP3);
    }

    @Test
    void synthesizesOnceAndReusesFileId() {
        when(sendMessageService.sendAudio(any())).thenReturn(Optional.of(messageWithAudio("file-1")));

        assertTrue(service.sendPronunciation(CHAT_ID, "Intrinsic"));
        assertTrue(service.sendPronunciation(CHAT_ID, "intrinsic"));

        verify(speechSynthesisClient, times(1)).synthesize(any());
        ArgumentCaptor<SendAudio> sent = ArgumentCaptor.forClass(SendAudio.class);
        verify(sendMessageService, times(2)).sendAudio(sent.capture());
        assertTrue(sent.getAllValues().get(0).getAudio().isNew());
        assertEquals("file-1", sent.getAllValues().get(1).getAudio().getAttachName());
        assertEquals("Intrinsic", sent.getAllValues().get(0).getTitle());
    }

    @Test
    void uploadsAgainWhenCachedFileIdIsRejected() {
        cache.put("Intrinsic", "stale");
        when(sendMessageService.sendAudio(any()))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(messageWithAudio("file-2")));

        assertTrue(service.sendPronunciation(CHAT_ID, "Intrinsic"));

        verify(speechSynthesisClient).synthesize("Intrinsic");
        assertEquals(Optional.of("file-2"), cache.find("intrinsic"));
    }

    @Test
    void reportsUnavailableSpeechSynthesis() {
        when(speechSynthesisClient.synthesize("Intrinsic")).thenThrow(new PronunciationUnavailableException("down"));

        assertFalse(service.sendPronunciation(CHAT_ID, "Intrinsic"));

        verify(sendMessageService, never()).sendAudio(any());
        assertTrue(cache.find("Intrinsic").isEmpty());
    }

    private static Message messageWithAudio(String fileId) {
        Audio audio = new Audio();
        audio.setFileId(fileId);
        Message message = new Message();
        message.setAudio(audio);
        return message;
    }
}
