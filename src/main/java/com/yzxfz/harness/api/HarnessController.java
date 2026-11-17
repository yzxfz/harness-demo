package com.yzxfz.harness.api;

import com.yzxfz.harness.api.dto.HarnessRequest;
import com.yzxfz.harness.api.dto.HarnessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/** REST 入口：所有业务应用（BI/报表/智能体）统一调用此接口 */
@RestController
@RequestMapping("/api/harness")
@RequiredArgsConstructor
public class HarnessController {

    private final Harness harness;

    @PostMapping("/run")
    public HarnessResponse run(@RequestBody HarnessRequest request) {
        return harness.run(request);
    }
}
