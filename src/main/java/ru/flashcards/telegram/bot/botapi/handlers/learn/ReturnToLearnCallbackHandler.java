package ru.flashcards.telegram.bot.botapi.handlers.learn;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import ru.flashcards.telegram.bot.botapi.records.CallbackData;
import ru.flashcards.telegram.bot.botapi.MessageHandler;
import ru.flashcards.telegram.bot.db.LearningExercisesDao;
import ru.flashcards.telegram.bot.db.NotificationsDao;
import ru.flashcards.telegram.bot.db.UserProfileFlashcardsDao;
import ru.flashcards.telegram.bot.db.dto.UserFlashcard;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.toIntExact;

@Component
@AllArgsConstructor
public class ReturnToLearnCallbackHandler implements MessageHandler<CallbackQuery> {
    private UserProfileFlashcardsDao userProfileFlashcardsDao;
    private LearningExercisesDao learningExercisesDao;
    private NotificationsDao notificationsDao;

    @Override
    public List<BotApiMethod<?>> handle(CallbackQuery callbackQuery) {
        CallbackData callbackData = jsonToCallbackData(callbackQuery.getData());
        List<BotApiMethod<?>> list = new ArrayList<>();
        var message = callbackQuery.getMessage();
        long messageId = message.getMessageId();
        long chatId = message.getChatId();
        Long userFlashcardId = callbackData.entityId();

        UserFlashcard flashcard = userProfileFlashcardsDao.findUserFlashcardById(userFlashcardId);
        notificationsDao.deleteSpacedRepetitionHistory(userFlashcardId);
        learningExercisesDao.deleteExerciseStat(userFlashcardId);
        learningExercisesDao.returnToLearn(userFlashcardId);

        EditMessageText resultMessage = new EditMessageText();
        resultMessage.setChatId(String.valueOf(chatId));
        resultMessage.setMessageId(toIntExact(messageId));
        resultMessage.enableMarkdown(true);
        resultMessage.setText("*Карточка возвращена для повторного изучения*\n*"+flashcard.word()+"* /" + flashcard.transcription() + "/\n\n"+flashcard.description() + "\n\n"+flashcard.translation());

        list.add(resultMessage);
        return list;
    }
}