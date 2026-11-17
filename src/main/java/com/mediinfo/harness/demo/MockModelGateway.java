package com.mediinfo.harness.demo;

import com.mediinfo.harness.api.dto.Message;
import com.mediinfo.harness.config.HarnessProperties;
import com.mediinfo.harness.core.model.ModelGateway;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 离线演示用的 Mock 模型：不调用任何外部 API，按轮次模拟
 * 「先调工具 -> 再给答案」的 Agent 循环，方便无 Key 环境直接跑通。
 * 生产环境删除本类（或去掉 @Primary）即自动切换到真实网关。
 */
@Primary
@Component
public class MockModelGateway implements ModelGateway {

    private int turn = 0;

    @Override
    public String chat(List<Message> messages, HarnessProperties.Model config) {
        // 每次请求从用户最后一条输入判断：第一轮调工具，之后给答案
        String lastUser = "";
        for (int i = messages.size() - 1; i >= 0; i--) {
            if ("user".equals(messages.get(i).getRole())) {
                lastUser = messages.get(i).getContent();
                break;
            }
        }
        turn++;
        if (turn % 2 == 1) {
            return "{\"tool\":\"query_metadata\",\"args\":{\"table\":\"GY_BINGRENXX\"}}";
        }
        return "已完成分析：" + lastUser + "（这是 Mock 模型的演示答案）";
    }

    @Override
    public String provider() {
        return "mock";
    }
}
