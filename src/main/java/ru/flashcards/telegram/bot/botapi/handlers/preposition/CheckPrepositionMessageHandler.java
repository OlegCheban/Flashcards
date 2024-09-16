package ru.flashcards.telegram.bot.botapi.handlers.preposition;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.flashcards.telegram.bot.botapi.MessageHandler;
import ru.flashcards.telegram.bot.botapi.preposition.Preposition;
import ru.flashcards.telegram.bot.botapi.preposition.PrepositionLearningMode;
import ru.flashcards.telegram.bot.botapi.preposition.UserPrepositionMistakes;
import ru.flashcards.telegram.bot.botapi.preposition.UserPrepositionSettings;
import ru.flashcards.telegram.bot.utils.RandomMessageText;

import java.util.ArrayList;
import java.util.List;

@Component
public class CheckPrepositionMessageHandler implements MessageHandler<Message> {
    private UserPrepositionSettings userPrepositionSettings;
    private Preposition preposition;
    private PrepositionLearningMode prepositionLearningMode;
    private UserPrepositionMistakes userPrepositionMistakes;

    public CheckPrepositionMessageHandler(UserPrepositionSettings userPrepositionSettings,
                                          Preposition preposition,
                                          PrepositionLearningMode prepositionLearningMode,
                                          UserPrepositionMistakes userPrepositionMistakes) {
        this.userPrepositionSettings = userPrepositionSettings;
        this.preposition = preposition;
        this.prepositionLearningMode = prepositionLearningMode;
        this.userPrepositionMistakes = userPrepositionMistakes;
    }

    @Override
    public List<BotApiMethod<?>> handle(Message message){
        List<BotApiMethod<?>> list = new ArrayList<>();
        var chatId = message.getChatId();
        var latestId = userPrepositionSettings.getLatestPrepositionId(chatId);
        var current = preposition.getPrepositions().get(latestId == null ? 1 : latestId);
        var question = current.get(0);
        var correctAnswer = current.get(1);
        list.add(createResultMessage(chatId, message.getText().trim().equalsIgnoreCase(correctAnswer), correctAnswer, question));

        var naxtId = latestId == null ? 1 : latestId + 1;
        if (preposition.getPrepositions().get(naxtId) == null){
            naxtId = 1;
        }

        userPrepositionSettings.setCurrentPrepositionId(chatId, naxtId);
        list.add(prepositionLearningMode.newPreposition(chatId));

        return list;
    }

    private BotApiMethod<?> createResultMessage(Long chatId, Boolean isCorrectAnswer, String correctAnswer, String question){
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.enableMarkdown(true);
        if (!isCorrectAnswer) {
            userPrepositionMistakes.putNewMistake(chatId, question.replace("...", String.format("*%s*", correctAnswer)));
        }

        sendMessage.setText(isCorrectAnswer ?
                RandomMessageText.getPositiveMessage() :
                String.format("%s. Correct answer is *%s*", RandomMessageText.getNegativeMessage(), correctAnswer)
        );

        return sendMessage;
    }
}
