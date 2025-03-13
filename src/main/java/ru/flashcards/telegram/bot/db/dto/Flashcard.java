package ru.flashcards.telegram.bot.db.dto;

public record Flashcard(
    Long categoryId,
    String description,
    String transcription,
    String translation,
    String word) {}