package ru.flashcards.telegram.bot.botapi;

import java.util.Optional;

public enum BotCommand {
    START("/start"),
    START_LEARNING("/l"),
    START_WATERING_SESSION("/w"),
    ENABLE_EXERCISE("/e"),
    DISABLE_EXERCISE("/d"),
    OPEN_SWIPER("/s"),
    FIND_FLASHCARD("/f"),
    NOTIFICATION_INTERVAL_SETTINGS("/ni"),
    TRAINING_FLASHCARDS_QUANTITY_SETTINGS("/fq"),
    WATERING_SESSION_REPLY_TIME_SETTINGS("/wt"),
    CHANGE_TRANSLATION("/ed"),
    HELP("/h"),
    RECENT_LEARNED("/rl"),
    PREPOSITION("/pr"),
    PHRASAL_VERB("/pv"),
    STOP_LEARNING("Выйти");

    private final String command;

    BotCommand(String command) {
        this.command = command;
    }

    public String command() {
        return command;
    }

    public static Optional<BotCommand> fromCommand(String command) {
        for (BotCommand botCommand : values()) {
            if (botCommand.command.equals(command)) {
                return Optional.of(botCommand);
            }
        }
        return Optional.empty();
    }
}
