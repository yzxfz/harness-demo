package com.yzxfz.harness.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 统一消息结构，对齐 OpenAI 消息协议 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Message {
    private String role;      // system / user / assistant / tool
    private String content;
    private String name;      // role=tool 时为工具名

    public static Message of(String role, String content) {
        return Message.builder().role(role).content(content).build();
    }

    public static Message system(String content) {
        return of("system", content);
    }

    public static Message user(String content) {
        return of("user", content);
    }

    public static Message assistant(String content) {
        return of("assistant", content);
    }

    public static Message tool(String name, String content) {
        return Message.builder().role("tool").name(name).content(content).build();
    }
}
