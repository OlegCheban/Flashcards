package ru.flashcards.telegram.bot.botapi.handlers.command;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.flashcards.telegram.bot.botapi.*;
import ru.flashcards.telegram.bot.botapi.handlers.learn.exercises.common.ExerciseProvider;
import ru.flashcards.telegram.bot.botapi.records.CallbackData;
import ru.flashcards.telegram.bot.botapi.preposition.PrepositionLearningMode;
import ru.flashcards.telegram.bot.botapi.swiper.Swiper;
import ru.flashcards.telegram.bot.botapi.wateringSession.WateringSessionQuestion;
import ru.flashcards.telegram.bot.db.*;
import ru.flashcards.telegram.bot.db.dto.ExerciseKind;
import ru.flashcards.telegram.bot.db.dto.SwiperFlashcard;
import ru.flashcards.telegram.bot.db.dto.UserFlashcard;
import ru.flashcards.telegram.bot.services.InlineKeyboardCreator;
import ru.flashcards.telegram.bot.services.SendMessageService;
import ru.flashcards.telegram.bot.utils.ExerciseCodeMapper;
import ru.flashcards.telegram.bot.utils.Help;
import ru.flashcards.telegram.bot.utils.Number;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static ru.flashcards.telegram.bot.botapi.BotCommand.*;
import static ru.flashcards.telegram.bot.botapi.BotKeyboardButton.*;
import static ru.flashcards.telegram.bot.botapi.BotReplyMsg.FLASHCARDS_NOT_FOUND_MSG;
import static ru.flashcards.telegram.bot.botapi.BotReplyMsg.UNRECOGNIZED_OPTION_MSG;

@Component
@RequiredArgsConstructor
public class CommandMessageHandler implements MessageHandler<Message> {
    private final WateringSessionsDao wateringSessionsDao;
    private final NotificationsDao notificationsDao;
    private final SuggestFlashcard suggestFlashcard;
    private final WateringSessionQuestion wateringSessionQuestion;
    private final UserModeSettings userModeSettings;
    private final PrepositionLearningMode prepositionLearningMode;
    private final UserMessageTypeBuffer userMessageTypeBuffer;
    private final ExerciseProvider exerciseProvider;
    private final LearningExercisesDao learningExercisesDao;
    private final UserProfileFlashcardsDao userProfileFlashcardsDao;
    private final SwiperDao swiperDao;
    private final UsersDao usersDao;
    private final SendMessageService sendMessageService;
    private Long chatId;

    @Override
    public List<BotApiMethod<?>> handle(Message message) {
        chatId = message.getChatId();
        String messageText = message.getText();
        if (messageText.startsWith(START.command)) {
            return start(message.getChat().getUserName());

        } else if (messageText.startsWith(TRAINING_FLASHCARDS_QUANTITY_SETTINGS.command)){
            return trainingFlashcardsQuantitySettings(messageText.replace(TRAINING_FLASHCARDS_QUANTITY_SETTINGS.command, "").trim());
            
        } else if (messageText.startsWith(FIND_FLASHCARD.command)){
            return find(messageText.replace(FIND_FLASHCARD.command, ""));

        } else if (messageText.startsWith(START_LEARNING.command)){
            return learn();

        } else if (messageText.startsWith(RECENT_LEARNED.command)){
            return recentLearned(messageText.replace(RECENT_LEARNED.command, ""));

        } else if (messageText.startsWith(HELP.command)){
            return help();

        } else if (messageText.startsWith(CHANGE_TRANSLATION.command)){
            return changeTranslation(messageText.replace(CHANGE_TRANSLATION.command, ""));

        } else if (messageText.startsWith(ENABLE_EXERCISE.command)){
            return EnableExcercise();

        } else if (messageText.startsWith(DISABLE_EXERCISE.command)){
            return DisableExcercise();

        } else if (messageText.startsWith(WATERING_SESSION_REPLY_TIME_SETTINGS.command)){
            return wateringSessionReplyTimeSettings(messageText.replace(WATERING_SESSION_REPLY_TIME_SETTINGS.command, "").trim());

        } else if (messageText.startsWith(START_WATERING_SESSION.command)){
            return startWateringSession();

        } else if (messageText.startsWith(NOTIFICATION_INTERVAL_SETTINGS.command)){
            return notificationIntervalSettings(messageText.replace(NOTIFICATION_INTERVAL_SETTINGS.command, "").trim());

        } else if (messageText.startsWith(OPEN_SWIPER.command)){
            return openSwiper(messageText.replace(OPEN_SWIPER.command, ""));

        } else if (messageText.startsWith(PREPOSITION.command)){
            return startPrepositionMode();
        }

        return Collections.emptyList();
    }

