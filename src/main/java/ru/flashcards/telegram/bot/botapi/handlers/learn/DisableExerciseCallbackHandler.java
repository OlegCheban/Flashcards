package ru.flashcards.telegram.bot.botapi.handlers.learn;

import static ru.flashcards.telegram.bot.botapi.BotReplyMsg.DONE_MSG;

import java.util.List;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.MaybeInaccessibleMessage;

import lombok.RequiredArgsConstructor;

import ru.flashcards.telegram.bot.botapi.BotMessages;
import ru.flashcards.telegram.bot.botapi.CallbackHandler;
import ru.flashcards.telegram.bot.botapi.records.CallbackData;
import ru.flashcards.telegram.bot.db.LearningExercisesDao;
import ru.flashcards.telegram.bot.utils.ExerciseCodeMapper;

@Component
@RequiredArgsConstructor
public class DisableExerciseCallbackHandler implements CallbackHandler {
    private final LearningExercisesDao learningExercisesDao;

    @Override
    public List<BotApiMethod<?>> handle(CallbackQuery callbackQuery, CallbackData callbackData) {
        MaybeInaccessibleMessage message = callbackQuery.getMessage();
        learningExercisesDao.disableExercise(
                message.getChatId(), ExerciseCodeMapper.getOriginalCode(callbackData.entityCode()));

        return List.of(BotMessages.editText(message, DONE_MSG));
    }
}
