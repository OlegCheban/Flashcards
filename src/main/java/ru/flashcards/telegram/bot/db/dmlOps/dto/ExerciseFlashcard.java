package ru.flashcards.telegram.bot.db.dmlOps.dto;

import ru.flashcards.telegram.bot.botapi.ExerciseKinds;

public record ExerciseFlashcard(
        Long chatId,
        String word,
        ExerciseKinds exerciseKindsCode,
        String description,
        String transcription,
        Long userFlashcardId,
        String translation,
        String example) {}