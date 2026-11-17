package com.mediinfo.harness.api;

import com.mediinfo.harness.api.dto.HarnessRequest;
import com.mediinfo.harness.api.dto.HarnessResponse;
import com.mediinfo.harness.core.security.SecurityGuard;
import com.mediinfo.harness.core.session.SessionManager;
import com.mediinfo.harness.core.task.TaskExecutor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 统一运行接口——业务应用唯一需要依赖的入口。
 * 所有 AI 基础设施能力（模型/上下文/会话/记忆/工具/任务/安全）都藏在门面之后，
 * 业务侧不再各自重复开发。
 */
@Service
@RequiredArgsConstructor
public class Harness {

    private final SessionManager sessionManager;
    private final SecurityGuard securityGuard;
    private final TaskExecutor taskExecutor;

    public HarnessResponse run(HarnessRequest request) {
        // 会话：无 sessionId 则新建；有则尊重外部传入的 ID（首次出现时登记）
        if (request.getSessionId() == null || request.getSessionId().isBlank()) {
            request.setSessionId(sessionManager.newSession());
        } else {
            sessionManager.ensure(request.getSessionId());
        }

        // 安全：输入拦截
        var deny = securityGuard.checkInput(request);
        if (deny.isPresent()) {
            return HarnessResponse.blocked(request.getSessionId(), deny.get());
        }

        // 任务执行：Agent 循环
        return taskExecutor.execute(request);
    }
}
