package com.yzxfz.harness.core.session;

import com.yzxfz.harness.api.dto.Message;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 能力③：会话管理。
 * 骨架版用内存实现，生产可替换为 Redis 实现（实现同一接口即可）。
 */
@Component
public class SessionManager {

    private final Map<String, List<Message>> sessions = new ConcurrentHashMap<>();

    public String newSession() {
        String id = UUID.randomUUID().toString();
        sessions.put(id, Collections.synchronizedList(new ArrayList<>()));
        return id;
    }

    public boolean exists(String sessionId) {
        return sessionId != null && sessions.containsKey(sessionId);
    }

    /** 尊重外部传入的会话 ID（如持久化在业务库的 ID），首次出现时登记而非替换 */
    public void ensure(String sessionId) {
        if (sessionId != null && !sessionId.isBlank()) {
            sessions.computeIfAbsent(sessionId, k -> Collections.synchronizedList(new ArrayList<>()));
        }
    }

    public void append(String sessionId, Message message) {
        sessions.computeIfAbsent(sessionId, k -> Collections.synchronizedList(new ArrayList<>()))
                .add(message);
    }

    /** 取最近 N 轮（按消息条数近似） */
    public List<Message> history(String sessionId, int maxRounds) {
        List<Message> all = sessions.getOrDefault(sessionId, List.of());
        synchronized (all) {
            int from = Math.max(0, all.size() - maxRounds * 2);
            return new ArrayList<>(all.subList(from, all.size()));
        }
    }

    public void clear(String sessionId) {
        sessions.remove(sessionId);
    }
}
