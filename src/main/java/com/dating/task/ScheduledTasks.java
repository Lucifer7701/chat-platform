package com.dating.task;

import com.dating.service.SmsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ScheduledTasks {

    @Autowired
    private SmsService smsService;

    /**
     * 每小时清理过期验证码
     */
    @Scheduled(fixedRate = 3600000) // 1小时
    public void clearExpiredSmsCode() {
        log.info("开始清理过期验证码");
        smsService.clearExpiredCodes();
        log.info("清理过期验证码完成");
    }
}