package ru.flashcards.telegram.bot.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * Short aliases of exercise kind codes: the codes are put into callback data, which Telegram limits to 64 bytes.
 */
public final class ExerciseCodeMapper {
    private static final Map<String, String> ORIGINAL_TO_MAPPED = Map.of(
            "CHECK_SPELLING", "CHECK1",
            "COMPLETE_THE_GAPS", "CHECK2",
            "CHECK_TRANSLATION", "CHECK3",
            "CHECK_DESCRIPTION", "CHECK4",
            "CHECK_SPELLING_WITH_HELPS", "CHECK5",
            "MEMORISED", "MEM1");
    private static final Map<String, String> MAPPED_TO_ORIGINAL = invert(ORIGINAL_TO_MAPPED);

    private ExerciseCodeMapper() {
    }

    public static String getMappedCode(String originalCode) {
        return ORIGINAL_TO_MAPPED.getOrDefault(originalCode, originalCode);
    }

    public static String getOriginalCode(String mappedCode) {
        return MAPPED_TO_ORIGINAL.getOrDefault(mappedCode, mappedCode);
    }

    private static Map<String, String> invert(Map<String, String> map) {
        Map<String, String> inverted = new HashMap<>();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            inverted.put(entry.getValue(), entry.getKey());
        }
        return Map.copyOf(inverted);
    }
}
