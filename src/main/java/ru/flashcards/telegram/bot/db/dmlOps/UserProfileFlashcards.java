package ru.flashcards.telegram.bot.db.dmlOps;

import java.util.List;

public interface UserProfileFlashcards {
    List<String> findUnlearnedFlashcardKeyword(Long chatId, Integer flashcardQuantity);
}
