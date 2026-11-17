package com.yzxfz.harness.core.tool;

import java.util.Map;

/**
 * 能力⑤：工具调用。
 * 所有业务能力（查元数据、生成 SQL、生成 KTR）都实现此接口注册进底座，
 * 由底座统一负责描述暴露、参数校验、权限与安全控制、结果回填。
 */
public interface Tool {

    /** 工具唯一名，模型据此调用 */
    String name();

    /** 工具功能描述，会注入模型上下文 */
    String description();

    /** 参数说明（简单 JSON Schema 文本），引导模型正确填参 */
    String parameters();

    /** 是否在只读模式可用；危险工具可被安全策略拦截 */
    default boolean readOnly() {
        return true;
    }

    /** 执行工具，入参为模型解析出的参数 map */
    String execute(Map<String, Object> args);
}
