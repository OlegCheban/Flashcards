package ru.flashcards.telegram.bot.db.dto;

import java.time.LocalDateTime;

public record UserFlashcardSpacedRepetitionNotification (
    Long userFlashcardId,
    String word,
    String description,
    Long userId,
    LocalDateTime notificationDate,
    String transcription,
    int prc){}