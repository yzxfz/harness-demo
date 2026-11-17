package com.yzxfz.harness.core.tool;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.LinkedHashMap;
import java.util.Map;

/** 模型输出的工具调用指令解析结果 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ToolInvocation {
    private String tool;
    private Map<String, Object> args = new LinkedHashMap<>();

    public boolean valid() {
        return tool != null && !tool.isBlank();
    }
}
