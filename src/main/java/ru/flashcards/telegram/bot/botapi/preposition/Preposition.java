package ru.flashcards.telegram.bot.botapi.preposition;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class Preposition {
    private static Logger logger = LoggerFactory.getLogger(Preposition.class);

    private Map<Integer, List<String>> prepositions = new HashMap<>();
    {
        logger.info("Preposition static block initialization");
        prepositions.put(1, Arrays.asList("There's no point in getting *angry* ... things that don't matter.","about"));
        prepositions.put(2, Arrays.asList("Her *attitude* ... life is always positive.","to"));
        prepositions.put(3, Arrays.asList("Upon receiving the unexpected gift, she was *delighted* ... the thoughtfulness and generosity of her friend.","with"));
        prepositions.put(4, Arrays.asList("After watching the same TV show for the third time, he was *bored* ... the lack of variety and decided to find something new to watch.","with"));
        prepositions.put(5, Arrays.asList("Are you *nervous* ... the exam?","about"));
        prepositions.put(6, Arrays.asList("He was *amazed* ... the magician's tricks.","by","at"));
        prepositions.put(7, Arrays.asList("He was *furious* ... his roommate for constantly leaving dirty dishes in the sink","with"));
        prepositions.put(8, Arrays.asList("Everybody was *surprised* ... the news","by","at"));
        prepositions.put(9, Arrays.asList("Are you *annoyed* ... me for being late?","with"));
        prepositions.put(10, Arrays.asList("She was *pleased* ... the progress of her students, seeing how they had improved their skills over the semester.","with"));
        prepositions.put(11, Arrays.asList("Hard work is often the *key* ... success.","to"));
        prepositions.put(12, Arrays.asList("He was *tired* ... his monotonous job and yearned for a new challenge.","of"));
        prepositions.put(13, Arrays.asList("I'm still waiting for his *reply* ... my email.","to"));
        prepositions.put(14, Arrays.asList("After completing the project ahead of schedule and exceeding all expectations, the client was *satisfied* ... the outcome.","with"));
        prepositions.put(15, Arrays.asList("I hope you weren't *shocked* ... the news","at","by"));
        prepositions.put(16, Arrays.asList("She was *impressed*... the level of detail in the artwork, admiring the artist's skill and creativity.","with","by"));
        prepositions.put(17, Arrays.asList("She was *astonished* ... his sudden announcement.","at","by"));
        prepositions.put(18, Arrays.asList("He was *astonished* ... the incredible performance.","by","at"));
        prepositions.put(19, Arrays.asList("He was frustrated by the *damage* ... his phone screen.","to"));
        prepositions.put(20, Arrays.asList("Finding a *solution* ... the problem took longer than expected.","to"));
        prepositions.put(21, Arrays.asList("He was *happy* ... the outcome because it meant that his favorite team had won the championship after years of hard work and dedication.","about"));
        prepositions.put(22, Arrays.asList("She was *excited* ... the upcoming trip to Paris","about"));
        prepositions.put(23, Arrays.asList("She was *fed up* ... her noisy neighbors partying late into the night, disrupting her sleep.","with"));
        prepositions.put(24, Arrays.asList("He was *impressed* ... his friend's dedication to charity work, finding inspiration in their selflessness and commitment to helping others.","by","with"));
        prepositions.put(25, Arrays.asList("She provided the *answer* ... the difficult question.","to"));
        prepositions.put(26, Arrays.asList("She was *sick* ... her neighbor's loud parties every weekend and wished for some peace and quiet.","of"));
        prepositions.put(27, Arrays.asList("She was *amazed* ... the view.","at","by"));
        prepositions.put(28, Arrays.asList("Lisa is *upset* ... not being invited to the party.","about"));
        prepositions.put(29, Arrays.asList("They sent out an *invitation* ... their wedding.","to"));
        prepositions.put(30, Arrays.asList("He couldn't help but feel *worried* ...  his daughter's safety","about"));
        prepositions.put(31, Arrays.asList("Were you *happy* ... your exam results?","with"));
        prepositions.put(32, Arrays.asList("He was *disappointed* ... the quality of service at the restaurant.","with"));
        prepositions.put(33, Arrays.asList("She was *furious* ... the reckless damage done to her garden by the neighbor's unruly dog.","about"));
        prepositions.put(34, Arrays.asList("His *reaction* ... the news was unexpected.","to"));
    }
    public Map<Integer, List<String>> getPrepositions (){
        return prepositions;
    }
}