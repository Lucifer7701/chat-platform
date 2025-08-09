package com.dating.service;

import com.dating.entity.UserAuth;
import com.dating.mapper.UserAuthMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@Slf4j
public class UserAuthService {

    @Autowired
    private UserAuthMapper userAuthMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 创建用户认证信息
     */
    public boolean createUserAuth(Long userId, String phone, String password) {
        try {
            UserAuth userAuth = new UserAuth();
            userAuth.setUserId(userId);
            userAuth.setPhone(phone);
            userAuth.setPassword(passwordEncoder.encode(password));
            userAuth.setLoginType(1); // 手机号登录
            userAuth.setLoginCount(0);
            userAuth.setStatus(1);

            return userAuthMapper.insert(userAuth) > 0;
        } catch (Exception e) {
            log.error("创建用户认证信息失败：userId={}, phone={}", userId, phone, e);
            return false;
        }
    }

    /**
     * 验证用户登录
     */
    public UserAuth authenticateUser(String phone, String password) {
        try {
            UserAuth userAuth = userAuthMapper.findByPhoneAndLoginType(phone, 1);
            if (userAuth != null && passwordEncoder.matches(password, userAuth.getPassword())) {
                return userAuth;
            }
            return null;
        } catch (Exception e) {
            log.error("用户认证失败：phone={}", phone, e);
            return null;
        }
    }

    /**
     * 更新登录信息
     */
    public boolean updateLoginInfo(Long authId, String loginIp) {
        try {
            return userAuthMapper.updateLoginInfo(authId, new Date(), loginIp) > 0;
        } catch (Exception e) {
            log.error("更新登录信息失败：authId={}, loginIp={}", authId, loginIp, e);
            return false;
        }
    }

    /**
     * 检查手机号是否已注册
     */
    public boolean isPhoneRegistered(String phone) {
        try {
            return userAuthMapper.countByPhone(phone) > 0;
        } catch (Exception e) {
            log.error("检查手机号注册状态失败：phone={}", phone, e);
            return false;
        }
    }

    /**
     * 修改密码
     */
    public boolean changePassword(String phone, String newPassword) {
        try {
            UserAuth userAuth = userAuthMapper.findByPhoneAndLoginType(phone, 1);
            if (userAuth != null) {
                String encodedPassword = passwordEncoder.encode(newPassword);
                return userAuthMapper.updatePassword(userAuth.getId(), encodedPassword) > 0;
            }
            return false;
        } catch (Exception e) {
            log.error("修改密码失败：phone={}", phone, e);
            return false;
        }
    }

    /**
     * 根据用户ID获取认证信息
     */
    public UserAuth getByUserId(Long userId) {
        try {
            return userAuthMapper.findByUserId(userId);
        } catch (Exception e) {
            log.error("根据用户ID获取认证信息失败：userId={}", userId, e);
            return null;
        }
    }
}