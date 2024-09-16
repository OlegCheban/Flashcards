package ru.flashcards.telegram.bot.db.dmlOps.dto;

public record UserFlashcard(
    Long id,
    String description,
    String transcription,
    String translation,
    String word){}