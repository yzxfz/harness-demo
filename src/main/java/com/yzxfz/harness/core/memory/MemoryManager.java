package com.yzxfz.harness.core.memory;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 能力④：记忆管理（区别于会话：会话是短期上下文，记忆是跨会话的长期事实）。
 * 骨架版内存实现，生产可替换为 Mem0 / Zep / 向量库实现。
 */
@Component
public class MemoryManager {

    private final Map<String, List<String>> longTerm = new ConcurrentHashMap<>();

    /** 召回用户事实，注入上下文 */
    public List<String> recall(String userId) {
        if (userId == null) {
            return List.of();
        }
        return List.copyOf(longTerm.getOrDefault(userId, List.of()));
    }

    /** 写入长期事实 */
    public void remember(String userId, String fact) {
        longTerm.computeIfAbsent(userId, k -> new ArrayList<>()).add(fact);
    }

    public void forget(String userId) {
        longTerm.remove(userId);
    }
}
