package ru.flashcards.telegram.bot.db.dto;

public record SwiperFlashcard(
    Long prevId,
    Long nextId,
    Long currentId,
    String word,
    String description,
    String translation,
    String transcription,
    int learnPrc,
    int nearestTraining){}