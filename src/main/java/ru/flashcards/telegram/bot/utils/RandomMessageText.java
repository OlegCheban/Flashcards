package ru.flashcards.telegram.bot.utils;


import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class RandomMessageText {

    public static List<String> positiveMessages = Arrays.asList(
            "Brilliant!",
            "Wonderful!",
            "Keep it up!",
            "Fantastic!",
            "Excellent!",
            "Superb!",
            "Spectacular!",
            "Outstanding!",
            "Awesome!",
            "Keep going!",
            "Amasing!",
            "That's right!",
            "Astonishing!",
            "Well done!",
            "Magnificent!"
    );

    public static String getPositiveMessage(){
        int randomNum = ThreadLocalRandom.current().nextInt(0, positiveMessages.size());
        return positiveMessages.get(randomNum);
    }

    public static String getNegativeMessage(){
        List<String> negativeMessages = Arrays.asList(
                "Wrong",
                "Incorrect",
                "Mistake",
                "Improperly",
                "Miss",
                "Failure",
                "Bad"
        );

        int randomNum = ThreadLocalRandom.current().nextInt(0, negativeMessages.size());
        return negativeMessages.get(randomNum);
    }

    public static String getAnotherMessage(){
        List<String> negativeMessages = Arrays.asList(
                "What are you doing?",
                "Keep Learning! "
        );

        int randomNum = ThreadLocalRandom.current().nextInt(0, negativeMessages.size());
        return negativeMessages.get(randomNum);
    }
}
