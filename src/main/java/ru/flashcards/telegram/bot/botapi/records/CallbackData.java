package ru.flashcards.telegram.bot.botapi.records;

import com.fasterxml.jackson.annotation.JsonProperty;
import ru.flashcards.telegram.bot.botapi.BotKeyboardButton;

public record CallbackData (
    @JsonProperty("c")
    BotKeyboardButton command,
    @JsonProperty("id")
    Long entityId,
    @JsonProperty("code")
    String entityCode,
    @JsonProperty("sw")
    SwiperParams swiper) {

    public CallbackData (BotKeyboardButton command, Long entityId, SwiperParams swiper){
        this(command, entityId, "", swiper);
    }

    public CallbackData (BotKeyboardButton command, String entityCode){
        this(command, null, entityCode, new SwiperParams("",""));
    }
}