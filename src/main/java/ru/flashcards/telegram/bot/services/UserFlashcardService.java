package ru.flashcards.telegram.bot.services;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import ru.flashcards.telegram.bot.db.LearningExercisesDao;
import ru.flashcards.telegram.bot.db.NotificationsDao;
import ru.flashcards.telegram.bot.db.UserProfileFlashcardsDao;
import ru.flashcards.telegram.bot.db.dto.UserFlashcard;

/**
 * Multi-step changes of a user's flashcard. Each operation verifies that the flashcard belongs to the user
 * and runs in a single transaction, so a failure cannot leave the statistics half-deleted.
 */
@Service
@RequiredArgsConstructor
public class UserFlashcardService {
    private final UserProfileFlashcardsDao userProfileFlashcardsDao;
    private final LearningExercisesDao learningExercisesDao;
    private final NotificationsDao notificationsDao;

    /**
     * Resets the learning progress so that the flashcard is learned again.
     *
     * @return the flashcard, empty if the user has no such flashcard
     */
    @Transactional
    public Optional<UserFlashcard> returnToLearn(Long chatId, Long userFlashcardId) {
        Optional<UserFlashcard> flashcard = userProfileFlashcardsDao.findUserFlashcardById(chatId, userFlashcardId);
        if (flashcard.isPresent()) {
            deleteProgress(userFlashcardId);
            learningExercisesDao.returnToLearn(userFlashcardId);
        }
        return flashcard;
    }

    /**
     * @return {@code false} if the user has no such flashcard
     */
    @Transactional
    public boolean remove(Long chatId, Long userFlashcardId) {
        if (userProfileFlashcardsDao.findUserFlashcardById(chatId, userFlashcardId).isEmpty()) {
            return false;
        }
        deleteProgress(userFlashcardId);
        userProfileFlashcardsDao.removeUserFlashcard(chatId, userFlashcardId);
        return true;
    }

    private void deleteProgress(Long userFlashcardId) {
        notificationsDao.deleteSpacedRepetitionHistory(userFlashcardId);
        learningExercisesDao.deleteExerciseStat(userFlashcardId);
    }
}
