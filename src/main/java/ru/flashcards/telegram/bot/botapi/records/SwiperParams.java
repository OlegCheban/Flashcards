package ru.flashcards.telegram.bot.botapi.records;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record SwiperParams(
        @JsonProperty("ch")
        String charCond,
        @JsonProperty("p")
        String prc) {

    public static final SwiperParams EMPTY = new SwiperParams("", "");

    public SwiperParams {
        charCond = charCond == null ? "" : charCond;
        prc = prc == null ? "" : prc;
    }
}
