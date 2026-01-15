package ru.dreader.dreaderllmparser.service;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.log4j.Log4j2;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import ru.dreader.dreaderllmparser.dto.ArticleAiAnalysis;
import ru.dreader.dreaderllmparser.utils.LLMJsonCleaner;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Log4j2
@Service
public class ArticleAiServiceImpl implements ArticleAiService {

    private final ChatClient chatClient;

    public ArticleAiServiceImpl(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    @Override
    public ArticleAiAnalysis analyze(String title, String bodyPlainText, List<String> rawTags, Locale locale) {
        log.info("LLM call started");
        String prompt = buildPrompt(title, bodyPlainText, rawTags, locale);

        Instant now = Instant.now();

        String response = chatClient.prompt(prompt)
                .call()
                .content();

        Instant then = Instant.now();

        log.info("LLM call duration: {} s", (then.toEpochMilli() - now.toEpochMilli()) / 1000.0);

        return parseResponse(response);
    }

    private String buildPrompt(String title, String body, List<String> tags, Locale locale) {
        String language = (locale.getLanguage().equals("ru")) ? "русском" : "английском";
        String joinedTags = (tags == null || tags.isEmpty()) ? "нет" : String.join(", ", tags);

        return """
                Ты — помощник по аналитике новостных статей.
                Работай строго по инструкции и верни ответ в JSON.

                Язык ответа: %s.

                Задача:
                1) Игнорируй рекламные вставки, ссылки на подписку, баннеры, промо и несодержательные вставки.
                2) Сделай краткий пересказ статьи (summary, 1 предложение).
                3) Сформулируй 5–15 тезисов (короткие пункты, bullet points).
                4) Определи одну главную категорию статьи из перечня: "технологии", "экономика", "политика", "наука", "культура", "спорт".
                5) Опционально добавь до 3 дополнительных категорий (например: "ИИ", "космос", "США", "Европа", "Россия", "стартапы", "бизнес", "финансы").
                6) Не придумывай факты, которых нет в тексте.

                Исходные данные:
                Заголовок: "%s"

                Теги со страницы: %s

                Текст статьи:
                \"\"\"%s\"\"\"

                Формат ответа строго в виде JSON (без пояснений и форматирования):

                {
                  "summary": "строка",
                  "bulletPoints": ["строка1", "строка2", "..."],
                  "mainCategory": "строка",
                  "secondaryCategories": ["строка1", "строка2"]
                }
                """.formatted(language, title, joinedTags, body);
    }

    private ArticleAiAnalysis parseResponse(String json) {
        try {
            JsonNode node = LLMJsonCleaner.parse(json);

            String summary = node.path("summary").asText("");
            List<String> bullets = new ArrayList<>();
            node.path("bulletPoints").forEach(n -> bullets.add(n.asText()));

            String mainCategory = node.path("mainCategory").asText("");
            List<String> secondary = new ArrayList<>();
            node.path("secondaryCategories").forEach(n -> secondary.add(n.asText()));

            return new ArticleAiAnalysis(summary, bullets, mainCategory, secondary);
        } catch (Exception e) {
            // fallback: хотя бы summary = обрезанный оригинальный текст
            return new ArticleAiAnalysis(
                    "",
                    List.of(),
                    "",
                    List.of()
            );
        }
    }
}
