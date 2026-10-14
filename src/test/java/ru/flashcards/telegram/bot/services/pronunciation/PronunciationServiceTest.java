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

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.telegram.telegrambots.meta.api.methods.send.SendAudio;
import org.telegram.telegrambots.meta.api.objects.Audio;
import org.telegram.telegrambots.meta.api.objects.Message;

import ru.flashcards.telegram.bot.exception.PronunciationUnavailableException;
import ru.flashcards.telegram.bot.services.SendMessageService;

class PronunciationServiceTest {
    private static final long CHAT_ID = 1L;
    private static final String WORD = "Intrinsic";
    private static final byte[] MP3 = {2};

    private final PronunciationSource recordings = mock(PronunciationSource.class);
    private final PronunciationSource synthesis = mock(PronunciationSource.class);
    private final SendMessageService sendMessageService = mock(SendMessageService.class);
    private final PronunciationAudioCache cache = new PronunciationAudioCache();
    private final PronunciationService service =
            new PronunciationService(List.of(recordings, synthesis), cache, sendMessageService);

    @Test
    void usesFirstSourceThatHasTheRecordingAndCachesFileId() {
        when(recordings.findMp3(WORD)).thenReturn(Optional.of(MP3));
        when(sendMessageService.sendAudio(any())).thenReturn(Optional.of(messageWithAudio("file-1")));

        assertTrue(service.sendPronunciation(CHAT_ID, WORD));
        assertTrue(service.sendPronunciation(CHAT_ID, "intrinsic"));

        verify(recordings, times(1)).findMp3(any());
        verify(synthesis, never()).findMp3(any());
        ArgumentCaptor<SendAudio> sent = ArgumentCaptor.forClass(SendAudio.class);
        verify(sendMessageService, times(2)).sendAudio(sent.capture());
        assertTrue(sent.getAllValues().get(0).getAudio().isNew());
        assertEquals("file-1", sent.getAllValues().get(1).getAudio().getAttachName());
        assertEquals(WORD, sent.getAllValues().get(0).getTitle());
    }

    @Test
    void fallsBackToNextSourceWhenRecordingIsMissing() {
        when(recordings.findMp3(WORD)).thenReturn(Optional.empty());
        when(synthesis.findMp3(WORD)).thenReturn(Optional.of(MP3));
        when(sendMessageService.sendAudio(any())).thenReturn(Optional.of(messageWithAudio("file-1")));

        assertTrue(service.sendPronunciation(CHAT_ID, WORD));

        verify(synthesis).findMp3(WORD);
    }

    @Test
    void fallsBackToNextSourceWhenSourceFails() {
        when(recordings.findMp3(WORD)).thenThrow(new PronunciationUnavailableException("status=522"));
        when(synthesis.findMp3(WORD)).thenReturn(Optional.of(MP3));
        when(sendMessageService.sendAudio(any())).thenReturn(Optional.of(messageWithAudio("file-1")));

        assertTrue(service.sendPronunciation(CHAT_ID, WORD));
    }

    @Test
    void reportsUnavailableWhenAllSourcesFail() {
        when(recordings.findMp3(WORD)).thenThrow(new PronunciationUnavailableException("status=522"));
        when(synthesis.findMp3(WORD)).thenThrow(new PronunciationUnavailableException("status=400"));

        assertFalse(service.sendPronunciation(CHAT_ID, WORD));

        verify(sendMessageService, never()).sendAudio(any());
        assertTrue(cache.find(WORD).isEmpty());
    }

    @Test
    void uploadsAgainWhenCachedFileIdIsRejected() {
        cache.put(WORD, "stale");
        when(recordings.findMp3(WORD)).thenReturn(Optional.of(MP3));
        when(sendMessageService.sendAudio(any()))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(messageWithAudio("file-2")));

        assertTrue(service.sendPronunciation(CHAT_ID, WORD));

        assertEquals(Optional.of("file-2"), cache.find("intrinsic"));
    }

    private static Message messageWithAudio(String fileId) {
        Audio audio = new Audio();
        audio.setFileId(fileId);
        Message message = new Message();
        message.setAudio(audio);
        return message;
    }
}
