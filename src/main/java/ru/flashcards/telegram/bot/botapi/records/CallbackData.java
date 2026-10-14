package ru.flashcards.telegram.bot.botapi.records;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import ru.flashcards.telegram.bot.botapi.BotKeyboardButton;

/**
 * Payload of an inline button. It is stored in Telegram's callback_data, which is limited to 64 bytes,
 * so empty values are omitted from JSON and restored as empty values on deserialization.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record CallbackData(
        @JsonProperty("c")
        BotKeyboardButton command,
        @JsonProperty("id")
        Long entityId,
        @JsonProperty("code")
        String entityCode,
        @JsonProperty("sw")
        SwiperParams swiper) {

    public CallbackData {
        entityCode = entityCode == null ? "" : entityCode;
        swiper = swiper == null ? SwiperParams.EMPTY : swiper;
    }

    public CallbackData(BotKeyboardButton command, Long entityId, SwiperParams swiper) {
        this(command, entityId, "", swiper);
    }

    public CallbackData(BotKeyboardButton command, Long entityId) {
        this(command, entityId, "", SwiperParams.EMPTY);
    }

    public CallbackData(BotKeyboardButton command, String entityCode) {
        this(command, null, entityCode, SwiperParams.EMPTY);
    }
}
