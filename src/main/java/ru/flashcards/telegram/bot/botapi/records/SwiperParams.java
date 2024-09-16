package ru.flashcards.telegram.bot.botapi.records;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SwiperParams (
    @JsonProperty("ch")
    String charCond,
    @JsonProperty("p")
    String prc) {}
