package ru.flashcards.telegram.bot.db.dto;

public record UserFlashcard(
    Long id,
    String description,
    String transcription,
    String translation,
    String word){}