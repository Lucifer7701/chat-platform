package com.dating.service;

import io.minio.*;
import io.minio.http.Method;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class MinioService {

    @Autowired
    private MinioClient minioClient;

    @Value("${minio.bucket-name}")
    private String bucketName;

    @Value("${minio.public-read}")
    private boolean publicRead;

    @Value("${minio.endpoint}")
    private String endpoint;

    // 公开读取策略JSON
    private static final String PUBLIC_READ_POLICY = """
            {
              "Version": "2012-10-17",
              "Statement": [
                {
                  "Effect": "Allow",
                  "Principal": "*",
                  "Action": ["s3:GetObject"],
                  "Resource": ["arn:aws:s3:::%s/*"]
                }
              ]
            }
            """;

    // 私有策略JSON
    private static final String PRIVATE_POLICY = """
            {
              "Version": "2012-10-17",
              "Statement": [
                {
                  "Effect": "Deny",
                  "Principal": "*",
                  "Action": ["s3:*"],
                  "Resource": ["arn:aws:s3:::%s/*"]
                }
              ]
            }
            """;

    /**
     * 初始化存储桶
     */
    @PostConstruct
    public void initializeBucket() {
        try {
            // 检查存储桶是否存在
            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(bucketName).build()
            );

            if (!exists) {
                // 创建存储桶
                minioClient.makeBucket(
                        MakeBucketArgs.builder().bucket(bucketName).build()
                );
                log.info("存储桶 {} 创建成功", bucketName);
            }

            // 设置访问策略
            String policy = publicRead 
                    ? String.format(PUBLIC_READ_POLICY, bucketName)
                    : String.format(PRIVATE_POLICY, bucketName);

            minioClient.setBucketPolicy(
                    SetBucketPolicyArgs.builder()
                            .bucket(bucketName)
                            .config(policy)
                            .build()
            );

            log.info("存储桶 {} 访问策略设置为: {}", bucketName, publicRead ? "公开读取" : "私有");

        } catch (Exception e) {
            log.error("初始化MinIO存储桶失败", e);
            throw new RuntimeException("MinIO初始化失败", e);
        }
    }

    /**
     * 上传文件
     *
     * @param file     文件
     * @param type     文件类型 (avatar/moment/profile/chat)
     * @param userId   用户ID
     * @return 文件访问URL
     */
    public String uploadFile(MultipartFile file, String type, Long userId) {
        try {
            // 生成文件名
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".")
                    ? originalFilename.substring(originalFilename.lastIndexOf("."))
                    : ".jpg";
            String filename = System.currentTimeMillis() + extension;

            // 生成对象名 (路径)
            String objectName = type + "/" + userId + "/" + filename;

            // 上传文件
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );

            log.info("文件上传成功: {}", objectName);

            // 返回访问URL
            return getFileUrl(objectName);

        } catch (Exception e) {
            log.error("文件上传失败", e);
            throw new RuntimeException("文件上传失败", e);
        }
    }

    /**
     * 获取文件访问URL
     *
     * @param objectName 对象名
     * @return 访问URL
     */
    public String getFileUrl(String objectName) {
        try {
            if (publicRead) {
                // 公开访问，返回直接URL
                return endpoint + "/" + bucketName + "/" + objectName;
            } else {
                // 私有访问，返回预签名URL (7天有效期)
                return minioClient.getPresignedObjectUrl(
                        GetPresignedObjectUrlArgs.builder()
                                .method(Method.GET)
                                .bucket(bucketName)
                                .object(objectName)
                                .expiry(7, TimeUnit.DAYS)
                                .build()
                );
            }
        } catch (Exception e) {
            log.error("获取文件URL失败: {}", objectName, e);
            throw new RuntimeException("获取文件URL失败", e);
        }
    }

    /**
     * 删除文件
     *
     * @param objectName 对象名
     */
    public void deleteFile(String objectName) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );
            log.info("文件删除成功: {}", objectName);
        } catch (Exception e) {
            log.error("文件删除失败: {}", objectName, e);
            throw new RuntimeException("文件删除失败", e);
        }
    }

    /**
     * 检查文件是否存在
     *
     * @param objectName 对象名
     * @return 是否存在
     */
    public boolean fileExists(String objectName) {
        try {
            minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
