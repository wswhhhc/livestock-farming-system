package com.livestock.service;

import com.livestock.config.AiProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class AiService {

    private static final Logger log = LoggerFactory.getLogger(AiService.class);

    private final AiProperties aiProperties;
    private final RestTemplate restTemplate;

    public AiService(AiProperties aiProperties, RestTemplate restTemplate) {
        this.aiProperties = aiProperties;
        this.restTemplate = restTemplate;
    }

    public boolean isEnabled() {
        return aiProperties.isEnabled();
    }

    /**
     * 根据批次上下文生成 AI 养殖建议
     *
     * @param context 批次上下文数据
     * @return AI 返回的建议文字，失败时返回 null
     */
    public String generateAdvice(Map<String, Object> context) {
        if (!aiProperties.isEnabled()) return null;

        String systemPrompt = """
                你是一位经验丰富的畜牧养殖顾问，擅长根据养殖数据提供专业、具体的养殖管理建议。
                请根据提供的批次信息，给出针对性的养殖建议。要求：
                1. 内容具体、可操作，结合数据进行分析
                2. 语气专业、务实，直接给出建议
                3. 长度控制在 100-200 字
                4. 只输出建议内容，不要前缀如"建议："或"养殖顾问："
                """;

        StringBuilder userContent = new StringBuilder();
        userContent.append("请分析以下批次并提供养殖管理建议：\n\n");

        appendIfPresent(userContent, context, "批次编号");
        appendIfPresent(userContent, context, "种类名称");
        appendIfPresent(userContent, context, "当前生长阶段");
        appendIfPresent(userContent, context, "当前存栏量");
        appendIfPresent(userContent, context, "入场日期");
        appendIfPresent(userContent, context, "已饲养天数");
        appendIfPresent(userContent, context, "预计出栏日");
        appendIfPresent(userContent, context, "剩余饲养天数");
        appendIfPresent(userContent, context, "生长周期");
        appendIfPresent(userContent, context, "出栏标准体重");
        appendIfPresent(userContent, context, "累计饲料成本");
        appendIfPresent(userContent, context, "累计人工成本");
        appendIfPresent(userContent, context, "累计防疫成本");
        appendIfPresent(userContent, context, "总成本");
        appendIfPresent(userContent, context, "预估收入");
        appendIfPresent(userContent, context, "预估利润");
        appendIfPresent(userContent, context, "当前月份");

        Map<String, Object> requestBody = Map.of(
                "model", aiProperties.getModel(),
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt.trim()),
                        Map.of("role", "user", "content", userContent.toString().trim())
                ),
                "temperature", 0.7,
                "max_tokens", 500
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(aiProperties.getApiKey());

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        try {
            log.info("正在调用 AI 生成建议，model={}", aiProperties.getModel());
            ResponseEntity<Map> response = restTemplate.exchange(
                    aiProperties.getApiUrl(),
                    HttpMethod.POST,
                    request,
                    Map.class
            );

            if (response.getBody() == null) {
                log.warn("AI 返回空响应");
                return null;
            }

            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
            if (choices == null || choices.isEmpty()) {
                log.warn("AI 返回 choices 为空");
                return null;
            }

            Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
            if (message == null) {
                log.warn("AI 返回 message 为空");
                return null;
            }

            String content = (String) message.get("content");
            log.info("AI 建议生成成功，长度={}字", content != null ? content.length() : 0);
            return content;

        } catch (Exception e) {
            log.error("调用 AI 接口失败: {}", e.getMessage());
            return null;
        }
    }

    private void appendIfPresent(StringBuilder sb, Map<String, Object> context, String key) {
        Object value = context.get(key);
        if (value != null) {
            sb.append(key).append("：").append(value).append("\n");
        }
    }
}
