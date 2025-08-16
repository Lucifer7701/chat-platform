package com.dating.controller;

import com.dating.entity.UserPhoto;
import com.dating.service.MinioService;
import com.dating.service.UserPhotoService;
import com.dating.util.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/file")
@CrossOrigin
@Slf4j
public class FileController {

    @Autowired
    private MinioService minioService;

    @Autowired
    private UserPhotoService userPhotoService;

    /**
     * 统一文件上传接口
     */
    @PostMapping("/upload")
    public Result uploadFile(@RequestAttribute("userId") Long userId,
                             @RequestParam("file") MultipartFile file,
                             @RequestParam(defaultValue = "avatar") String type) {
        try {
            // 验证文件
            if (file.isEmpty()) {
                return Result.error("文件不能为空");
            }

            // 验证文件类型
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return Result.error("只支持图片文件");
            }

            // 验证文件大小 (5MB)
            if (file.getSize() > 5 * 1024 * 1024) {
                return Result.error("文件大小不能超过5MB");
            }

            // 验证文件类型参数
            if (!isValidFileType(type)) {
                return Result.error("不支持的文件类型: " + type);
            }

            // 上传到MinIO
            String fileUrl = minioService.uploadFile(file, type, userId);

            // 如果是头像，更新用户表
            if ("avatar".equals(type)) {
                userPhotoService.updateUserAvatar(userId, fileUrl);
            }

            // 保存到照片表
            UserPhoto userPhoto = new UserPhoto();
            userPhoto.setUserId(userId);
            userPhoto.setPhotoUrl(fileUrl);
            userPhoto.setIsAvatar("avatar".equals(type) ? 1 : 0);
            userPhoto.setStatus(1);
            userPhotoService.addUserPhoto(userPhoto);

            log.info("文件上传成功: userId={}, type={}, url={}", userId, type, fileUrl);
            return Result.success("上传成功").put("photoUrl", fileUrl);

        } catch (Exception e) {
            log.error("文件上传失败: userId={}, type={}", userId, type, e);
            return Result.error("上传失败: " + e.getMessage());
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
            log.error("获取照片列表失败: userId={}", userId, e);
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
            log.error("设置头像失败: userId={}, photoId={}", userId, photoId, e);
            return Result.error("设置头像失败");
        }
    }

    /**
     * 删除文件
     */
    @DeleteMapping("/{photoId}")
    public Result deletePhoto(@RequestAttribute("userId") Long userId,
                              @PathVariable Long photoId) {
        try {
            // TODO: 实现删除逻辑，包括从MinIO删除文件和从数据库删除记录
            return Result.success("删除成功");
        } catch (Exception e) {
            log.error("删除文件失败: userId={}, photoId={}", userId, photoId, e);
            return Result.error("删除失败");
        }
    }

    /**
     * 验证文件类型是否有效
     */
    private boolean isValidFileType(String type) {
        return "avatar".equals(type) || 
               "moment".equals(type) || 
               "profile".equals(type) || 
               "chat".equals(type);
    }
}
