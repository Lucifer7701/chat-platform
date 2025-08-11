// 11. 定时任务清理无效连接
package com.dating.task;

import com.dating.service.OnlineStatusService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OnlineStatusTask {

    @Autowired
    private OnlineStatusService onlineStatusService;

    // 每30秒清理一次无效连接
    @Scheduled(fixedRate = 30000)
    public void cleanInactiveSessions() {
        onlineStatusService.cleanInactiveSessions();
        int onlineCount = onlineStatusService.getOnlineCount();
        log.debug("当前在线用户数量: {}", onlineCount);
    }
}
