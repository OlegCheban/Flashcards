package ru.flashcards.telegram.bot.utils;

import java.util.HashMap;
import java.util.Map;

public class ExerciseCodeMapper {
    private static final Map<String, String> originalToMapped = new HashMap<>();
    private static final Map<String, String> mappedToOriginal = new HashMap<>();

    static {
        originalToMapped.put("CHECK_SPELLING", "CHECK1");
        originalToMapped.put("COMPLETE_THE_GAPS", "CHECK2");
        originalToMapped.put("CHECK_TRANSLATION", "CHECK3");
        originalToMapped.put("CHECK_DESCRIPTION", "CHECK4");
        originalToMapped.put("CHECK_SPELLING_WITH_HELPS", "CHECK5");
        originalToMapped.put("MEMORISED", "MEM1");

        for (Map.Entry<String, String> entry : originalToMapped.entrySet()) {
            mappedToOriginal.put(entry.getValue(), entry.getKey());
        }
    }

    public static String getMappedCode(String originalCode) {
        return originalToMapped.getOrDefault(originalCode, originalCode);
    }

    public static String getOriginalCode(String mappedCode) {
        return mappedToOriginal.getOrDefault(mappedCode, mappedCode);
    }
}
