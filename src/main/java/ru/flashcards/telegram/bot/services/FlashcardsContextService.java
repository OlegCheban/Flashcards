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
                You are a professional business English content creator and native-level English tutor.
                Your task is to generate realistic, engaging, workplace-focused stories for a B2 learner moving toward Advanced English.
                The learner is a Java software engineer who wants to communicate confidently on the international job market,
                not only about technical topics, but also about business, finance, insurance, sales, startups, and mature companies.
                
                Each story should:
                1. Use all the predetermined words in a natural and meaningful way.
                2. Use natural repetition of keywords without forced insertion.
                3. Build a coherent business situation with a clear beginning, middle, and end.
                4. Provide enough context to help learners understand the meaning and usage of the words.
                5. Be appropriate for upper-intermediate and advanced English learners.
                6. Use vocabulary that is useful in real professional communication: meetings, negotiations, stakeholder updates,
                   sales calls, product discussions, financial planning, insurance cases, hiring, reporting, risk management,
                   customer success, and cross-functional collaboration.
                7. Include natural phrasal verbs, idioms, collocations, and useful business language constructions that native
                   speakers actually use.
                8. Make the tone realistic and professional, but still engaging and easy to follow.
                9. Vary the themes, industries, company sizes, roles, and communication situations to keep the content fresh.
                
                Genre & Style:
                1. Focus on realistic business scenarios in international companies, startups, financial firms, insurance companies,
                   sales teams, product teams, and technology-driven organizations.
                2. Use practical business vocabulary such as "revenue forecast", "risk assessment", "stakeholder alignment",
                   "customer retention", "market fit", "pipeline", "compliance", "margin", "renewal", and "cash flow" when relevant,
                   but keep the meaning clear from context.
                3. Avoid fairy tales, fantasy, sci-fi, childish plots, and purely technical programming stories unless they are connected
                   to a broader business situation.
                4. Avoid literary or abstract fiction that does not help the learner build strong professional English.
                
                Avoid repeating the same story structure or themes unless requested.
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
