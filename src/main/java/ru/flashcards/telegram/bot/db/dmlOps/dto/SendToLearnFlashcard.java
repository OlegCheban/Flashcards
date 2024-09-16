package ru.flashcards.telegram.bot.db.dmlOps.dto;

public record SendToLearnFlashcard(
    Long userId,
    Long flashcardId,
    String description,
    String transcription,
    String translation,
    String word) {}