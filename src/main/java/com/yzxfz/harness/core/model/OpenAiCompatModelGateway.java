package com.yzxfz.harness.core.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.yzxfz.harness.api.dto.Message;
import com.yzxfz.harness.config.HarnessProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

/**
 * 基于 OpenAI 兼容 /chat/completions 协议的实现。
 * LiteLLM / vLLM / 通义 / DeepSeek / Moonshot 均可用此实现接入。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OpenAiCompatModelGateway implements ModelGateway {

    private final ObjectMapper objectMapper;

    @Override
    public String chat(List<Message> messages, HarnessProperties.Model config) {
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(config.getTimeoutMs()))
                .build();
        try {
            String body = buildRequestBody(messages, config);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(config.getBaseUrl().replaceAll("/$", "") + "/chat/completions"))
                    .timeout(Duration.ofMillis(config.getTimeoutMs()))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + config.getApiKey())
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = client.send(
                    request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() / 100 != 2) {
                log.error("模型调用失败 status={} body={}", response.statusCode(), response.body());
                throw new IllegalStateException("模型调用失败: HTTP " + response.statusCode());
            }
            JsonNode root = objectMapper.readTree(response.body());
            return root.path("choices").path(0).path("message").path("content").asText();
        } catch (Exception e) {
            log.error("模型调用异常", e);
            throw new IllegalStateException("模型调用异常", e);
        }
    }

    private String buildRequestBody(List<Message> messages, HarnessProperties.Model config)
            throws Exception {
        ObjectNode root = objectMapper.createObjectNode();
        root.put("model", config.getModelName());
        ArrayNode arr = root.putArray("messages");
        for (Message m : messages) {
            ObjectNode node = arr.addObject();
            node.put("role", m.getRole());
            node.put("content", m.getContent() == null ? "" : m.getContent());
            if (m.getName() != null) {
                node.put("name", m.getName());
            }
        }
        return objectMapper.writeValueAsString(root);
    }

    @Override
    public String provider() {
        return "openai-compatible";
    }
}
