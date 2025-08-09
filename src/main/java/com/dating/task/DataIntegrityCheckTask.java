// 1. 数据完整性检查定时任务
package com.dating.task;

import com.dating.mapper.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class DataIntegrityCheckTask {

    private static final Logger logger = LoggerFactory.getLogger(DataIntegrityCheckTask.class);

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserAuthMapper userAuthMapper;

    @Autowired
    private UserPhotoMapper userPhotoMapper;

    @Autowired
    private UserMatchMapper userMatchMapper;

    @Autowired
    private ChatMessageMapper chatMessageMapper;

    @Autowired
    private ReportMapper reportMapper;

//    /**
//     * 每天凌晨2点执行数据完整性检查
//     */
//    @Scheduled(cron = "0 0 2 * * ?")
//    public void checkDataIntegrity() {
//        logger.info("开始执行数据完整性检查");
//
//        try {
//            cleanupOrphanedUserPhotos();
//            cleanupOrphanedUserMatches();
//            cleanupOrphanedChatMessages();
//            cleanupOrphanedReports();
//
//            logger.info("数据完整性检查完成");
//        } catch (Exception e) {
//            logger.error("数据完整性检查失败", e);
//        }
//    }

    /**
     * 清理孤立的用户照片记录
     */
    private void cleanupOrphanedUserPhotos() {
        try {
            int count = userPhotoMapper.deleteOrphanedRecords();
            if (count > 0) {
                logger.warn("清理了 {} 条孤立的用户照片记录", count);
            }
        } catch (Exception e) {
            logger.error("清理孤立用户照片记录失败", e);
        }
    }

    /**
     * 清理孤立的匹配记录
     */
    private void cleanupOrphanedUserMatches() {
        try {
            int count = userMatchMapper.deleteOrphanedRecords();
            if (count > 0) {
                logger.warn("清理了 {} 条孤立的匹配记录", count);
            }
        } catch (Exception e) {
            logger.error("清理孤立匹配记录失败", e);
        }
    }

    /**
     * 清理孤立的聊天消息记录
     */
    private void cleanupOrphanedChatMessages() {
        try {
            int count = chatMessageMapper.deleteOrphanedRecords();
            if (count > 0) {
                logger.warn("清理了 {} 条孤立的聊天消息记录", count);
            }
        } catch (Exception e) {
            logger.error("清理孤立聊天消息记录失败", e);
        }
    }

    /**
     * 清理孤立的举报记录
     */
    private void cleanupOrphanedReports() {
        try {
            int count = reportMapper.deleteOrphanedRecords();
            if (count > 0) {
                logger.warn("清理了 {} 条孤立的举报记录", count);
            }
        } catch (Exception e) {
            logger.error("清理孤立举报记录失败", e);
        }
    }
}
