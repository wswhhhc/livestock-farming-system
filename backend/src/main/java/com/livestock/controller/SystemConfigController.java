package com.livestock.controller;

import com.livestock.common.Result;
import com.livestock.entity.SystemConfig;
import com.livestock.service.SystemConfigService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/system")
public class SystemConfigController {

    private final SystemConfigService configService;

    public SystemConfigController(SystemConfigService configService) {
        this.configService = configService;
    }

    @GetMapping("/config")
    public Result<List<SystemConfig>> getConfigs() {
        return Result.ok(configService.list());
    }

    @PutMapping("/config")
    public Result<Void> updateConfig(@RequestBody Map<String, String> body) {
        Long id = Long.parseLong(body.get("id"));
        String value = body.get("configValue");
        configService.lambdaUpdate()
                .eq(SystemConfig::getId, id)
                .set(SystemConfig::getConfigValue, value)
                .update();
        return Result.ok();
    }
}
