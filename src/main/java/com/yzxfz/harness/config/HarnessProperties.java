package com.yzxfz.harness.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/** 全部能力通过配置驱动，业务代码零硬编码 */
@Data
@ConfigurationProperties(prefix = "harness")
public class HarnessProperties {

    private Model model = new Model();
    private Context context = new Context();
    private Agent agent = new Agent();
    private Security security = new Security();

    @Data
    public static class Model {
        private String baseUrl;
        private String apiKey;
        private String modelName;
        private int timeoutMs = 30000;
    }

    @Data
    public static class Context {
        private int maxHistoryRounds = 10;
        private int maxContextChars = 12000;
    }

    @Data
    public static class Agent {
        private int maxSteps = 5;
    }

    @Data
    public static class Security {
        private boolean enabled = true;
        private List<String> blockedKeywords = new ArrayList<>();
    }
}
