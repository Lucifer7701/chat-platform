// 5. 用户照片控制器
package com.dating.controller;

import com.dating.entity.UserPhoto;
import com.dating.service.UserPhotoService;
import com.dating.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/photo")
@CrossOrigin
public class PhotoController {

    @Autowired
    private UserPhotoService userPhotoService;

    /**
     * 上传照片
     */
    @PostMapping("/upload")
    public Result uploadPhoto(@RequestAttribute("userId") Long userId,
                              @RequestParam("file") MultipartFile file,
                              @RequestParam(defaultValue = "0") Integer isAvatar) {
        try {
            // 这里应该实现文件上传逻辑，保存到文件服务器或云存储
            String photoUrl = "http://example.com/photos/" + System.currentTimeMillis() + ".jpg";

            UserPhoto userPhoto = new UserPhoto();
            userPhoto.setUserId(userId);
            userPhoto.setPhotoUrl(photoUrl);
            userPhoto.setIsAvatar(isAvatar);
            userPhoto.setStatus(1);

            userPhotoService.addUserPhoto(userPhoto);
            return Result.success("上传成功").put("photoUrl", photoUrl);
        } catch (Exception e) {
            return Result.error("上传失败");
        }
    }

    /**
     * 获取用户照片列表
     */
    @GetMapping("/list")
    public Result getUserPhotos(@RequestAttribute("userId") Long userId) {
        try {
            return Result.success(userPhotoService.getUserPhotos(userId));
        } catch (Exception e) {
            return Result.error("获取照片列表失败");
        }
    }

    /**
     * 设置头像
     */
    @PostMapping("/avatar")
    public Result setAvatar(@RequestAttribute("userId") Long userId,
                            @RequestParam Long photoId) {
        try {
            userPhotoService.setAvatar(userId, photoId);
            return Result.success("设置头像成功");
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("设置头像失败");
        }
    }
}