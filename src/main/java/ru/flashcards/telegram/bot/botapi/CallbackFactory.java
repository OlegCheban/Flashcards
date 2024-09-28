package ru.flashcards.telegram.bot.botapi;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import ru.flashcards.telegram.bot.botapi.handlers.examples.FlashcardUsageExamplesCallbackHandler;
import ru.flashcards.telegram.bot.botapi.handlers.learn.*;
import ru.flashcards.telegram.bot.botapi.handlers.swiper.BoostPriorityCallbackHandler;
import ru.flashcards.telegram.bot.botapi.handlers.swiper.RemoveFlashcardCallbackHandler;
import ru.flashcards.telegram.bot.botapi.handlers.swiper.ReturnToLearnSwiperCallbackHandler;
import ru.flashcards.telegram.bot.botapi.handlers.swiper.SwiperRefreshFlashcardCallbackHandler;
import ru.flashcards.telegram.bot.botapi.handlers.translation.TranslateFlashcardCallbackHandler;
import ru.flashcards.telegram.bot.botapi.records.CallbackData;

import java.util.Collections;

@Component
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CallbackFactory implements CallbackHandlerAbstractFactory<MessageHandler<CallbackQuery>> {
    TranslateFlashcardCallbackHandler translateFlashcardCallbackHandler;
    AddToLearnCallbackHandler addToLearnCallbackHandler;
    AddToLearnAndNextCallbackHandler addToLearnAndNextCallbackHandler;
    ProceedToRepetitionCallbackHandler proceedToRepetitionCallbackHandler;
    ReturnToLearnSwiperCallbackHandler returnToLearnSwiperCallbackHandler;
    ReturnToLearnCallbackHandler returnToLearnCallbackHandler;
    BoostPriorityCallbackHandler boostPriorityCallbackHandler;
    ExcludeCallbackHandler excludeCallbackHandler;
    ExcludeAndNextCallbackHandler excludeAndNextCallbackHandler;
    SwiperRefreshFlashcardCallbackHandler swiperRefreshFlashcardCallbackHandler;
    FlashcardUsageExamplesCallbackHandler flashcardUsageExamplesCallbackHandler;
    QuiteCallbackHandler quiteCallbackHandler;
    DisableExerciseMessageHandler disableExerciseMessageHandler;
    EnableExerciseMessageHandler enableExerciseMessageHandler;
    RemoveFlashcardCallbackHandler removeFlashcardCallbackHandler;

    @Override
    public MessageHandler<CallbackQuery> getHandler(CallbackData callbackData) {
        switch (callbackData.command()){
            case TRANSLATE:
                return translateFlashcardCallbackHandler;
            case ADD:
                return addToLearnCallbackHandler;
            case ADD_NEXT:
                return addToLearnAndNextCallbackHandler;
            case PROCEED:
                return proceedToRepetitionCallbackHandler;
            case SRELEARN:
                return returnToLearnSwiperCallbackHandler;
            case RTL:
                return returnToLearnCallbackHandler;
            case BOOST:
                return boostPriorityCallbackHandler;
            case EXCL:
                return excludeCallbackHandler;
            case EXCLN:
                return excludeAndNextCallbackHandler;
            case PREV:
            case NEXT:
                return swiperRefreshFlashcardCallbackHandler;
            case EXAMPLES:
                return flashcardUsageExamplesCallbackHandler;
            case QUITE:
                return quiteCallbackHandler;
            case DISABLE:
                return disableExerciseMessageHandler;
            case ENABLE:
                return enableExerciseMessageHandler;
            case REMOVE:
                return removeFlashcardCallbackHandler;
        }

        return m -> Collections.emptyList();
    }
}
