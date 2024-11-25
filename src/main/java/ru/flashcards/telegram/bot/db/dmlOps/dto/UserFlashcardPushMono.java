package ru.flashcards.telegram.bot.db.dmlOps.dto;

import java.time.LocalDateTime;

public record UserFlashcardPushMono(
    Long userFlashcardId,
    String word,
    String description,
    Long userId,
    String transcription){}