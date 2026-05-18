package com.livestock.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.livestock.entity.SysUser;

public interface SysUserService extends IService<SysUser> {
    SysUser login(String username, String password);
    void updateProfile(Long userId, String realName, String phone);
    void changePassword(Long userId, String oldPassword, String newPassword);
}
