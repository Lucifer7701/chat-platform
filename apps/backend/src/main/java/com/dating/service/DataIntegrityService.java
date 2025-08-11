// 1. 数据完整性验证服务
package com.dating.service;

import com.dating.entity.User;
import com.dating.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DataIntegrityService {

    @Autowired
    private UserMapper userMapper;

    /**
     * 验证用户是否存在且状态正常
     */
    public boolean validateUser(Long userId) {
        if (userId == null || userId <= 0) {
            return false;
        }

        User user = userMapper.findById(userId);
        return user != null && user.getStatus() == 1; // 1表示正常状态
    }

    /**
     * 验证两个用户是否都存在且状态正常
     */
    public boolean validateTwoUsers(Long userId1, Long userId2) {
        return validateUser(userId1) && validateUser(userId2);
    }

    /**
     * 验证用户不能对自己进行操作
     */
    public boolean validateNotSameUser(Long userId1, Long userId2) {
        return userId1 != null && userId2 != null && !userId1.equals(userId2);
    }

    /**
     * 综合验证：两个不同的有效用户
     */
    public boolean validateTwoDifferentUsers(Long userId1, Long userId2) {
        return validateTwoUsers(userId1, userId2) && validateNotSameUser(userId1, userId2);
    }
}