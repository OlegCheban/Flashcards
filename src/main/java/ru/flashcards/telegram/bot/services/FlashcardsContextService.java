package ru.flashcards.telegram.bot.services;

import lombok.AllArgsConstructor;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.util.List;

@Service
@AllArgsConstructor
public class FlashcardsContextService {

    private OpenAiChatModel chatModel;

    public String generateContext(String flashcards){
        String userText =  String.format("""
            Create a story using the following words: %s.
            Ensure the story is engaging and helps English learners understand the meaning of these words in context.
            The story should be approximately 50-150 words long. Wrap the placeholders with * symbols on both sides to make them bold.
        """, flashcards);

        String systemText = """
                You are a futuristic AI content creator specializing in sci-fi, high-tech, IT, space exploration, and advanced science narratives. 
                Your task is to generate engaging, modern, and technically plausible texts (not fairy tales) using predetermined keywords. Each story should: 
                1. Use all the predetermined words in a natural and meaningful way.
                2. Use natural repetition of keywords without forced insertion. 
                3. Incorporate the words into a coherent narrative with a clear beginning, middle, and end.
                4. Provide context that helps learners understand the meaning of the words through the story.
                5. Be appropriate for English learners, using simple grammar and vocabulary while maintaining an engaging tone.
                6. Vary the themes and settings for each story to keep the content fresh and interesting.
                7. The generated story should have phrasal verbs, idioms, useful language construction and be natural for natives. Stick to upper-intermediate/advanced English level.
                
                8. Genre & Style: 
                    1. Focus on hard sci-fi, cyberpunk, near-future tech, space missions, AI ethics, hacking, or dystopian/utopian societies.
                    2. Use realistic jargon (e.g., "neural interface," "zero-gravity lab," "quantum encryption") but keep it accessible for English learners.
                    3. Avoid fantasy tropes (magic, dragons, fairy-tale endings).
                
                9. Avoid repeating the same story structure or themes unless requested.
        """;

        return generate(userText, systemText);
    }

    public String generateFlashcardReport(String flashcard){
        String userText = MessageFormat.format("""
                The report should cover various meanings and usage examples of the word "{0}".
                
                **Meanings**
                - List all possible meanings of "{0}" using simple language suitable for the target audience.
                
                **Usage Examples**
                Provide short examples demonstrating the usage of "{0}" in different forms:
                - **As a Noun**: [Example sentence]
                - **As a Pronoun**: [Example sentence]
                - **As an Adjective**: [Example sentence]
                - **As an Adverb**: [Example sentence]
                - **As a Verb**: [Example sentence]
                
                Ensure the examples are relevant and easy to understand for intermediate and upper-intermediate learners.
                
                **Language Level**
                Write the report in a way that is accessible to intermediate and upper-intermediate English learners. Avoid using overly complex vocabulary or grammar structures.
                
                **Using**
                Define if the word "{0}" using in formal English conversation or informal. Will it be natural to use this word in spoken English.
                
                **Final Note**
                Conclude the report with a brief note encouraging learners to practice using "{0}" in context.
        """, flashcard);

        String systemText = "You are a professional native English tutor. You have a strong grasp in English vocabulary and can recommend to an English learner if a word is valuable for learning or it can be omitted.";


        return generate(userText, systemText);
    }

    private String generate(String userText, String systemText){
        Message userMessage = new UserMessage(userText.trim());
        SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(systemText.trim());
        Message systemMessage = systemPromptTemplate.createMessage();
        Prompt prompt = new Prompt(List.of(userMessage, systemMessage));
        return chatModel.call(prompt).getResults().get(0).getOutput().getText();
    }
}
