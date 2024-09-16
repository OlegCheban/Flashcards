package ru.flashcards.telegram.bot.db.dmlOps.dto;

public record Flashcard(
    Long categoryId,
    String description,
    String transcription,
    String translation,
    String word) {}