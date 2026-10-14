package ru.flashcards.telegram.bot.services.pronunciation;

import java.io.ByteArrayInputStream;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendAudio;
import org.telegram.telegrambots.meta.api.objects.Audio;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import ru.flashcards.telegram.bot.exception.PronunciationUnavailableException;
import ru.flashcards.telegram.bot.services.SendMessageService;

/**
 * Sends the pronunciation of a word as an MP3 audio. The audio is synthesized once per word:
 * Telegram's file_id of the uploaded audio is cached and reused for the next requests.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PronunciationService {
    private static final String PERFORMER = "Pronunciation";

    private final SpeechSynthesisClient speechSynthesisClient;
    private final Mp3Encoder mp3Encoder;
    private final PronunciationAudioCache pronunciationAudioCache;
    private final SendMessageService sendMessageService;

    /**
     * @return {@code false} if the pronunciation cannot be produced or sent right now
     */
    public boolean sendPronunciation(Long chatId, String word) {
        Optional<String> cachedFileId = pronunciationAudioCache.find(word);
        if (cachedFileId.isPresent()) {
            if (send(chatId, word, new InputFile(cachedFileId.get())).isPresent()) {
                return true;
            }
            // the cached file_id was rejected, the audio is uploaded again
            pronunciationAudioCache.remove(word);
        }

        byte[] mp3;
        try {
            mp3 = mp3Encoder.encode(speechSynthesisClient.synthesize(word));
        } catch (PronunciationUnavailableException e) {
            log.warn("Pronunciation is unavailable: word={}", word, e);
            return false;
        }

        Optional<Message> sent = send(chatId, word, new InputFile(new ByteArrayInputStream(mp3), fileName(word)));
        sent.map(Message::getAudio)
                .map(Audio::getFileId)
                .ifPresent(fileId -> pronunciationAudioCache.put(word, fileId));
        return sent.isPresent();
    }

    private Optional<Message> send(Long chatId, String word, InputFile audio) {
        SendAudio sendAudio = new SendAudio();
        sendAudio.setChatId(chatId);
        sendAudio.setAudio(audio);
        sendAudio.setTitle(word);
        sendAudio.setPerformer(PERFORMER);
        return sendMessageService.sendAudio(sendAudio);
    }

    private static String fileName(String word) {
        return word.trim().replaceAll("[^A-Za-z0-9-]+", "_") + ".mp3";
    }
}
