package com.mediinfo.harness.core.task;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mediinfo.harness.api.dto.HarnessRequest;
import com.mediinfo.harness.api.dto.HarnessResponse;
import com.mediinfo.harness.api.dto.Message;
import com.mediinfo.harness.config.HarnessProperties;
import com.mediinfo.harness.core.context.ContextManager;
import com.mediinfo.harness.core.model.ModelGateway;
import com.mediinfo.harness.core.security.SecurityGuard;
import com.mediinfo.harness.core.session.SessionManager;
import com.mediinfo.harness.core.tool.Tool;
import com.mediinfo.harness.core.tool.ToolInvocation;
import com.mediinfo.harness.core.tool.ToolRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 能力⑥：任务执行（Agent 循环）。
 * 组装上下文 -> 调模型 -> 解析工具指令 -> 安全校验 -> 执行工具 -> 回填 -> 循环，
 * 直到模型给出最终答案或达到最大步数。
 */
@Slf4j
@Component("harnessTaskExecutor")
@RequiredArgsConstructor
public class TaskExecutor {

    private final ContextManager contextManager;
    private final ModelGateway modelGateway;
    private final ToolRegistry toolRegistry;
    private final SessionManager sessionManager;
    private final SecurityGuard securityGuard;
    private final HarnessProperties props;
    private final ObjectMapper objectMapper;

    public HarnessResponse execute(HarnessRequest request) {
        long start = System.currentTimeMillis();
        List<Message> messages = contextManager.build(request);
        List<String> calledTools = new ArrayList<>();
        int maxSteps = props.getAgent().getMaxSteps();
        String finishReason = "STOP";
        String answer = "";

        for (int step = 0; step < maxSteps; step++) {
            String output = modelGateway.chat(messages, props.getModel());

            ToolInvocation invocation = parseInvocation(output);
            if (!invocation.valid()) {
                // 无工具指令，视为最终答案
                answer = output;
                messages.add(Message.assistant(output));
                break;
            }

            Optional<Tool> toolOpt = toolRegistry.get(invocation.getTool());
            if (toolOpt.isEmpty()) {
                messages.add(Message.user("工具不存在: " + invocation.getTool() + "，请重新选择工具。"));
                continue;
            }

            Tool tool = toolOpt.get();
            Optional<String> deny = securityGuard.checkTool(tool, false);
            if (deny.isPresent()) {
                messages.add(Message.user("工具调用被拒绝: " + deny.get()));
                continue;
            }

            String result;
            try {
                result = tool.execute(invocation.getArgs());
            } catch (Exception e) {
                log.error("工具执行异常 tool={}", tool.name(), e);
                result = "工具执行异常: " + e.getMessage();
            }
            calledTools.add(tool.name());
            messages.add(Message.assistant(output));
            messages.add(Message.tool(tool.name(), result));

            if (step == maxSteps - 1) {
                finishReason = "MAX_STEPS";
                answer = "达到最大执行步数，最后一轮工具结果：" + result;
            }
        }

        // 落会话：把用户输入与最终答案写入，供下一轮续接
        if (request.getSessionId() != null) {
            sessionManager.append(request.getSessionId(), Message.user(request.getUserInput()));
            sessionManager.append(request.getSessionId(), Message.assistant(answer));
        }

        return HarnessResponse.builder()
                .sessionId(request.getSessionId())
                .answer(answer)
                .finishReason(finishReason)
                .steps(calledTools.size())
                .toolCalls(calledTools)
                .elapsedMs(System.currentTimeMillis() - start)
                .build();
    }

    /** 解析形如 {"tool":"xxx","args":{...}} 的模型输出；非 JSON 或不含 tool 字段则返回空 */
    private ToolInvocation parseInvocation(String output) {
        ToolInvocation empty = new ToolInvocation();
        if (output == null) {
            return empty;
        }
        String text = output.trim();
        // 兼容模型包裹 markdown 代码块的情况
        if (text.startsWith("```")) {
            text = text.replaceAll("^```(json)?", "").replaceAll("```$", "").trim();
        }
        if (!text.startsWith("{")) {
            return empty;
        }
        try {
            JsonNode node = objectMapper.readTree(text);
            if (!node.has("tool")) {
                return empty;
            }
            ToolInvocation invocation = new ToolInvocation();
            invocation.setTool(node.path("tool").asText());
            JsonNode args = node.path("args");
            if (args.isObject()) {
                args.fields().forEachRemaining(e ->
                        invocation.getArgs().put(e.getKey(), objectMapper.convertValue(e.getValue(), Object.class)));
            }
            return invocation;
        } catch (Exception e) {
            log.debug("输出非工具调用指令，按最终答案处理: {}", output);
            return empty;
        }
    }
}
