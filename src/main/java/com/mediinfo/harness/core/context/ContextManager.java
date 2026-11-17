package com.mediinfo.harness.core.context;

import com.mediinfo.harness.api.dto.HarnessRequest;
import com.mediinfo.harness.api.dto.Message;
import com.mediinfo.harness.config.HarnessProperties;
import com.mediinfo.harness.core.memory.MemoryManager;
import com.mediinfo.harness.core.session.SessionManager;
import com.mediinfo.harness.core.tool.ToolRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 能力②：上下文管理。
 * 负责拼装 system prompt + 长期记忆 + 会话历史 + 当前输入，并做 token 预算裁剪。
 */
@Component
@RequiredArgsConstructor
public class ContextManager {

    private final HarnessProperties props;
    private final SessionManager sessions;
    private final MemoryManager memory;
    private final ToolRegistry toolRegistry;

    public List<Message> build(HarnessRequest request) {
        List<Message> messages = new ArrayList<>();

        String system = "你是一个企业助手。可以调用工具完成任务。\n"
                + "如需调用工具，只输出一行 JSON：{\"tool\":\"工具名\",\"args\":{...}}\n"
                + "如果不需要工具，直接给出最终答案文本。\n"
                + toolRegistry.manifestFor(request.getAppCode());
        messages.add(Message.system(system));

        List<String> facts = memory.recall(request.getUserId());
        if (!facts.isEmpty()) {
            messages.add(Message.system("关于该用户的长期记忆：" + String.join("；", facts)));
        }

        if (request.getSessionId() != null) {
            messages.addAll(sessions.history(
                    request.getSessionId(), props.getContext().getMaxHistoryRounds()));
        }
        messages.add(Message.user(request.getUserInput()));
        return trim(messages);
    }

    /** 简单的字符预算裁剪：保留 system 与最近输入，丢弃过长的中间历史 */
    private List<Message> trim(List<Message> messages) {
        int max = props.getContext().getMaxContextChars();
        int total = messages.stream().mapToInt(m -> m.getContent() == null ? 0 : m.getContent().length()).sum();
        if (total <= max) {
            return messages;
        }
        List<Message> kept = new ArrayList<>();
        kept.add(messages.get(0)); // system 必留
        int used = kept.get(0).getContent().length();
        for (int i = messages.size() - 1; i >= 1; i--) {
            Message m = messages.get(i);
            int len = m.getContent() == null ? 0 : m.getContent().length();
            if (used + len > max) {
                break;
            }
            kept.add(m);
            used += len;
        }
        // 反转中段以保持时序：system 固定在首位
        List<Message> tail = new ArrayList<>(kept.subList(1, kept.size()));
        java.util.Collections.reverse(tail);
        List<Message> result = new ArrayList<>();
        result.add(kept.get(0));
        result.addAll(tail);
        return result;
    }
}
