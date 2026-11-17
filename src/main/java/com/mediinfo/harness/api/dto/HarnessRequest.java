package com.mediinfo.harness.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/** 业务应用调用 AI 能力的统一入参——业务侧只需要认识这一个对象 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HarnessRequest {
    /** 业务应用标识，用于工具白名单、计量与审计隔离 */
    private String appCode;
    /** 终端用户标识，用于会话与记忆隔离 */
    private String userId;
    /** 传入则续接会话，为空则新建 */
    private String sessionId;
    /** 用户输入 / 任务描述 */
    private String userInput;
    /** 业务自定义上下文（如当前报表 ID、患者脱敏 ID），可选 */
    private Map<String, Object> variables;
}
