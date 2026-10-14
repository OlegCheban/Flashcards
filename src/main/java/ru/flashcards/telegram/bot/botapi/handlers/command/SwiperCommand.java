package ru.flashcards.telegram.bot.botapi.handlers.command;

import static ru.flashcards.telegram.bot.botapi.BotReplyMsg.FLASHCARDS_NOT_FOUND_MSG;
import static ru.flashcards.telegram.bot.botapi.BotReplyMsg.UNRECOGNIZED_OPTION_MSG;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;

import lombok.RequiredArgsConstructor;

import ru.flashcards.telegram.bot.botapi.BotMessages;
import ru.flashcards.telegram.bot.botapi.records.SwiperParams;
import ru.flashcards.telegram.bot.botapi.swiper.SwiperView;
import ru.flashcards.telegram.bot.db.SwiperDao;
import ru.flashcards.telegram.bot.db.dto.SwiperFlashcard;
import ru.flashcards.telegram.bot.utils.Numbers;

/**
 * Command /s [prefix] [percentage]: opens the Swiper on the first matching flashcard.
 * The arguments may go in any order; a number is the learning percentage, anything else is the word prefix.
 */
@Component
@RequiredArgsConstructor
public class SwiperCommand {
    private static final Pattern WHITESPACE = Pattern.compile("\\s+");
    /**
     * The filters are repeated in every Swiper button, and Telegram limits callback data to 64 bytes.
     */
    private static final int MAX_PREFIX_BYTES = 10;
    private static final int MAX_PERCENTAGE_LENGTH = 3;

    private final SwiperDao swiperDao;
    private final SwiperView swiperView;

    public List<BotApiMethod<?>> open(Long chatId, String argument) {
        Optional<SwiperParams> params = parseParams(argument);
        if (params.isEmpty()) {
            return List.of(BotMessages.text(chatId, UNRECOGNIZED_OPTION_MSG));
        }

        String prefix = params.get().charCond();
        String percentage = params.get().prc();
        Optional<SwiperFlashcard> flashcard = swiperDao.getFirstSwiperFlashcard(chatId, prefix, percentage)
                .flatMap(firstId -> swiperDao.getSwiperFlashcard(chatId, firstId, prefix, percentage));
        if (flashcard.isEmpty()) {
            return List.of(BotMessages.text(chatId, FLASHCARDS_NOT_FOUND_MSG));
        }
        return List.of(swiperView.message(chatId, flashcard.get(), params.get()));
    }

    /**
     * @return empty if there are more than two arguments, two arguments of the same kind, or an argument is too long
     */
    private Optional<SwiperParams> parseParams(String argument) {
        String[] arguments = argument.isEmpty() ? new String[0] : WHITESPACE.split(argument);
        if (arguments.length > 2) {
            return Optional.empty();
        }

        String prefix = "";
        String percentage = "";
        for (String value : arguments) {
            if (Numbers.isDigits(value) && percentage.isEmpty()) {
                percentage = value;
            } else if (!Numbers.isDigits(value) && prefix.isEmpty()) {
                prefix = value;
            } else {
                return Optional.empty();
            }
        }

        if (prefix.getBytes(StandardCharsets.UTF_8).length > MAX_PREFIX_BYTES
                || percentage.length() > MAX_PERCENTAGE_LENGTH) {
            return Optional.empty();
        }
        return Optional.of(new SwiperParams(prefix, percentage));
    }
}
