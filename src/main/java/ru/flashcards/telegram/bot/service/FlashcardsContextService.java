package ru.flashcards.telegram.bot.service;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FlashcardsContextService {

    OpenAiChatModel chatModel;

    public String generateContext(String flashcards){
        String userText =  String.format("""
            Create a story using the following words: %s.
            Ensure the story is engaging and helps English learners understand the meaning of these words in context.
            The story should be approximately 100-200 words long. Wrap the placeholders with * symbols on both sides to make them bold.
        """, flashcards);

        Message userMessage = new UserMessage(userText.trim());

        String systemText = """
                You are a creative and helpful AI assistant specialized in generating engaging, educational, and context-rich stories for English learners. 
                Your task is to create unique and interesting stories using a list of predetermined words provided by the user. Each story should: 
                1. Use all the predetermined words in a natural and meaningful way.
                2. Incorporate the words into a coherent narrative with a clear beginning, middle, and end.
                3. Provide context that helps learners understand the meaning of the words through the story.
                4. Be appropriate for English learners, using simple grammar and vocabulary while maintaining an engaging tone.
                5. Vary the themes, settings, and characters for each story to keep the content fresh and interesting.
                The stories should be fun, imaginative, and useful for learners to practice vocabulary in context. 
                Avoid repeating the same story structure or themes unless requested.
        """;

        SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(systemText.trim());
        Message systemMessage = systemPromptTemplate.createMessage();
        Prompt prompt = new Prompt(List.of(userMessage, systemMessage));
        return chatModel.call(prompt).getResults().get(0).getOutput().getText();
    }
}
