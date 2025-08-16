package com.dating.service;

import com.dating.entity.User;
import com.dating.dto.UserRegisterRequest;
import com.dating.mapper.UserMapper;
import com.dating.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserAuthService userAuthService;

    @Autowired
    private SmsService smsService;

    /**
     * 用户注册
     */
    @Transactional(rollbackFor = Exception.class)
    public User registerUser(UserRegisterRequest request) {
        try {
            // 1. 创建用户基础信息
            User user = new User();
            user.setPhone(request.getPhone());
            setUserDefaults(user, request);

            if (userMapper.insert(user) > 0) {
                // 2. 创建认证信息
                boolean authResult = userAuthService.createUserAuth(
                        user.getId(),
                        request.getPhone(),
                        request.getPassword()
                );

                if (authResult) {
                    // 3. 异步处理后续任务
                    asyncPostRegister(user.getId());

                    return user;
                } else {
                    throw new BusinessException("创建认证信息失败");
                }
            }

            throw new BusinessException("创建用户失败");

        } catch (Exception e) {
            log.error("注册用户失败：", e);
            if (e instanceof BusinessException) {
                throw e;
            }
            throw new BusinessException("注册失败：" + e.getMessage());
        }
    }

    /**
     * 检查手机号是否已注册
     */
    public boolean isPhoneRegistered(String phone) {
        return userAuthService.isPhoneRegistered(phone);
    }

    /**
     * 检查用户名是否已存在
     */
    public boolean existsByUsername(String username) {
        return userMapper.countByUsername(username) > 0;
    }

    /**
     * 根据ID查询用户
     */
    public User findById(Long id) {
        try {
            return userMapper.findById(id);
        } catch (Exception e) {
            log.error("根据ID查询用户失败：id={}", id, e);
            return null;
        }
    }

    /**
     * 设置用户默认值
     */
    private void setUserDefaults(User user, UserRegisterRequest request) {
        // 生成默认用户名
        if (StringUtils.isBlank(request.getUsername())) {
            user.setUsername(generateDefaultUsername());
        } else {
            user.setUsername(request.getUsername());
        }

        // 生成默认昵称
        if (StringUtils.isBlank(request.getNickname())) {
            user.setNickname("用户" + System.currentTimeMillis() % 100000);
        } else {
            user.setNickname(request.getNickname());
        }

        // 设置默认头像
        user.setAvatar(getDefaultAvatar(request.getGender()));
        user.setGender(request.getGender());
        user.setStatus(1);
        user.setRealNameVerified(0);
    }

    /**
     * 生成默认用户名
     */
    private String generateDefaultUsername() {
        return "user_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 1000);
    }

    /**
     * 获取默认头像
     */
    private String getDefaultAvatar(Integer gender) {
        if (gender != null && gender == 1) {
            return "/default/avatar_male.png";
        } else if (gender != null && gender == 2) {
            return "/default/avatar_female.png";
        }
        return "/default/avatar_default.png";
    }

    /**
     * 注册后异步处理
     */
    @Async
    public void asyncPostRegister(Long userId) {
        try {
            // 创建用户详情记录
            // 发送欢迎消息
            // 统计数据更新
            log.info("异步处理注册后任务：userId={}", userId);
        } catch (Exception e) {
            log.error("异步处理注册后任务失败：userId={}", userId, e);
        }
    }

    /**
     * 更新用户位置
     */
    public int updateUserLocation(Long userId, BigDecimal latitude, BigDecimal longitude) {
        try {
            return userMapper.updateLocation(userId, latitude, longitude);
        } catch (Exception e) {
            log.error("更新用户位置失败：userId={}", userId, e);
            return 0;
        }
    }

    /**
     * 获取附近用户
     */
    public List<User> findNearbyUsers(Long userId, BigDecimal latitude, BigDecimal longitude, Integer limit) {
        try {
            return userMapper.findNearbyUsers(userId, latitude, longitude, limit);
        } catch (Exception e) {
            log.error("获取附近用户失败：userId={}", userId, e);
            return new ArrayList<>();
        }
    }

    /**
     * 获取同城用户
     */
    public List<User> findSameCityUsers(Long userId, String city, Integer limit) {
        try {
            return userMapper.findSameCityUsers(userId, city, limit);
        } catch (Exception e) {
            log.error("获取同城用户失败：userId={}", userId, e);
            return new ArrayList<>();
        }
    }

    /**
     * 更新用户资料
     */
    public int updateUserProfile(User user) {
        try {
            return userMapper.updateById(user);
        } catch (Exception e) {
            log.error("更新用户资料失败：userId={}", user.getId(), e);
            return 0;
        }
    }

    /**
     * 更新用户头像
     */
    public boolean updateUserAvatar(Long userId, String avatarUrl) {
        try {
            User user = new User();
            user.setId(userId);
            user.setAvatar(avatarUrl);
            return userMapper.updateById(user) > 0;
        } catch (Exception e) {
            log.error("更新用户头像失败：userId={}, avatarUrl={}", userId, avatarUrl, e);
            return false;
        }
    }
}