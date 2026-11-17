package com.yzxfz.harness.core.security;

import com.yzxfz.harness.api.dto.HarnessRequest;
import com.yzxfz.harness.config.HarnessProperties;
import com.yzxfz.harness.core.tool.Tool;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Optional;

/**
 * 能力⑦：安全控制。
 * 输入侧：关键词 / 注入特征拦截；工具侧：只读与白名单校验。
 * 生产可再接入 OPA / Presidio 做细粒度策略与脱敏。
 */
@Component
@RequiredArgsConstructor
public class SecurityGuard {

    private final HarnessProperties props;

    public Optional<String> checkInput(HarnessRequest request) {
        if (!props.getSecurity().isEnabled() || request.getUserInput() == null) {
            return Optional.empty();
        }
        String text = request.getUserInput().toUpperCase(Locale.ROOT);
        for (String keyword : props.getSecurity().getBlockedKeywords()) {
            if (text.contains(keyword.toUpperCase(Locale.ROOT))) {
                return Optional.of("输入包含被禁止的内容: " + keyword);
            }
        }
        // 简易提示词注入特征检测
        if (text.contains("IGNORE ALL PREVIOUS") || text.contains("忽略之前所有指令")) {
            return Optional.of("检测到提示词注入特征");
        }
        return Optional.empty();
    }

    public Optional<String> checkTool(Tool tool, boolean dryRun) {
        if (dryRun && !tool.readOnly()) {
            return Optional.of("只读模式下禁止调用写工具: " + tool.name());
        }
        return Optional.empty();
    }
}
