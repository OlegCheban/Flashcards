package ru.flashcards.telegram.bot.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.OptionalInt;

import org.junit.jupiter.api.Test;

class NumbersTest {

    @Test
    void parsesPositiveInteger() {
        assertEquals(OptionalInt.of(120), Numbers.parsePositiveInt("120"));
    }

    @Test
    void rejectsZeroNegativeOverflowAndText() {
        assertTrue(Numbers.parsePositiveInt("0").isEmpty());
        assertTrue(Numbers.parsePositiveInt("-5").isEmpty());
        assertTrue(Numbers.parsePositiveInt("99999999999").isEmpty());
        assertTrue(Numbers.parsePositiveInt("12a").isEmpty());
        assertTrue(Numbers.parsePositiveInt("").isEmpty());
    }

    @Test
    void acceptsOnlyAsciiDigits() {
        assertTrue(Numbers.isDigits("100"));
        assertFalse(Numbers.isDigits("١٠٠"));
        assertFalse(Numbers.isDigits(null));
    }
}
