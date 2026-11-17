package com.yzxfz.harness.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/** 统一出参，带可观测字段 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HarnessResponse {
    private String sessionId;
    private String answer;
    /** STOP=正常结束 / MAX_STEPS=达最大步数 / BLOCKED=被安全策略拦截 / ERROR=异常 */
    private String finishReason;
    private int steps;
    private List<String> toolCalls;
    private long promptTokens;
    private long completionTokens;
    private long elapsedMs;

    public static HarnessResponse blocked(String sessionId, String reason) {
        return HarnessResponse.builder()
                .sessionId(sessionId)
                .finishReason("BLOCKED")
                .answer(reason)
                .toolCalls(new ArrayList<>())
                .build();
    }
}
