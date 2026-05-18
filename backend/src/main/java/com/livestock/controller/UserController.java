package com.livestock.controller;

import com.livestock.common.JwtUtil;
import com.livestock.common.Result;
import com.livestock.entity.SysUser;
import com.livestock.service.SysUserService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final SysUserService userService;
    private final JwtUtil jwtUtil;

    public UserController(SysUserService userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping("/profile")
    public Result<Map<String, Object>> getProfile(@RequestHeader("Authorization") String authHeader) {
        Long userId = extractUserId(authHeader);
        SysUser user = userService.getById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }
        String roleName = user.getRole() != null && user.getRole() == 2 ? "管理员" : "养殖户";
        return Result.ok(Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "realName", user.getRealName() != null ? user.getRealName() : "",
                "phone", user.getPhone() != null ? user.getPhone() : "",
                "role", user.getRole(),
                "roleName", roleName,
                "createTime", user.getCreateTime()
        ));
    }

    @PutMapping("/profile")
    public Result<Void> updateProfile(@RequestHeader("Authorization") String authHeader,
                                       @RequestBody Map<String, String> body) {
        Long userId = extractUserId(authHeader);
        String realName = body.getOrDefault("realName", "");
        String phone = body.getOrDefault("phone", "");
        userService.updateProfile(userId, realName, phone);
        return Result.ok();
    }

    @PutMapping("/password")
    public Result<Void> changePassword(@RequestHeader("Authorization") String authHeader,
                                        @RequestBody Map<String, String> body) {
        Long userId = extractUserId(authHeader);
        String oldPassword = body.get("oldPassword");
        String newPassword = body.get("newPassword");
        if (oldPassword == null || oldPassword.isEmpty()) {
            return Result.error("请输入原密码");
        }
        if (newPassword == null || newPassword.isEmpty()) {
            return Result.error("请输入新密码");
        }
        try {
            userService.changePassword(userId, oldPassword, newPassword);
            return Result.ok();
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    private Long extractUserId(String authHeader) {
        String token = authHeader.substring(7);
        return jwtUtil.getUserId(token);
    }
}
