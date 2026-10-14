package ru.flashcards.telegram.bot.botapi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import ru.flashcards.telegram.bot.botapi.records.CallbackData;
import ru.flashcards.telegram.bot.botapi.records.SwiperParams;

class CallbackDataCodecTest {
    /**
     * Telegram's limit for callback_data.
     */
    private static final int MAX_CALLBACK_DATA_BYTES = 64;

    private final CallbackDataCodec codec = new CallbackDataCodec(new ObjectMapper());

    @Test
    void roundTrips() {
        CallbackData callbackData = new CallbackData(BotKeyboardButton.SRTL, 42L, new SwiperParams("ca", "100"));

        assertEquals(callbackData, codec.decode(codec.encode(callbackData)));
    }

    @Test
    void omitsEmptyValuesAndRestoresThemAsEmpty() {
        String json = codec.encode(new CallbackData(BotKeyboardButton.EXS, 42L));

        assertEquals("{\"c\":\"EXS\",\"id\":42,\"sw\":{}}", json);
        assertEquals(new CallbackData(BotKeyboardButton.EXS, 42L, "", SwiperParams.EMPTY), codec.decode(json));
    }

    @Test
    void decodesButtonsSentBeforeEmptyValuesWereOmitted() {
        CallbackData callbackData =
                codec.decode("{\"c\":\"PR\",\"id\":7,\"code\":\"\",\"sw\":{\"ch\":\"\",\"p\":\"\"}}");

        assertEquals(new CallbackData(BotKeyboardButton.PR, 7L, SwiperParams.EMPTY), callbackData);
    }

    @Test
    void fitsTelegramLimitWithLongestSwiperFilters() {
        String json = codec.encode(
                new CallbackData(BotKeyboardButton.SRTL, 9_999_999_999L, new SwiperParams("abcdefghij", "100")));

        assertTrue(json.getBytes(StandardCharsets.UTF_8).length <= MAX_CALLBACK_DATA_BYTES, json);
    }
}
