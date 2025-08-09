// 4. 匹配服务
package com.dating.service;

import com.dating.entity.UserMatch;
import com.dating.mapper.UserMatchMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MatchService {

    @Autowired
    private UserMatchMapper userMatchMapper;

    @Autowired
    private DataIntegrityService dataIntegrityService;

    /**
     * 用户匹配操作（喜欢/不喜欢）
     */
    @Transactional
    public boolean userMatch(Long userId, Long targetUserId, Integer action) {
        // 验证两个用户都存在且不是同一个用户
        if (!dataIntegrityService.validateTwoDifferentUsers(userId, targetUserId)) {
            throw new IllegalArgumentException("用户不存在、状态异常或尝试对自己进行操作");
        }

        // 检查是否已经有匹配记录
        UserMatch existingMatch = userMatchMapper.findByUserAndTarget(userId, targetUserId);
        if (existingMatch != null) {
            // 更新现有记录
            existingMatch.setAction(action);
            userMatchMapper.updateById(existingMatch);
        } else {
            // 创建新的匹配记录
            UserMatch userMatch = new UserMatch();
            userMatch.setUserId(userId);
            userMatch.setTargetUserId(targetUserId);
            userMatch.setAction(action);
            userMatchMapper.insert(userMatch);
        }

        // 如果是喜欢操作，检查是否互相喜欢
        if (action == 1) {
            checkMutualMatch(userId, targetUserId);
        }

        return true;
    }

    /**
     * 检查并更新互相喜欢状态
     */
    private void checkMutualMatch(Long userId, Long targetUserId) {
        UserMatch reverseMatch = userMatchMapper.findByUserAndTarget(targetUserId, userId);

        if (reverseMatch != null && reverseMatch.getAction() == 1) {
            // 互相喜欢，更新双方记录
            userMatchMapper.updateMutualStatus(userId, targetUserId, 1);
            userMatchMapper.updateMutualStatus(targetUserId, userId, 1);
        }
    }

    /**
     * 获取互相喜欢的用户列表
     */
    public List<UserMatch> getMutualMatches(Long userId) {
        if (!dataIntegrityService.validateUser(userId)) {
            return null;
        }

        return userMatchMapper.findMutualMatches(userId);
    }

    /**
     * 删除用户相关的所有匹配记录（用户注销时调用）
     */
    @Transactional
    public void deleteUserMatchesByUserId(Long userId) {
        userMatchMapper.deleteByUserId(userId);
    }
}