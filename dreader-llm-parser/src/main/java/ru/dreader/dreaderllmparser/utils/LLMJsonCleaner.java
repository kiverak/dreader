package ru.dreader.dreaderllmparser.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class LLMJsonCleaner {

    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * Полная очистка JSON-строки, полученной от LLM.
     */
    public static String clean(String raw) {
        if (raw == null) {
            throw new IllegalArgumentException("JSON string is null");
        }

        String json = raw;

        // 1. Удаляем BOM
        json = json.replace("\uFEFF", "");

        // 2. Удаляем Markdown-блоки ```json ... ```
        json = json.replaceAll("^```[a-zA-Z]*", "");
        json = json.replaceAll("```$", "");
        json = json.replace("```", "");

        // 3. Удаляем управляющие символы (кроме \n \r \t)
        json = json.replaceAll("[\\u0000-\\u001F&&[^\\n\\r\\t]]", "");

        // 4. Убираем неразрывные пробелы и прочие спецсимволы
        json = json.replace("\u00A0", " ");

        // 5. Трим
        json = json.trim();

        return json;
    }

    /**
     * Чистит и парсит JSON в JsonNode.
     */
    public static JsonNode parse(String raw) {
        try {
            String cleaned = clean(raw);
            return mapper.readTree(cleaned);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse LLM JSON. Raw: " + raw, e);
        }
    }
}
