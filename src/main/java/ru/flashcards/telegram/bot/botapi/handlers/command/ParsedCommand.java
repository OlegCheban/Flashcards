package ru.flashcards.telegram.bot.botapi.handlers.command;

import java.util.regex.Pattern;

/**
 * A command message split into the command and its argument: {@code "/f comfy"} -> {@code ("/f", "comfy")}.
 * The bot name suffix used in group chats is dropped: {@code "/start@SomeBot"} -> {@code "/start"}.
 */
public record ParsedCommand(String command, String argument) {
    private static final Pattern WHITESPACE = Pattern.compile("\\s+");

    public static ParsedCommand parse(String text) {
        String[] parts = WHITESPACE.split(text.trim(), 2);
        String command = parts[0];
        int botNameStart = command.indexOf('@');
        if (botNameStart > 0) {
            command = command.substring(0, botNameStart);
        }
        String argument = parts.length > 1 ? parts[1].trim() : "";
        return new ParsedCommand(command, argument);
    }
}
