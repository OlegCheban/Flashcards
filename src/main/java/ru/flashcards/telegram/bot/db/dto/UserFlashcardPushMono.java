package ru.flashcards.telegram.bot.db.dto;

public record UserFlashcardPushMono(
    Long userFlashcardId,
    String word,
    String description,
    Long userId,
    String transcription){}