    private List<BotApiMethod<?>> openSwiper(String text) {
        List<BotApiMethod<?>> list = new ArrayList<>();
        String[] params = text.trim().split(" ");
        String characterConditionParam = "";
        String percentile = "";

        if (params.length > 2) {
            list.add(createMessage(chatId, UNRECOGNIZED_OPTION_MSG));
            return list;
        }

        if (params.length == 1) {
            if (Number.isInteger(params[0], 10)) {
                percentile = params[0];
            } else {
                characterConditionParam = params[0];
            }
        }

        if (params.length == 2) {
            boolean firstIsNumber = Number.isInteger(params[0], 10);
            boolean secondIsNumber = Number.isInteger(params[1], 10);
            if (firstIsNumber && secondIsNumber || !firstIsNumber && !secondIsNumber){
                list.add(createMessage(chatId, UNRECOGNIZED_OPTION_MSG));
                return list;
            }
            percentile = firstIsNumber ? params[0] : params[1];
            characterConditionParam = !firstIsNumber ? params[0] : params[1];
        }

        Long firstFlashcard = swiperDao.getFirstSwiperFlashcard(chatId, characterConditionParam, percentile);

        if (firstFlashcard != null){
            SwiperFlashcard swiperFlashcard =
                    swiperDao.getSwiperFlashcard(chatId, firstFlashcard, characterConditionParam, percentile);
            if (swiperFlashcard != null){
                Swiper swiper = new Swiper(characterConditionParam, swiperFlashcard, percentile);

                SendMessage replyMessage = new SendMessage();
                replyMessage.setChatId(String.valueOf(chatId));
                replyMessage.setText("*" + swiperFlashcard.word() + "* /" + swiperFlashcard.transcription() +
                        "/ ("+swiperFlashcard.learnPrc()+"% выучено)\n" + swiperFlashcard.description() +
                        "\n\n" + "*Перевод:* " + swiperFlashcard.translation());
                replyMessage.enableMarkdown(true);
                replyMessage.setReplyMarkup(swiper.getSwiperKeyboardMarkup());
                list.add(replyMessage);

            } else {
                list.add(createMessage(chatId, FLASHCARDS_NOT_FOUND_MSG));
            }
        }

        return list;
    }

    private List<BotApiMethod<?>> notificationIntervalSettings(String qty) {
        List<BotApiMethod<?>> list = new ArrayList<>();
        if (Number.isInteger(qty, 10)) {
            notificationsDao.setNotificationInterval(Integer.valueOf(qty), chatId);
            list.add(createMessage(chatId, "Готово"));
        } else {
            list.add(createMessage(chatId, "Неверный параметр, должно быть число"));
        }

        return list;
    }

    private List<BotApiMethod<?>> trainingFlashcardsQuantitySettings(String qty) {
        List<BotApiMethod<?>> list = new ArrayList<>();
        if (Number.isInteger(qty, 10)) {
            learningExercisesDao.setTrainingFlashcardsQuantity(Integer.valueOf(qty), chatId);
            list.add(createMessage(chatId, "Готово"));
        } else {
            list.add(createMessage(chatId, "Неверный параметр, должно быть число"));
        }

        return list;
    }

    private List<BotApiMethod<?>> wateringSessionReplyTimeSettings(String seconds) {
        List<BotApiMethod<?>> list = new ArrayList<>();

        if (Number.isInteger(seconds, 10)) {
            wateringSessionsDao.setWateringSessionReplyTime(Integer.valueOf(seconds), chatId);
            list.add(createMessage(chatId, "Готово"));
        } else {
            list.add(createMessage(chatId, "Неверный параметр, должно быть число"));
        }

        return list;
    }

    private List<BotApiMethod<?>> changeTranslation(String keyword){
        List<BotApiMethod<?>> list = new ArrayList<>();
        UserFlashcard userFlashcard = userProfileFlashcardsDao.findUserFlashcardByName(chatId, keyword.trim());
        if (userFlashcard == null){
            list.add(createMessage(chatId, "Карточка не найдена в Вашем профиле"));
        } else {
            list.add(createMessage(chatId, "Отправьте перевод"));
            userMessageTypeBuffer.putRequest(chatId, userFlashcard.id(), MessageType.CHANGE_TRANSLATION);
        }

        return list;
    }

    private List<BotApiMethod<?>> help(){
        List<BotApiMethod<?>> list = new ArrayList<>();
        list.add(createManual(chatId));

        return list;
    }

