package com.livestock.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.livestock.entity.SysUser;
import com.livestock.mapper.SysUserMapper;
import com.livestock.service.SysUserService;
import org.springframework.stereotype.Service;

@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    @Override
    public SysUser login(String username, String password) {
        SysUser user = lambdaQuery().eq(SysUser::getUsername, username).one();
        if (user != null && password.equals(user.getPassword())) {
            return user;
        }
        return null;
    }

    @Override
    public void updateProfile(Long userId, String realName, String phone) {
        lambdaUpdate()
                .eq(SysUser::getId, userId)
                .set(SysUser::getRealName, realName)
                .set(SysUser::getPhone, phone)
                .update();
    }

    @Override
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        SysUser user = getById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        if (!oldPassword.equals(user.getPassword())) {
            throw new RuntimeException("原密码错误");
        }
        lambdaUpdate()
                .eq(SysUser::getId, userId)
                .set(SysUser::getPassword, newPassword)
                .update();
    }
}
