package com.mediinfo.harness.demo;

import com.mediinfo.harness.core.tool.Tool;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 示例工具：查询 BI 元数据。
 * 真实场景可在此查 information_schema / 数据字典，返回表结构给模型。
 */
@Component
public class BiMetadataTool implements Tool {

    @Override
    public String name() {
        return "query_metadata";
    }

    @Override
    public String description() {
        return "查询业务表结构元数据，用于生成正确的 SQL";
    }

    @Override
    public String parameters() {
        return "{\"table\":\"表名，如 GY_BINGRENXX\"}";
    }

    @Override
    public String execute(Map<String, Object> args) {
        String table = String.valueOf(args.getOrDefault("table", ""));
        // 骨架示例：生产环境替换为真实元数据查询
        return "表 " + table + " 字段：id, bingrenxm, chuangjiansj, zufeibz（示例数据）";
    }
}
