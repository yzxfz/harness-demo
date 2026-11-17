package com.yzxfz.harness.demo;

import com.yzxfz.harness.core.tool.Tool;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 示例工具：字段映射（对应你正在做的 db-table-field-mapping skill）。
 * 严格 1:1、禁止幻觉——底座只负责调度，映射规则由工具内的业务逻辑保证。
 */
@Component
public class FieldMappingTool implements Tool {

    @Override
    public String name() {
        return "map_fields";
    }

    @Override
    public String description() {
        return "对两张表做字段映射，返回 1:1 映射结果，无对应字段标记 NOT_MAPPED";
    }

    @Override
    public String parameters() {
        return "{\"source\":\"源表名\",\"target\":\"目标表名\"}";
    }

    @Override
    public boolean readOnly() {
        return true;
    }

    @Override
    public String execute(Map<String, Object> args) {
        String source = String.valueOf(args.getOrDefault("source", ""));
        String target = String.valueOf(args.getOrDefault("target", ""));
        // 骨架示例：真实场景由映射引擎/LLM 推理填充
        return "映射 " + source + " -> " + target + "："
                + "{\"ziDuanList\":[{\"source\":\"id\",\"target\":\"ID\"},"
                + "{\"source\":\"remark\",\"target\":\"NOT_MAPPED\"}]}（示例数据）";
    }
}
