package ru.flashcards.telegram.bot.botapi.preposition;

import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class UserPrepositionSettings extends ConcurrentHashMap<Long, Integer> {
    public void setCurrentPrepositionId(Long chatId, Integer id){
        put(chatId, id);
    }

    public Integer getLatestPrepositionId(Long chatId){
        return get(chatId);
    }
}
