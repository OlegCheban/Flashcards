package ru.flashcards.telegram.bot.botapi.preposition;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserPrepositionMistakesTest {

    @Test
    void countsMistakesByChatAndSentence() {
        UserPrepositionMistakes mistakes = new UserPrepositionMistakes();

        mistakes.putNewMistake(1L, "look *at* me");
        mistakes.putNewMistake(1L, "look *at* me");
        mistakes.putNewMistake(1L, "think *about* it");
        mistakes.putNewMistake(2L, "look *at* me");

        assertEquals(2, mistakes.getMistakes(1L).get("look *at* me"));
        assertEquals(1, mistakes.getMistakes(1L).get("think *about* it"));
        assertEquals(1, mistakes.getMistakes(2L).get("look *at* me"));
    }

    @Test
    void returnsEmptyMapWhenChatHasNoMistakes() {
        UserPrepositionMistakes mistakes = new UserPrepositionMistakes();

        assertTrue(mistakes.getMistakes(1L).isEmpty());
    }

    @Test
    void clearsMistakesForChat() {
        UserPrepositionMistakes mistakes = new UserPrepositionMistakes();

        mistakes.putNewMistake(1L, "look *at* me");
        mistakes.clearMistakes(1L);

        assertTrue(mistakes.getMistakes(1L).isEmpty());
    }
}
