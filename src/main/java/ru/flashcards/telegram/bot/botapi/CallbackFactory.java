package ru.flashcards.telegram.bot.botapi;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import ru.flashcards.telegram.bot.botapi.handlers.context.FlashcardsAIRepCallbackHandler;
import ru.flashcards.telegram.bot.botapi.handlers.context.FlashcardsContextCallbackHandler;
import ru.flashcards.telegram.bot.botapi.handlers.examples.FlashcardUsageExamplesCallbackHandler;
import ru.flashcards.telegram.bot.botapi.handlers.learn.AddToLearnAndNextCallbackHandler;
import ru.flashcards.telegram.bot.botapi.handlers.learn.AddToLearnCallbackHandler;
import ru.flashcards.telegram.bot.botapi.handlers.learn.DisableExerciseCallbackHandler;
import ru.flashcards.telegram.bot.botapi.handlers.learn.EnableExerciseCallbackHandler;
import ru.flashcards.telegram.bot.botapi.handlers.learn.ExcludeAndNextCallbackHandler;
import ru.flashcards.telegram.bot.botapi.handlers.learn.ExcludeCallbackHandler;
import ru.flashcards.telegram.bot.botapi.handlers.learn.ProceedToRepetitionCallbackHandler;
import ru.flashcards.telegram.bot.botapi.handlers.learn.QuitCallbackHandler;
import ru.flashcards.telegram.bot.botapi.handlers.learn.ReturnToLearnCallbackHandler;
import ru.flashcards.telegram.bot.botapi.handlers.pronunciation.PronunciationCallbackHandler;
import ru.flashcards.telegram.bot.botapi.handlers.swiper.BoostPriorityCallbackHandler;
import ru.flashcards.telegram.bot.botapi.handlers.swiper.RemoveFlashcardCallbackHandler;
import ru.flashcards.telegram.bot.botapi.handlers.swiper.ReturnToLearnSwiperCallbackHandler;
import ru.flashcards.telegram.bot.botapi.handlers.swiper.SwiperRefreshFlashcardCallbackHandler;
import ru.flashcards.telegram.bot.botapi.handlers.translation.TranslateFlashcardCallbackHandler;
import ru.flashcards.telegram.bot.botapi.records.CallbackData;

@Component
@RequiredArgsConstructor
public class CallbackFactory {
    private final TranslateFlashcardCallbackHandler translateFlashcardCallbackHandler;
    private final AddToLearnCallbackHandler addToLearnCallbackHandler;
    private final AddToLearnAndNextCallbackHandler addToLearnAndNextCallbackHandler;
    private final ProceedToRepetitionCallbackHandler proceedToRepetitionCallbackHandler;
    private final ReturnToLearnSwiperCallbackHandler returnToLearnSwiperCallbackHandler;
    private final ReturnToLearnCallbackHandler returnToLearnCallbackHandler;
    private final BoostPriorityCallbackHandler boostPriorityCallbackHandler;
    private final ExcludeCallbackHandler excludeCallbackHandler;
    private final ExcludeAndNextCallbackHandler excludeAndNextCallbackHandler;
    private final SwiperRefreshFlashcardCallbackHandler swiperRefreshFlashcardCallbackHandler;
    private final FlashcardUsageExamplesCallbackHandler flashcardUsageExamplesCallbackHandler;
    private final QuitCallbackHandler quitCallbackHandler;
    private final DisableExerciseCallbackHandler disableExerciseCallbackHandler;
    private final EnableExerciseCallbackHandler enableExerciseCallbackHandler;
    private final RemoveFlashcardCallbackHandler removeFlashcardCallbackHandler;
    private final FlashcardsContextCallbackHandler flashcardsContextCallbackHandler;
    private final FlashcardsAIRepCallbackHandler flashcardsAIRepCallbackHandler;
    private final PronunciationCallbackHandler pronunciationCallbackHandler;

    public CallbackHandler getHandler(CallbackData callbackData) {
        return switch (callbackData.command()) {
            case TRANSLATE -> translateFlashcardCallbackHandler;
            case ADD -> addToLearnCallbackHandler;
            case ADD_NEXT -> addToLearnAndNextCallbackHandler;
            case AIREP -> flashcardsAIRepCallbackHandler;
            case PROCEED -> proceedToRepetitionCallbackHandler;
            case SRTL -> returnToLearnSwiperCallbackHandler;
            case RTL -> returnToLearnCallbackHandler;
            case BST -> boostPriorityCallbackHandler;
            case EXCL -> excludeCallbackHandler;
            case EXCLN -> excludeAndNextCallbackHandler;
            case PR, NE -> swiperRefreshFlashcardCallbackHandler;
            case EXS -> flashcardUsageExamplesCallbackHandler;
            case QUITE -> quitCallbackHandler;
            case DISABLE -> disableExerciseCallbackHandler;
            case ENABLE -> enableExerciseCallbackHandler;
            case REM -> removeFlashcardCallbackHandler;
            case MAKEUP -> flashcardsContextCallbackHandler;
            case PRON, SPRON -> pronunciationCallbackHandler;
        };
    }
}
