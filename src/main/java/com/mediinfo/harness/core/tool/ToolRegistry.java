package com.mediinfo.harness.core.tool;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** 工具注册中心：Spring 容器里的所有 Tool Bean 自动注册 */
@Slf4j
@Component
public class ToolRegistry {

    private final Map<String, Tool> tools = new LinkedHashMap<>();

    public ToolRegistry(List<Tool> toolBeans) {
        toolBeans.forEach(tool -> {
            tools.put(tool.name(), tool);
            log.info("注册工具: {}", tool.name());
        });
    }

    public Optional<Tool> get(String name) {
        return Optional.ofNullable(tools.get(name));
    }

    public Map<String, Tool> all() {
        return tools;
    }

    /** 生成工具清单，注入到模型 system prompt */
    public String manifestFor(String appCode) {
        StringBuilder sb = new StringBuilder("可用工具：\n");
        tools.values().forEach(t -> sb.append("- ")
                .append(t.name()).append(": ").append(t.description())
                .append(" | 参数: ").append(t.parameters()).append("\n"));
        return sb.toString();
    }
}
