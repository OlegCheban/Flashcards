package ru.flashcards.telegram.bot.botapi.preposition;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class UserPrepositionMistakes extends ConcurrentHashMap<Long, Map<String, Integer>> {
    public void putNewMistake(Long chatId, String sentence){
        var userMistakes = get(chatId);

        if (userMistakes == null){
            userMistakes = new HashMap<>();
        }

        var qty = userMistakes.get(sentence);
        userMistakes.put(sentence, qty != null ? qty + 1 : 1);

        put(chatId, userMistakes);
    }
}
