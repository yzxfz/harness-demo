package com.yzxfz.harness.core.model;

import com.yzxfz.harness.api.dto.Message;
import com.yzxfz.harness.config.HarnessProperties;

import java.util.List;

/**
 * 能力①：模型接入。
 * 统一抽象，屏蔽 OpenAI / 通义 / DeepSeek / vLLM 等协议差异。
 * 换模型只需替换实现，业务层无感。
 */
public interface ModelGateway {

    /** 同步对话，返回 assistant 文本 */
    String chat(List<Message> messages, HarnessProperties.Model config);

    /** 供应商标识，用于计量与审计 */
    String provider();
}
