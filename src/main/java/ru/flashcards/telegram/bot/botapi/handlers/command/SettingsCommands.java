package ru.flashcards.telegram.bot.botapi.handlers.command;

import static ru.flashcards.telegram.bot.botapi.BotReplyMsg.DONE_MSG;
import static ru.flashcards.telegram.bot.botapi.BotReplyMsg.POSITIVE_NUMBER_EXPECTED_MSG;

import java.util.List;
import java.util.OptionalInt;
import java.util.function.IntConsumer;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;

import lombok.RequiredArgsConstructor;

import ru.flashcards.telegram.bot.botapi.BotMessages;
import ru.flashcards.telegram.bot.db.LearningExercisesDao;
import ru.flashcards.telegram.bot.db.NotificationsDao;
import ru.flashcards.telegram.bot.db.WateringSessionsDao;
import ru.flashcards.telegram.bot.utils.Numbers;

/**
 * Commands /ni, /fq and /wt that change numeric user settings.
 */
@Component
@RequiredArgsConstructor
public class SettingsCommands {
    private final NotificationsDao notificationsDao;
    private final LearningExercisesDao learningExercisesDao;
    private final WateringSessionsDao wateringSessionsDao;

    public List<BotApiMethod<?>> setNotificationInterval(Long chatId, String minutes) {
        return update(chatId, minutes, value -> notificationsDao.setNotificationInterval(value, chatId));
    }

    public List<BotApiMethod<?>> setTrainingFlashcardsQuantity(Long chatId, String quantity) {
        return update(chatId, quantity, value -> learningExercisesDao.setTrainingFlashcardsQuantity(value, chatId));
    }

    public List<BotApiMethod<?>> setWateringSessionReplyTime(Long chatId, String seconds) {
        return update(chatId, seconds, value -> wateringSessionsDao.setWateringSessionReplyTime(value, chatId));
    }

    private List<BotApiMethod<?>> update(Long chatId, String argument, IntConsumer setting) {
        OptionalInt value = Numbers.parsePositiveInt(argument);
        if (value.isEmpty()) {
            return List.of(BotMessages.text(chatId, POSITIVE_NUMBER_EXPECTED_MSG));
        }

        setting.accept(value.getAsInt());
        return List.of(BotMessages.text(chatId, DONE_MSG));
    }
}
