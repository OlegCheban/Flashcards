package ru.flashcards.telegram.bot.services.pronunciation;

import java.io.ByteArrayInputStream;
import java.util.List;
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
 * Sends the pronunciation of a word as an MP3 audio. The sources are tried in order (real recordings first,
 * speech synthesis last) until one returns the audio. Telegram's file_id of the uploaded audio is cached,
 * so every word is fetched from the sources only once.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PronunciationService {
    private static final String PERFORMER = "Pronunciation";

    /**
     * Ordered by {@link org.springframework.core.annotation.Order}.
     */
    private final List<PronunciationSource> pronunciationSources;
    private final PronunciationAudioCache pronunciationAudioCache;
    private final SendMessageService sendMessageService;

    /**
     * @return {@code false} if no source can provide the pronunciation right now or it cannot be sent
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

        Optional<byte[]> mp3 = findMp3(word);
        if (mp3.isEmpty()) {
            return false;
        }

        Optional<Message> sent = send(chatId, word, new InputFile(new ByteArrayInputStream(mp3.get()), fileName(word)));
        sent.map(Message::getAudio)
                .map(Audio::getFileId)
                .ifPresent(fileId -> pronunciationAudioCache.put(word, fileId));
        return sent.isPresent();
    }

    /**
     * A failure of one source is logged and the next source is tried.
     */
    private Optional<byte[]> findMp3(String word) {
        for (PronunciationSource source : pronunciationSources) {
            String sourceName = source.getClass().getSimpleName();
            try {
                Optional<byte[]> mp3 = source.findMp3(word);
                if (mp3.isPresent()) {
                    log.debug("Pronunciation found: word={}, source={}", word, sourceName);
                    return mp3;
                }
                log.debug("Pronunciation not found: word={}, source={}", word, sourceName);
            } catch (PronunciationUnavailableException e) {
                log.warn("Pronunciation source failed: word={}, source={}", word, sourceName, e);
            }
        }
        log.warn("Pronunciation is unavailable in all sources: word={}", word);
        return Optional.empty();
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
