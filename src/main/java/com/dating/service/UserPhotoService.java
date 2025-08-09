// 3. 用户相册服务
package com.dating.service;

import com.dating.entity.UserPhoto;
import com.dating.mapper.UserPhotoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserPhotoService {

    @Autowired
    private UserPhotoMapper userPhotoMapper;

    @Autowired
    private DataIntegrityService dataIntegrityService;

    /**
     * 添加用户照片
     */
    @Transactional
    public boolean addUserPhoto(UserPhoto userPhoto) {
        // 验证用户是否存在
        if (!dataIntegrityService.validateUser(userPhoto.getUserId())) {
            throw new IllegalArgumentException("用户不存在或状态异常");
        }

        try {
            return userPhotoMapper.insert(userPhoto) > 0;
        } catch (Exception e) {
            throw new RuntimeException("添加用户照片失败", e);
        }
    }

    /**
     * 获取用户照片列表
     */
    public List<UserPhoto> getUserPhotos(Long userId) {
        if (!dataIntegrityService.validateUser(userId)) {
            return null;
        }

        return userPhotoMapper.findByUserId(userId);
    }

    /**
     * 设置头像
     */
    @Transactional
    public boolean setAvatar(Long userId, Long photoId) {
        if (!dataIntegrityService.validateUser(userId)) {
            throw new IllegalArgumentException("用户不存在或状态异常");
        }

        // 验证照片是否属于该用户
        UserPhoto photo = userPhotoMapper.findById(photoId);
        if (photo == null || !photo.getUserId().equals(userId)) {
            throw new IllegalArgumentException("照片不存在或不属于该用户");
        }

        try {
            // 清除旧头像标记
            userPhotoMapper.clearAvatarFlag(userId);
            // 设置新头像
            return userPhotoMapper.setAsAvatar(photoId) > 0;
        } catch (Exception e) {
            throw new RuntimeException("设置头像失败", e);
        }
    }

    /**
     * 删除用户相关的所有照片（用户注销时调用）
     */
    @Transactional
    public void deleteUserPhotosByUserId(Long userId) {
        userPhotoMapper.deleteByUserId(userId);
    }
}