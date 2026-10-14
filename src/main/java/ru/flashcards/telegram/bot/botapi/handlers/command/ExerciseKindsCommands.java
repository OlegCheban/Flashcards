package ru.flashcards.telegram.bot.botapi.handlers.command;

import static ru.flashcards.telegram.bot.botapi.BotKeyboardButton.DISABLE;
import static ru.flashcards.telegram.bot.botapi.BotKeyboardButton.ENABLE;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import lombok.RequiredArgsConstructor;

import ru.flashcards.telegram.bot.botapi.BotKeyboardButton;
import ru.flashcards.telegram.bot.botapi.BotMessages;
import ru.flashcards.telegram.bot.botapi.keyboard.InlineKeyboardFactory;
import ru.flashcards.telegram.bot.botapi.records.CallbackData;
import ru.flashcards.telegram.bot.db.LearningExercisesDao;
import ru.flashcards.telegram.bot.db.dto.ExerciseKind;
import ru.flashcards.telegram.bot.utils.ExerciseCodeMapper;

/**
 * Commands /e and /d: offer the exercise kinds to enable or disable.
 */
@Component
@RequiredArgsConstructor
public class ExerciseKindsCommands {
    private final LearningExercisesDao learningExercisesDao;
    private final InlineKeyboardFactory inlineKeyboardFactory;

    public List<BotApiMethod<?>> offerToEnable(Long chatId) {
        return List.of(offer(chatId, learningExercisesDao.getExerciseKindToEnable(chatId), ENABLE,
                "Выберите упражнение которое необходимо включить:", "Все упражнения добавлены"));
    }

    public List<BotApiMethod<?>> offerToDisable(Long chatId) {
        return List.of(offer(chatId, learningExercisesDao.getExerciseKindToDisable(chatId), DISABLE,
                "Выберите упражнение которое необходимо исключить: ", "Все упражнения исключены"));
    }

    private SendMessage offer(
            Long chatId,
            List<ExerciseKind> exerciseKinds,
            BotKeyboardButton command,
            String offerText,
            String emptyText) {
        if (exerciseKinds.isEmpty()) {
            return BotMessages.text(chatId, emptyText);
        }

        List<InlineKeyboardButton> buttons = new ArrayList<>();
        for (ExerciseKind exerciseKind : exerciseKinds) {
            CallbackData callbackData =
                    new CallbackData(command, ExerciseCodeMapper.getMappedCode(exerciseKind.code()));
            buttons.add(inlineKeyboardFactory.button(exerciseKind.name(), callbackData));
        }

        SendMessage message = BotMessages.text(chatId, offerText);
        message.setReplyMarkup(inlineKeyboardFactory.column(buttons));
        return message;
    }
}
