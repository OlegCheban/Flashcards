package ru.flashcards.telegram.bot.botapi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Component;
import ru.flashcards.telegram.bot.botapi.records.CallbackData;
import ru.flashcards.telegram.bot.botapi.records.SwiperParams;
import ru.flashcards.telegram.bot.db.FlashcardsDao;
import ru.flashcards.telegram.bot.db.LearningExercisesDao;
import ru.flashcards.telegram.bot.db.dto.SendToLearnFlashcard;
import ru.flashcards.telegram.bot.services.SendMessageService;

import java.util.*;
import java.util.stream.Collectors;

import static ru.flashcards.telegram.bot.botapi.BotKeyboardButton.*;

@Component
@AllArgsConstructor
public class SuggestFlashcard {
    private SendMessageService sendMessageService;
    private FlashcardsDao flashcardsDao;
    private LearningExercisesDao learningExercisesDao;

    public void byParam(Long chatId, String param){
        List<SendToLearnFlashcard> sendToLearnFlashcards = learningExercisesDao.getFlashcardsByWordToSuggestLearning(chatId, param);
        sendToLearnFlashcards.forEach((queue) -> {
            try {
                sendMessageService.sendMessage(queue.userId(),
                        "*" + queue.word() + "* /" + queue.transcription() + "/\n" + queue.description() + "\n\n*Перевод:* " + queue.translation() + "\n" +
                                flashcardsDao.getExamplesByFlashcardId(queue.flashcardId()).stream().map(Objects::toString).collect(Collectors.joining("\n", "*Примеры:*\n", "")),
                        String.valueOf(prepareLearnButtonsInlineKeyboardJson(queue.flashcardId(), ADD, EXCL, AIREP))
                );

            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        });

        if (sendToLearnFlashcards.isEmpty()){
            sendMessageService.sendMessage(chatId, "Карточка *" +param + "* не найдена");
        }
    }

    public void byTop3000Category(Long chatId){
        List<SendToLearnFlashcard> sendToLearnFlashcards = flashcardsDao.getFlashcardsByCategoryToSuggestLearning(chatId, 713L);
        sendToLearnFlashcards.forEach((queue) -> {
            try {
                sendMessageService.sendMessage(queue.userId(),
                        "*" + queue.word() + "* /" + queue.transcription() + "/\n" + queue.description() + "\n\n*Перевод:* " + queue.translation() + "\n" +
                                flashcardsDao.getExamplesByFlashcardId(queue.flashcardId()).stream().map(Objects::toString).collect(Collectors.joining("\n","*Примеры:*\n", "")),
                        String.valueOf(prepareLearnButtonsInlineKeyboardJson(queue.flashcardId(), ADD_NEXT, EXCLN, AIREP))
                );

            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        });
    }

    private JSONObject prepareLearnButtonsInlineKeyboardJson(
            Long flashcardId,
            BotKeyboardButton addToLearnCommand,
            BotKeyboardButton excludeCommand,
            BotKeyboardButton aiRepCommand) throws JsonProcessingException {

        ObjectMapper objectMapper = new ObjectMapper();

        // Prepare callback data for all buttons
        CallbackData addToLearnCallback = createCallbackData(addToLearnCommand, flashcardId);
        CallbackData excludeCallback = createCallbackData(excludeCommand, flashcardId);
        CallbackData aiRepCallback = createCallbackData(aiRepCommand, flashcardId);
        CallbackData quitCallback = createCallbackData(QUITE, flashcardId);

        // Create individual keyboard buttons
        JSONObject addToLearnButton = createInlineKeyboardButton("Добавить для изучения", addToLearnCallback, objectMapper);
        JSONObject learnMoreButton = createInlineKeyboardButton("Узнать больше", aiRepCallback, objectMapper);
        JSONObject knowButton = createInlineKeyboardButton("Знаю", excludeCallback, objectMapper);
        JSONObject quitButton = createInlineKeyboardButton("Выйти", quitCallback, objectMapper);

        // Organize buttons into rows (each row is a JSONArray)
        JSONArray keyboardRows = new JSONArray();
        keyboardRows.put(createButtonRow(addToLearnButton));
        keyboardRows.put(createButtonRow(learnMoreButton));
        keyboardRows.put(createButtonRow(knowButton));
        keyboardRows.put(createButtonRow(quitButton));

        // Create the final keyboard structure
        JSONObject keyboardObject = new JSONObject();
        keyboardObject.put("inline_keyboard", keyboardRows);

        return keyboardObject;
    }

    private CallbackData createCallbackData(BotKeyboardButton command, Long flashcardId) {
        return new CallbackData(command, flashcardId, new SwiperParams("", ""));
    }

    private JSONObject createInlineKeyboardButton(String text, CallbackData callbackData, ObjectMapper objectMapper)
            throws JsonProcessingException {
        JSONObject button = new JSONObject();
        button.put("text", text);
        button.put("callback_data", objectMapper.writeValueAsString(callbackData));
        return button;
    }

    private JSONArray createButtonRow(JSONObject button) {
        JSONArray row = new JSONArray();
        row.put(button);
        return row;
    }
}
