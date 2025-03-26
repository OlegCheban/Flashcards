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
        prepositions.put(35, Arrays.asList("She has a natural *talent* ... playing the piano.","for"));
        prepositions.put(36, Arrays.asList("He felt *guilty* ... forgetting his best friend's birthday.","about"));
        prepositions.put(37, Arrays.asList("There's been a significant *increase* ... the cost of living this year.","in"));
        prepositions.put(38, Arrays.asList("She's extremely *good* ... solving complex math problems.","at"));
        prepositions.put(39, Arrays.asList("The company is looking for a *substitute* ... their outgoing manager.","for"));
        prepositions.put(40, Arrays.asList("He's completely *addicted* ... playing video games.","to"));
        prepositions.put(41, Arrays.asList("The teacher was *concerned* ... the student's sudden drop in grades.","about"));
        prepositions.put(42, Arrays.asList("She made an important *contribution* ... the research project.","to"));
        prepositions.put(43, Arrays.asList("He's *jealous* ... his brother's success.","of"));
        prepositions.put(44, Arrays.asList("The doctor was *optimistic* ... the patient's recovery.","about"));
        prepositions.put(45, Arrays.asList("There's no *reason* ... such rude behavior.","for"));
        prepositions.put(46, Arrays.asList("She's *famous* ... her innovative designs.","for"));
        prepositions.put(47, Arrays.asList("He's *responsible* ... managing the entire department.","for"));
        prepositions.put(48, Arrays.asList("The child was *afraid* ... the dark.","of"));
        prepositions.put(49, Arrays.asList("She's very *particular* ... how her coffee is prepared.","about"));
        prepositions.put(50, Arrays.asList("He showed no *respect* ... the traditional customs.","for"));
        prepositions.put(51, Arrays.asList("The hotel provides excellent *service* ... its guests.","to"));
        prepositions.put(52, Arrays.asList("She's *allergic* ... peanuts.","to"));
        prepositions.put(53, Arrays.asList("He's completely *absorbed* ... his new book.","in"));
        prepositions.put(54, Arrays.asList("The students were *curious* ... the new teacher's background.","about"));
        prepositions.put(55, Arrays.asList("She felt *grateful* ... all the support she received.","for"));
        prepositions.put(56, Arrays.asList("There's a growing *demand* ... renewable energy solutions.","for"));
        prepositions.put(57, Arrays.asList("He's always been *interested* ... ancient history.","in"));
        prepositions.put(58, Arrays.asList("She was *embarrassed* ... her mistake during the presentation.","about"));
        prepositions.put(59, Arrays.asList("The team is *dependent* ... their star player.","on"));
        prepositions.put(60, Arrays.asList("He made a rude *comment* ... her appearance.","about"));
        prepositions.put(61, Arrays.asList("She's *proud* ... her daughter's academic achievements.","of"));
        prepositions.put(62, Arrays.asList("The company is *aware* ... the potential risks.","of"));
        prepositions.put(63, Arrays.asList("He's completely *focused* ... his career right now.","on"));
        prepositions.put(64, Arrays.asList("There's a strict *ban* ... smoking in this area.","on"));
        prepositions.put(65, Arrays.asList("She's *capable* ... handling difficult situations.","of"));
        prepositions.put(66, Arrays.asList("He showed great *loyalty* ... his team.","to"));
        prepositions.put(67, Arrays.asList("The book provides a fresh *perspective* ... the issue.","on"));
        prepositions.put(68, Arrays.asList("She's *enthusiastic* ... learning new languages.","about"));
        prepositions.put(69, Arrays.asList("He had a strong *influence* ... the final decision.","on"));
        prepositions.put(70, Arrays.asList("The research shows a clear *link* ... diet and health.","between"));
        prepositions.put(71, Arrays.asList("She had a strong *belief* ... the power of education.","in"));
        prepositions.put(72, Arrays.asList("The company issued an *apology* ... the delayed shipment.","for"));
        prepositions.put(73, Arrays.asList("He developed an *immunity* ... that particular virus.","to"));
        prepositions.put(74, Arrays.asList("There's growing *evidence* ... climate change effects.","of"));
        prepositions.put(75, Arrays.asList("She felt *remorse* ... her harsh words.","for"));
        prepositions.put(76, Arrays.asList("The documentary provided *insight* ... marine life.","into"));
        prepositions.put(77, Arrays.asList("He showed remarkable *patience* ... the noisy children.","with"));
        prepositions.put(78, Arrays.asList("The town implemented new *measures* ... crime prevention.","against"));
        prepositions.put(79, Arrays.asList("She had a natural *aversion* ... violent movies.","to"));
        prepositions.put(80, Arrays.asList("The professor gave *feedback* ... our research papers.","on"));
        prepositions.put(81, Arrays.asList("There's a special *discount* ... senior citizens.","for"));
        prepositions.put(82, Arrays.asList("He expressed *sympathy* ... the victims' families.","for"));
        prepositions.put(83, Arrays.asList("The study revealed a *connection* ... sleep and productivity.","between"));
        prepositions.put(84, Arrays.asList("She made a generous *donation* ... the animal shelter.","to"));
        prepositions.put(85, Arrays.asList("The lawyer presented *arguments* ... his client's innocence.","for"));
        prepositions.put(86, Arrays.asList("He had a peculiar *fondness* ... vintage typewriters.","for"));
        prepositions.put(87, Arrays.asList("The museum has an impressive *collection* ... Renaissance art.","of"));
        prepositions.put(88, Arrays.asList("She felt *uneasy* ... the strange noises at night.","about"));
        prepositions.put(89, Arrays.asList("The government imposed *restrictions* ... water usage.","on"));
        prepositions.put(90, Arrays.asList("He showed great *respect* ... his elders.","for"));
        prepositions.put(91, Arrays.asList("The team developed a *strategy* ... dealing with emergencies.","for"));
        prepositions.put(92, Arrays.asList("She had a valid *excuse* ... being late.","for"));
        prepositions.put(93, Arrays.asList("The book offers practical *advice* ... starting a business.","on"));
        prepositions.put(94, Arrays.asList("He took *responsibility* ... the project's failure.","for"));
        prepositions.put(95, Arrays.asList("The child showed *obedience* ... his parents.","to"));
        prepositions.put(96, Arrays.asList("She had a serious *addiction* ... social media.","to"));
        prepositions.put(97, Arrays.asList("The doctor explained the *risks* ... the procedure.","of"));
        prepositions.put(98, Arrays.asList("He expressed *gratitude* ... everyone's support.","for"));
        prepositions.put(99, Arrays.asList("The city implemented a *ban* ... plastic bags.","on"));
        prepositions.put(100, Arrays.asList("She felt *nostalgic* ... her childhood home.","for"));
        prepositions.put(101, Arrays.asList("The teacher had high *expectations* ... her students.","of"));
        prepositions.put(102, Arrays.asList("He developed *tolerance* ... spicy food.","to"));
        prepositions.put(103, Arrays.asList("The article raised *awareness* ... mental health issues.","about"));
        prepositions.put(104, Arrays.asList("She had a strange *fascination* ... abandoned places.","with"));
        prepositions.put(105, Arrays.asList("The company faced *criticism* ... its environmental policies.","for"));
        prepositions.put(106, Arrays.asList("He showed *hostility* ... the new regulations.","toward"));
        prepositions.put(107, Arrays.asList("The study found a *correlation* ... exercise and happiness.","between"));
        prepositions.put(108, Arrays.asList("She had complete *confidence* ... her team.","in"));
        prepositions.put(109, Arrays.asList("The politician expressed *concern* ... rising unemployment.","about"));
        prepositions.put(110, Arrays.asList("He had little *tolerance* ... incompetence.","for"));
    }
    public Map<Integer, List<String>> getPrepositions (){
        return prepositions;
    }
}