    private List<BotApiMethod<?>> DisableExcercise(){
        List<BotApiMethod<?>> list = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();

        SendMessage replyMessage = new SendMessage();
        replyMessage.setChatId(String.valueOf(chatId));

        List<ExerciseKind> exerciseKinds  = learningExercisesDao.getExerciseKindToDisable(chatId);
        if (!exerciseKinds.isEmpty()) {
            InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

            exerciseKinds.forEach(v -> {
                List<InlineKeyboardButton> rowInline = new ArrayList<>();
                InlineKeyboardButton button = new InlineKeyboardButton();
                button.setText(v.name());
                CallbackData callbackData = new CallbackData(DISABLE, ExerciseCodeMapper.getMappedCode(v.code()));
                try {
                    button.setCallbackData(objectMapper.writeValueAsString(callbackData));
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
                rowInline.add(button);
                rowsInline.add(rowInline);
            });
            markupInline.setKeyboard(rowsInline);
            replyMessage.setText("Выберите упражнение которое необходимо исключить: ");
            replyMessage.setReplyMarkup(markupInline);
        } else
        {
            replyMessage.setText("Все упражнения исключены");
        }

        list.add(replyMessage);
        return list;
    }

    private List<BotApiMethod<?>> EnableExcercise(){
        List<BotApiMethod<?>> list = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();
        SendMessage replyMessage = new SendMessage();
        replyMessage.setChatId(String.valueOf(chatId));

        List<ExerciseKind> exerciseKinds  = learningExercisesDao.getExerciseKindToEnable(chatId);
        if (!exerciseKinds.isEmpty()) {
            InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

            exerciseKinds.forEach(v->{
                List<InlineKeyboardButton> rowInline = new ArrayList<>();
                InlineKeyboardButton button = new InlineKeyboardButton();
                button.setText(v.name());
                CallbackData callbackData = new CallbackData(ENABLE, ExerciseCodeMapper.getMappedCode(v.code()));
                try {
                    button.setCallbackData(objectMapper.writeValueAsString(callbackData));
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
                rowInline.add(button);
                rowsInline.add(rowInline);
            });
            markupInline.setKeyboard(rowsInline);
            replyMessage.setText("Выберите упражнение которое необходимо включить:");
            replyMessage.setReplyMarkup(markupInline);
        }
        else {
            replyMessage.setText("Все упражнения добавлены");
        }

        list.add(replyMessage);
        return list;
    }

    private List<BotApiMethod<?>> startWateringSession(){
        List<BotApiMethod<?>> list = new ArrayList<>();
        if (learningExercisesDao.existsLearnedFlashcards(chatId)){
            //dataLayerObject.setWateringSessionMode(chatId, true);
            userModeSettings.setMode(chatId, UserMode.WATERING_SESSION);
            list.add(wateringSessionQuestion.newQuestion(chatId));
        } else {
            SendMessage sendMessage = new SendMessage();
            sendMessage.setText("Нет выученных карточек");
            sendMessage.setChatId(String.valueOf(chatId));
            list.add(sendMessage);
        }
        return list;
    }

    private List<BotApiMethod<?>> startPrepositionMode(){
        List<BotApiMethod<?>> list = new ArrayList<>();
        userModeSettings.setMode(chatId, UserMode.PREPOSITION);
        list.add(prepositionLearningMode.newPreposition(chatId));
        return list;
    }

    private List<BotApiMethod<?>> recentLearned(String qty){
        List<BotApiMethod<?>> list = new ArrayList<>();
        if (Number.isInteger(qty.trim(), 10)) {
            Long qtyNum = Long.valueOf(qty.trim());
            StringBuffer msg = new StringBuffer ();
            msg.append("Последние изученные карточки:\n");
            AtomicInteger i = new AtomicInteger(1);
            learningExercisesDao.getRecentLearned(chatId, qtyNum, true).forEach(v -> {
                msg.append(i +". " + v);
                msg.append("\n");
                i.getAndIncrement();
            });
            msg.append("\n");
            msg.append("Продолжайте учить!");
            List<JSONObject> listButtons = new ArrayList<>();
            listButtons.add(InlineKeyboardCreator.prepareButton(qtyNum, "Создать контекст", MAKEUP));
            sendMessageService.sendMessage(chatId, msg.toString(), String.valueOf(InlineKeyboardCreator.createButtonMenu(listButtons)));
        } else {
            list.add(createMessage(chatId, "Неверный параметр, должно быть число"));
        }
        return list;
    }

    private List<BotApiMethod<?>> start(String username){
        List<BotApiMethod<?>> list = new ArrayList<>();
        usersDao.registerUser(chatId, username);
        list.add(createManual(chatId));

        return list;
    }

    private List<BotApiMethod<?>> find(String keyword) {
        List<BotApiMethod<?>> list = new ArrayList<>();
        if (!keyword.trim().isEmpty()) {
            suggestFlashcard.byParam(chatId, keyword.trim());
        } else {
            suggestFlashcard.byTop3000Category(chatId);
        }
        return list;
    }

    private List<BotApiMethod<?>> learn(){
        List<BotApiMethod<?>> list = new ArrayList<>();
        if (learningExercisesDao.existsExercise(chatId)){
            //enable learn mode
            userModeSettings.setMode(chatId, UserMode.EXERCISE);
            //dataLayerObject.setLearnFlashcardState(chatId, true);
            //send an exercise
            list.add(exerciseProvider.newExercise(chatId));
        } else {
            SendMessage sendMessage = new SendMessage();
            sendMessage.setText("Нет доступных карточек для изучения. Добавьте карточки.");
            sendMessage.setChatId(String.valueOf(chatId));
            list.add(sendMessage);
        }
        return list;
    }

    private SendMessage createManual(Long chatId){
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setParseMode("Markdown");
        sendMessage.setText(Help.sendBotManualRusConcise());
        return sendMessage;
    }
    private SendMessage createMessage(Long chatId, String message){
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setParseMode("Markdown");
        sendMessage.setText(message);
        return sendMessage;
    }
}
