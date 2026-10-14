package ru.flashcards.telegram.bot.botapi.handlers.command;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;

import lombok.RequiredArgsConstructor;

import ru.flashcards.telegram.bot.botapi.BotMessages;
import ru.flashcards.telegram.bot.botapi.UserMode;
import ru.flashcards.telegram.bot.botapi.UserModeSettings;
import ru.flashcards.telegram.bot.botapi.handlers.learn.exercises.common.ExerciseProvider;
import ru.flashcards.telegram.bot.botapi.phrasalverb.PhrasalVerbTrainingMode;
import ru.flashcards.telegram.bot.botapi.preposition.PrepositionLearningMode;
import ru.flashcards.telegram.bot.botapi.wateringSession.WateringSessionQuestion;
import ru.flashcards.telegram.bot.db.LearningExercisesDao;
import ru.flashcards.telegram.bot.db.WateringSessionsDao;
import ru.flashcards.telegram.bot.db.dto.ExerciseFlashcard;
import ru.flashcards.telegram.bot.db.dto.UserFlashcard;

/**
 * Commands /l, /w, /pr and /pv that switch the user into a learning mode.
 */
@Component
@RequiredArgsConstructor
public class LearningModeCommands {
    private final LearningExercisesDao learningExercisesDao;
    private final WateringSessionsDao wateringSessionsDao;
    private final UserModeSettings userModeSettings;
    private final ExerciseProvider exerciseProvider;
    private final WateringSessionQuestion wateringSessionQuestion;
    private final PrepositionLearningMode prepositionLearningMode;
    private final PhrasalVerbTrainingMode phrasalVerbTrainingMode;

    public List<BotApiMethod<?>> startLearning(Long chatId) {
        Optional<ExerciseFlashcard> exercise = learningExercisesDao.findCurrentExerciseCard(chatId);
        if (exercise.isPresent()) {
            userModeSettings.setMode(chatId, UserMode.EXERCISE);
            return List.of(exerciseProvider.newExercise(exercise.get()));
        }

        if (learningExercisesDao.existsExercise(chatId)) {
            return List.of(BotMessages.text(chatId, "Все упражнения отключены. Включите их командой /e"));
        }
        return List.of(BotMessages.text(chatId, "Нет доступных карточек для изучения. Добавьте карточки."));
    }

    public List<BotApiMethod<?>> startWateringSession(Long chatId) {
        Optional<UserFlashcard> flashcard = wateringSessionsDao.getUserFlashcardForWateringSession(chatId);
        if (flashcard.isEmpty()) {
            return List.of(BotMessages.text(chatId, "Нет выученных карточек"));
        }

        userModeSettings.setMode(chatId, UserMode.WATERING_SESSION);
        return List.of(wateringSessionQuestion.newQuestion(chatId, flashcard.get()));
    }

    /**
     * @param topic the exercise topic, empty for all topics
     */
    public List<BotApiMethod<?>> startPrepositionMode(Long chatId, String topic) {
        return prepositionLearningMode.start(chatId, topic);
    }

    public List<BotApiMethod<?>> startPhrasalVerbMode(Long chatId) {
        return phrasalVerbTrainingMode.start(chatId);
    }
}
