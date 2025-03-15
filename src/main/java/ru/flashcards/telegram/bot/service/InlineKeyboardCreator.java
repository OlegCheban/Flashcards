package ru.flashcards.telegram.bot.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONObject;
import ru.flashcards.telegram.bot.botapi.BotKeyboardButton;
import ru.flashcards.telegram.bot.botapi.records.CallbackData;
import ru.flashcards.telegram.bot.botapi.records.SwiperParams;

import java.util.List;

public class InlineKeyboardCreator {
    public static JSONObject prepareButton(Long payload, String label, BotKeyboardButton command){
        CallbackData callbackData = new CallbackData(command, payload, new SwiperParams("", ""));
        JSONObject inlineKeyboardButtonJson = new JSONObject();
        inlineKeyboardButtonJson.put("text", label);
        var objectMapper = new ObjectMapper();
        try {
            inlineKeyboardButtonJson.put("callback_data", objectMapper.writeValueAsString(callbackData));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return inlineKeyboardButtonJson;
    }

    public static JSONObject createButtonMenu(List<JSONObject> list){
        JSONObject mainObj = new JSONObject();
        JSONArray inlineKeyboardButtonArrayJson = new JSONArray();
        list.forEach(button -> inlineKeyboardButtonArrayJson.put(button));
        JSONArray inlineKeyboardArrays = new JSONArray();
        inlineKeyboardArrays.put(inlineKeyboardButtonArrayJson);
        mainObj.put("inline_keyboard", inlineKeyboardArrays);

        return mainObj;
    }
}
