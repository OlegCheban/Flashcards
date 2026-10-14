package ru.flashcards.telegram.bot.botapi.handlers.command;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;

import lombok.RequiredArgsConstructor;

import ru.flashcards.telegram.bot.botapi.BotCommand;
import ru.flashcards.telegram.bot.botapi.BotMessages;
import ru.flashcards.telegram.bot.botapi.MessageHandler;
import ru.flashcards.telegram.bot.db.UsersDao;
import ru.flashcards.telegram.bot.utils.Help;

/**
 * Routes a command to its handler. Unknown commands and plain text are ignored.
 */
@Component
@RequiredArgsConstructor
public class CommandMessageHandler implements MessageHandler {
    private final UsersDao usersDao;
    private final FlashcardCommands flashcardCommands;
    private final LearningModeCommands learningModeCommands;
    private final ExerciseKindsCommands exerciseKindsCommands;
    private final SettingsCommands settingsCommands;
    private final SwiperCommand swiperCommand;

    @Override
    public List<BotApiMethod<?>> handle(Message message) {
        ParsedCommand parsedCommand = ParsedCommand.parse(message.getText());
        Optional<BotCommand> command = BotCommand.fromCommand(parsedCommand.command());
        if (command.isEmpty()) {
            return List.of();
        }

        Long chatId = message.getChatId();
        String argument = parsedCommand.argument();
        return switch (command.get()) {
            case START -> start(chatId, message.getChat().getUserName());
            case HELP -> List.of(manual(chatId));
            case FIND_FLASHCARD -> flashcardCommands.find(chatId, argument);
            case CHANGE_TRANSLATION -> flashcardCommands.changeTranslation(chatId, argument);
            case RECENT_LEARNED -> flashcardCommands.recentLearned(chatId, argument);
            case START_LEARNING -> learningModeCommands.startLearning(chatId);
            case START_WATERING_SESSION -> learningModeCommands.startWateringSession(chatId);
            case PREPOSITION -> learningModeCommands.startPrepositionMode(chatId, argument);
            case PHRASAL_VERB -> learningModeCommands.startPhrasalVerbMode(chatId);
            case ENABLE_EXERCISE -> exerciseKindsCommands.offerToEnable(chatId);
            case DISABLE_EXERCISE -> exerciseKindsCommands.offerToDisable(chatId);
            case NOTIFICATION_INTERVAL_SETTINGS -> settingsCommands.setNotificationInterval(chatId, argument);
            case TRAINING_FLASHCARDS_QUANTITY_SETTINGS ->
                    settingsCommands.setTrainingFlashcardsQuantity(chatId, argument);
            case WATERING_SESSION_REPLY_TIME_SETTINGS -> settingsCommands.setWateringSessionReplyTime(chatId, argument);
            case OPEN_SWIPER -> swiperCommand.open(chatId, argument);
            case STOP_LEARNING -> List.of();
        };
    }

    private List<BotApiMethod<?>> start(Long chatId, String username) {
        usersDao.registerUser(chatId, username);
        return List.of(manual(chatId));
    }

    private BotApiMethod<?> manual(Long chatId) {
        return BotMessages.markdown(chatId, Help.manual());
    }
}
