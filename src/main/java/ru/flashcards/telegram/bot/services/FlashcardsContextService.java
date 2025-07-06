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
            Create a comprehensive report about the word "{0}" for English learners.
            
            IMPORTANT: Format the output as plain text without any markdown symbols (no ##, **, *, etc.).
            Use simple formatting with clear sections and bullet points using dashes or dots.
            
            📖 REPORT ON THE WORD "{0}"
            
            📝 Meanings
            The word "{0}" has the following meanings:
            - [List each meaning with simple explanations]
            
            💡 Usage Examples
            Here are examples showing how to use "{0}" in different contexts:
            
            As a Verb (Most Common):
            - [Example sentence]
            
            As a Noun (if applicable):
            - [Example sentence]
            
            As an Adjective (if applicable):
            - [Example sentence]
            
            As an Adverb (if applicable):
            - [Example sentence]
            
            🎯 Language Level & Formality
            - Formal/Informal usage: [Explain context]
            - Spoken English: [Explain if natural in speech]
            - Recommended for: [Learning level]
            
            🚀 Final Note
            [Brief encouraging note about practicing this word]
            
            Remember: Use clear, simple language. No markdown formatting. Use emojis and simple bullet points for visual appeal.
            """, flashcard);

        String systemText = """
            You are a professional native English tutor. Create reports that are:
            1. Easy to read in messaging apps
            2. Use simple formatting (no markdown symbols)
            3. Include emojis for visual appeal
            4. Suitable for intermediate English learners
            5. Well-structured with clear sections
            """;

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
