package ru.flashcards.telegram.bot.db.dto;

/**
 * @param prevId previous flashcard of the Swiper, {@code null} for the first one
 * @param nextId next flashcard of the Swiper, {@code null} for the last one
 */
public record SwiperFlashcard(
    Long prevId,
    Long nextId,
    Long currentId,
    String word,
    String description,
    String translation,
    String transcription,
    int learnPrc,
    int nearestTraining) {

    public static boolean exists(Long flashcardId) {
        return flashcardId != null && flashcardId != 0;
    }
}
