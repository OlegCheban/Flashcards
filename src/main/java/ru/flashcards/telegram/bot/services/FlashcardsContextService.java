package ru.flashcards.telegram.bot.services;

import java.text.MessageFormat;
import java.util.List;

import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FlashcardsContextService {

    /** A story of 50-150 words plus low-effort reasoning. */
    private static final int CONTEXT_MAX_TOKENS = 700;
    /** A multi-section report with examples plus low-effort reasoning. */
    private static final int REPORT_MAX_TOKENS = 1500;

    private final ChatModel chatModel;

    public String generateContext(String flashcards){
        String userText =  String.format("""
            Create a story using the following words: %s.
            Ensure the story is engaging and helps English learners understand the meaning of these words in context.
            The story should be approximately 50-150 words long. Wrap the placeholders with * symbols on both sides to make them bold.
        """, flashcards);

        String systemText = """
                You are a native-level business English tutor writing short workplace stories for a B2 learner
                (a Java engineer) moving toward Advanced English.

                Each story:
                - uses all given words naturally, with a clear beginning, middle and end, so their meaning is clear from context;
                - is a realistic business situation: meetings, negotiations, sales, finance, insurance, hiring, startups,
                  product or risk management;
                - includes natural phrasal verbs, idioms and collocations native speakers really use;
                - is professional but engaging; vary industries, roles and situations.

                Avoid fairy tales, fantasy, sci-fi, abstract fiction and purely technical programming plots.
        """;

        return generate(userText, systemText, CONTEXT_MAX_TOKENS);
    }

    public String generateFlashcardReport(String flashcard){
        String userText = MessageFormat.format("""
            Create a comprehensive report about the word "{0}" for English learners.
            
            IMPORTANT: Format the output as plain text. The only markdown allowed is **double asterisks** around
            the word "{0}" to make it bold; do not use any other markdown symbols (no ##, single *, _, etc.).
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
            
            Remember: Use clear, simple language. No other markdown formatting. Use emojis and simple bullet points for visual appeal.
            """, flashcard);

        String systemText = """
            You are a professional native English tutor. Create reports that are:
            1. Easy to read in messaging apps
            2. Use simple formatting (no markdown symbols except **bold** for the studied word)
            3. Include emojis for visual appeal
            4. Suitable for intermediate English learners
            5. Well-structured with clear sections
            """;

        return generate(userText, systemText, REPORT_MAX_TOKENS);
    }

    /**
     * The system message goes first: models treat instructions that follow the user message as less authoritative.
     * The token cap includes reasoning tokens and keeps a single request within the Groq tokens-per-minute limit.
     */
    private String generate(String userText, String systemText, int maxTokens) {
        Prompt prompt = new Prompt(
                List.of(new SystemMessage(systemText.trim()), new UserMessage(userText.trim())),
                ChatOptions.builder().maxTokens(maxTokens).build());
        ChatResponse response = chatModel.call(prompt);
        if (response == null || response.getResult() == null || response.getResult().getOutput().getText() == null) {
            throw new IllegalStateException("AI model returned an empty response");
        }
        return response.getResult().getOutput().getText();
    }
}
