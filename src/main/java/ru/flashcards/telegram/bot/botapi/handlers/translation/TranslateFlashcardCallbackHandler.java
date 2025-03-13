package ru.flashcards.telegram.bot.botapi.handlers.translation;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.flashcards.telegram.bot.botapi.records.CallbackData;
import ru.flashcards.telegram.bot.botapi.MessageHandler;
import ru.flashcards.telegram.bot.botapi.records.SwiperParams;
import ru.flashcards.telegram.bot.db.UserProfileFlashcardsDao;
import ru.flashcards.telegram.bot.db.dto.UserFlashcard;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.toIntExact;
import static ru.flashcards.telegram.bot.botapi.BotKeyboardButton.EXS;

@Component
@AllArgsConstructor
public class TranslateFlashcardCallbackHandler implements MessageHandler<CallbackQuery> {
    private UserProfileFlashcardsDao userProfileFlashcardsDao;

    @Override
    public List<BotApiMethod<?>> handle(CallbackQuery callbackQuery) {
        CallbackData callbackData1 = jsonToCallbackData(callbackQuery.getData());
        List<BotApiMethod<?>> list = new ArrayList<>();
        var message = callbackQuery.getMessage();
        long messageId = message.getMessageId();
        long chatId = message.getChatId();
        Long userFlashcardId = callbackData1.entityId();
        UserFlashcard flashcard = userProfileFlashcardsDao.findUserFlashcardById(userFlashcardId);

        EditMessageText translationMessage = new EditMessageText();
        translationMessage.setChatId(String.valueOf(chatId));
        translationMessage.setMessageId(toIntExact(messageId));
        translationMessage.enableMarkdown(true);

        String pushpinEmoji = "\uD83D\uDCCC";
        translationMessage.setText("*" + flashcard.word() + "* /" + flashcard.transcription() + "/ " + pushpinEmoji + " \n"+flashcard.description() + "\n\n"+flashcard.translation());

        CallbackData callbackData = new CallbackData(EXS, userFlashcardId, new SwiperParams("", ""));

        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText("example of usage");
        button.setCallbackData(callbackDataToJson(callbackData));
        rowInline.add(button);
        rowsInline.add(rowInline);
        markupInline.setKeyboard(rowsInline);

        translationMessage.setReplyMarkup(markupInline);
        list.add(translationMessage);

        return list;
    }
}