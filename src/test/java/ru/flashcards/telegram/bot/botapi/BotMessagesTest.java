package ru.flashcards.telegram.bot.botapi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

class BotMessagesTest {

    @Test
    void escapesMarkdownSpecialCharacters() {
        assertEquals("a\\_b\\*c\\`d\\[e]", BotMessages.escapeMarkdown("a_b*c`d[e]"));
    }

    @Test
    void keepsShortTextInOnePart() {
        assertEquals(List.of("short"), BotMessages.splitText("short"));
    }

    @Test
    void splitsLongTextAtLineBreaks() {
        String line = "x".repeat(3000);
        List<String> parts = BotMessages.splitText(line + "\n" + line);

        assertEquals(List.of(line, line), parts);
    }

    @Test
    void splitsLongTextWithoutLineBreaks() {
        List<String> parts = BotMessages.splitText("x".repeat(BotMessages.MAX_TEXT_LENGTH * 2 + 1));

        assertEquals(3, parts.size());
        for (String part : parts) {
            assertTrue(part.length() <= BotMessages.MAX_TEXT_LENGTH);
        }
    }
}
