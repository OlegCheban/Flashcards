package ru.flashcards.telegram.bot.botapi.handlers.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import ru.flashcards.telegram.bot.botapi.BotCommand;

class ParsedCommandTest {

    @Test
    void splitsCommandAndArgument() {
        ParsedCommand parsedCommand = ParsedCommand.parse("  /s   ca 100 ");

        assertEquals("/s", parsedCommand.command());
        assertEquals("ca 100", parsedCommand.argument());
    }

    @Test
    void returnsEmptyArgumentForCommandWithoutArgument() {
        assertEquals(new ParsedCommand("/f", ""), ParsedCommand.parse("/f"));
    }

    @Test
    void dropsBotNameSuffix() {
        assertEquals(new ParsedCommand("/start", ""), ParsedCommand.parse("/start@proFlashcardsBot"));
    }

    @Test
    void matchesCommandsExactlyRegardlessOfDeclarationOrder() {
        assertEquals(Optional.of(BotCommand.START), command("/start"));
        assertEquals(Optional.of(BotCommand.OPEN_SWIPER), command("/s ca"));
        assertEquals(Optional.of(BotCommand.TRAINING_FLASHCARDS_QUANTITY_SETTINGS), command("/fq 10"));
        assertEquals(Optional.of(BotCommand.FIND_FLASHCARD), command("/f comfy"));
        assertEquals(Optional.of(BotCommand.CHANGE_TRANSLATION), command("/ed word"));
        assertEquals(Optional.of(BotCommand.WATERING_SESSION_REPLY_TIME_SETTINGS), command("/wt 3"));
        assertTrue(command("/sca").isEmpty());
    }

    private Optional<BotCommand> command(String text) {
        return BotCommand.fromCommand(ParsedCommand.parse(text).command());
    }
}
