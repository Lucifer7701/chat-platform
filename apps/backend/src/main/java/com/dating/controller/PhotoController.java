// 5. 用户照片控制器
package com.dating.controller;

import com.dating.util.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/photo")
@CrossOrigin
@Slf4j
@Deprecated // 推荐使用 /api/file 接口
public class PhotoController {

    @Autowired
    private FileController fileController;

    /**
     * 上传照片 - 兼容性接口，重定向到新的文件上传接口
     */
    @PostMapping("/upload")
    public Result uploadPhoto(@RequestAttribute("userId") Long userId,
                              @RequestParam("file") MultipartFile file,
                              @RequestParam(defaultValue = "avatar") String type) {
        log.warn("使用了已废弃的 /api/photo/upload 接口，请迁移到 /api/file/upload");
        // 重定向到新的文件上传接口
        return fileController.uploadFile(userId, file, type);
    }

    /**
     * 获取用户照片列表 - 兼容性接口
     */
    @GetMapping("/list")
    public Result getUserPhotos(@RequestAttribute("userId") Long userId) {
        log.warn("使用了已废弃的 /api/photo/list 接口，请迁移到 /api/file/list");
        return fileController.getUserPhotos(userId);
    }

    /**
     * 设置头像 - 兼容性接口
     */
    @PostMapping("/avatar")
    public Result setAvatar(@RequestAttribute("userId") Long userId,
                            @RequestParam Long photoId) {
        log.warn("使用了已废弃的 /api/photo/avatar 接口，请迁移到 /api/file/avatar");
        return fileController.setAvatar(userId, photoId);
    }
}