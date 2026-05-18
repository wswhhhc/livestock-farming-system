package com.livestock.controller;

import com.livestock.common.JwtUtil;
import com.livestock.common.Result;
import com.livestock.dto.LoginRequest;
import com.livestock.entity.SysUser;
import com.livestock.service.SysUserService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final SysUserService userService;
    private final JwtUtil jwtUtil;

    public AuthController(SysUserService userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody LoginRequest request) {
        SysUser user = userService.login(request.getUsername(), request.getPassword());
        if (user == null) {
            return Result.error("用户名或密码错误");
        }
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());
        return Result.ok(Map.of(
                "token", token,
                "user", Map.of(
                        "id", user.getId(),
                        "username", user.getUsername(),
                        "realName", user.getRealName() != null ? user.getRealName() : user.getUsername(),
                        "role", user.getRole()
                )
        ));
    }

    @GetMapping("/me")
    public Result<Map<String, Object>> me(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Result.error("未登录");
        }
        String token = authHeader.substring(7);
        Long userId = jwtUtil.getUserId(token);
        SysUser user = userService.getById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }
        return Result.ok(Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "realName", user.getRealName() != null ? user.getRealName() : user.getUsername(),
                "role", user.getRole()
        ));
    }
}
