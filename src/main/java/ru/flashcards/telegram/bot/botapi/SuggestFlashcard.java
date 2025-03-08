package ru.flashcards.telegram.bot.botapi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Component;
import ru.flashcards.telegram.bot.botapi.records.CallbackData;
import ru.flashcards.telegram.bot.botapi.records.SwiperParams;
import ru.flashcards.telegram.bot.db.dmlOps.DataLayerObject;
import ru.flashcards.telegram.bot.db.dmlOps.FlashcardsDao;
import ru.flashcards.telegram.bot.db.dmlOps.UserProfileFlashcardsDao;
import ru.flashcards.telegram.bot.db.dmlOps.dto.SendToLearnFlashcard;
import ru.flashcards.telegram.bot.service.SendService;

import java.util.*;
import java.util.stream.Collectors;

import static ru.flashcards.telegram.bot.botapi.BotKeyboardButton.*;

@Component
@AllArgsConstructor
public class SuggestFlashcard {
    private DataLayerObject dataLayer;
    private SendService sendService;
    private FlashcardsDao flashcardsDao;

    public void byParam(Long chatId, String param){
        List<SendToLearnFlashcard> sendToLearnFlashcards = dataLayer.getFlashcardsByWordToSuggestLearning(chatId, param);
        sendToLearnFlashcards.forEach((queue) -> {
            try {
                sendService.sendMessage(queue.userId(),
                        "*" + queue.word() + "* /" + queue.transcription() + "/\n" + queue.description() + "\n\n*Перевод:* " + queue.translation() + "\n" +
                                flashcardsDao.getExamplesByFlashcardId(queue.flashcardId()).stream().map(Objects::toString).collect(Collectors.joining("\n", "*Примеры:*\n", "")),
                        String.valueOf(prepareLearnButtonsInlineKeyboardJson(queue.flashcardId(), ADD, EXCL))
                );

            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        });

        if (sendToLearnFlashcards.isEmpty()){
            sendService.sendMessage(chatId, "Карточка *" +param + "* не найдена");
        }
    }

    public void byTop3000Category(Long chatId){
        List<SendToLearnFlashcard> sendToLearnFlashcards = flashcardsDao.getFlashcardsByCategoryToSuggestLearning(chatId, 713L);
        sendToLearnFlashcards.forEach((queue) -> {
            try {
                sendService.sendMessage(queue.userId(),
                        "*" + queue.word() + "* /" + queue.transcription() + "/\n" + queue.description() + "\n\n*Перевод:* " + queue.translation() + "\n" +
                                flashcardsDao.getExamplesByFlashcardId(queue.flashcardId()).stream().map(Objects::toString).collect(Collectors.joining("\n","*Примеры:*\n", "")),
                        String.valueOf(prepareLearnButtonsInlineKeyboardJson(queue.flashcardId(), ADD_NEXT, EXCLN))
                );

            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        });
    }

    private JSONObject prepareLearnButtonsInlineKeyboardJson(Long flashcardId, BotKeyboardButton addToLearnCommand, BotKeyboardButton excludeCommand) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        CallbackData addToLearn = new CallbackData(addToLearnCommand, flashcardId, new SwiperParams("", ""));
        CallbackData exclude = new CallbackData(excludeCommand, flashcardId, new SwiperParams("", ""));
        CallbackData quite = new CallbackData(QUITE, flashcardId, new SwiperParams("", ""));

        JSONObject addToLearnInlineKeyboardButtonJson = new JSONObject();
        addToLearnInlineKeyboardButtonJson.put("text","Добавить для изучения");
        addToLearnInlineKeyboardButtonJson.put("callback_data", objectMapper.writeValueAsString(addToLearn));

        JSONObject excludeInlineKeyboardButtonJson = new JSONObject();
        excludeInlineKeyboardButtonJson.put("text","Знаю");
        excludeInlineKeyboardButtonJson.put("callback_data", objectMapper.writeValueAsString(exclude));

        JSONObject quiteInlineKeyboardButtonJson = new JSONObject();
        quiteInlineKeyboardButtonJson.put("text","Выйти");
        quiteInlineKeyboardButtonJson.put("callback_data", objectMapper.writeValueAsString(quite));

        JSONArray inlineKeyboardButtonArrayJson0 = new JSONArray();
        JSONArray inlineKeyboardButtonArrayJson1 = new JSONArray();
        JSONArray inlineKeyboardButtonArrayJson2 = new JSONArray();
        inlineKeyboardButtonArrayJson0.put(addToLearnInlineKeyboardButtonJson);
        inlineKeyboardButtonArrayJson1.put(excludeInlineKeyboardButtonJson);
        inlineKeyboardButtonArrayJson2.put(quiteInlineKeyboardButtonJson);

        JSONArray inlineKeyboardArrays = new JSONArray();
        inlineKeyboardArrays.put(inlineKeyboardButtonArrayJson0);
        inlineKeyboardArrays.put(inlineKeyboardButtonArrayJson1);
        inlineKeyboardArrays.put(inlineKeyboardButtonArrayJson2);
        JSONObject mainObj = new JSONObject();
        mainObj.put("inline_keyboard", inlineKeyboardArrays);

        return mainObj;
    }
}
