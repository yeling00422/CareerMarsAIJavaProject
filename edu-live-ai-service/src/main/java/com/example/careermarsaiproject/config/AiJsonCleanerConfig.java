package com.example.careermarsaiproject.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AiJsonCleanerConfig {

    // 全局通用的 ObjectMapper，配置最大容错
    public static final ObjectMapper OBJECT_MAPPER = JsonMapper.builder()
            .configure(com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true)
            .configure(com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_SINGLE_QUOTES, true)
            .configure(com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_TRAILING_COMMA, true)
            .configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false)
            .configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES, false)
            .addModule(new SimpleModule().addSerializer(Long.class, ToStringSerializer.instance))
            .build();

    // 匹配 JSON 数组的正则
    private static final Pattern JSON_ARRAY_PATTERN = Pattern.compile("\\[\\s*\\{[\\s\\S]*?}\\s*\\]", Pattern.DOTALL);
    // 匹配 JSON 对象的正则
    private static final Pattern JSON_OBJECT_PATTERN = Pattern.compile("\\{\\s*[\"\\w]+[\\s\\S]*?}\\s*$", Pattern.DOTALL);

    /**
     * 通用 JSON 提取方法 - 自动识别对象/数组
     */
    public static Optional<String> extractJson(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return Optional.empty();
        }

        String text = raw.trim()
                .replace("\\n", "")
                .replace("\\r", "")
                .replace("\\t", "")
                .replace("\\\"", "\"");

        // 1. 优先处理 Markdown 代码块
        Optional<String> markdownJson = extractMarkdownJson(text);
        if (markdownJson.isPresent()) {
            return validateJson(markdownJson.get());
        }

        // 2. 尝试提取 JSON 数组
        Optional<String> arrayJson = extractJsonArray(text);
        if (arrayJson.isPresent()) {
            return validateJson(arrayJson.get());
        }

        // 3. 尝试提取 JSON 对象
        Optional<String> objectJson = extractJsonObject(text);
        if (objectJson.isPresent()) {
            return validateJson(objectJson.get());
        }

        // 4. 最后尝试直接校验原始文本
        return validateJson(text);
    }

    /**
     * 提取 Markdown 代码块中的 JSON
     */
    private static Optional<String> extractMarkdownJson(String text) {
        Pattern markdownPattern = Pattern.compile("```(?:json)?\\s*([\\s\\S]*?)\\s*```", Pattern.DOTALL);
        Matcher matcher = markdownPattern.matcher(text);
        if (matcher.find()) {
            return Optional.of(matcher.group(1).trim());
        }
        return Optional.empty();
    }

    /**
     * 提取 JSON 数组
     */
    private static Optional<String> extractJsonArray(String text) {
        Matcher matcher = JSON_ARRAY_PATTERN.matcher(text);
        if (matcher.find()) {
            return Optional.of(matcher.group().trim());
        }
        return Optional.empty();
    }

    /**
     * 提取 JSON 对象
     */
    private static Optional<String> extractJsonObject(String text) {
        Matcher matcher = JSON_OBJECT_PATTERN.matcher(text);
        if (matcher.find()) {
            return Optional.of(matcher.group().trim());
        }
        return Optional.empty();
    }

    /**
     * 校验 JSON 合法性
     */
    private static Optional<String> validateJson(String json) {
        try {
            JsonNode node = OBJECT_MAPPER.readTree(json);
            if (node != null && (node.isObject() || node.isArray())) {
                // 重新序列化保证格式标准
                return Optional.of(OBJECT_MAPPER.writeValueAsString(node));
            }
        } catch (Exception e) {
            // 静默失败，返回空
        }
        return Optional.empty();
    }

    /**
     * 通用反序列化方法 - 带容错
     */
    public static <T> Optional<T> parseJson(String jsonStr, Class<T> clazz) {
        try {
            return Optional.ofNullable(OBJECT_MAPPER.readValue(jsonStr, clazz));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * 通用反序列化方法 - 支持泛型
     */
    public static <T> Optional<T> parseJson(String jsonStr, com.fasterxml.jackson.core.type.TypeReference<T> typeReference) {
        try {
            return Optional.ofNullable(OBJECT_MAPPER.readValue(jsonStr, typeReference));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public static String cleanJson(String raw) {
        if (raw == null) return "";
        // 去掉AI乱加的 markdown、文字、符号
        raw = raw.replaceAll("```json", "").replaceAll("```", "").trim();
        // 只保留 { ... } 标准JSON结构
        int start = raw.indexOf("{");
        int end = raw.lastIndexOf("}");
        if (start != -1 && end > start) {
            raw = raw.substring(start, end + 1);
        }
        return raw;
    }
}