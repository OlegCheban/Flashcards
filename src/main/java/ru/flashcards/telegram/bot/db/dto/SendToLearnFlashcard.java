package ru.flashcards.telegram.bot.db.dto;

public record SendToLearnFlashcard(
    Long userId,
    Long flashcardId,
    String description,
    String transcription,
    String translation,
    String word) {}