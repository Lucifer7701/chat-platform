package com.dating.service;

import com.dating.entity.SmsCode;
import com.dating.mapper.SmsCodeMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Random;

@Service
@Slf4j
public class SmsService {

    @Autowired
    private SmsCodeMapper smsCodeMapper;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private static final String SMS_LIMIT_PREFIX = "sms_limit:";
    private static final int SMS_LIMIT_COUNT = 5; // 每日限制次数
    private static final int SMS_INTERVAL = 60; // 发送间隔(秒)

    /**
     * 发送短信验证码
     */
    public boolean sendSmsCode(String phone, Integer type, String ip) {
        try {
            // 1. 检查发送限制
            if (!checkSendLimit(phone)) {
                log.warn("短信发送超限：phone={}", phone);
                return false;
            }

            // 2. 检查发送间隔
            if (!checkSendInterval(phone, type)) {
                log.warn("短信发送间隔不足：phone={}, type={}", phone, type);
                return false;
            }

            // 3. 生成验证码
            String code = generateRandomCode();

            // 4. 发送短信（这里需要接入具体的短信服务商）
            boolean sendResult = sendSmsToProvider(phone, code, type);
            if (!sendResult) {
                return false;
            }

            // 5. 保存验证码记录
            SmsCode smsCode = new SmsCode();
            smsCode.setPhone(phone);
            smsCode.setCode(code);
            smsCode.setType(type);
            smsCode.setUsed(0);
            smsCode.setExpireTime(new Date(System.currentTimeMillis() + 5 * 60 * 1000)); // 5分钟过期
            smsCode.setIp(ip);

            boolean saveResult = smsCodeMapper.insert(smsCode) > 0;

            // 6. 更新限制计数
            if (saveResult) {
                updateSendLimit(phone);
                updateSendInterval(phone, type);
            }

            return saveResult;

        } catch (Exception e) {
            log.error("发送短信验证码失败：phone={}, type={}", phone, type, e);
            return false;
        }
    }

    /**
     * 验证短信验证码
     */
    public boolean verifySmsCode(String phone, String code, Integer type) {
        try {
            SmsCode smsCode = smsCodeMapper.findLatestValidCode(phone, type);
            if (smsCode != null && smsCode.getCode().equals(code)) {
                // 标记为已使用
                smsCodeMapper.markAsUsed(smsCode.getId());
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error("验证短信验证码失败：phone={}, code={}, type={}", phone, code, type, e);
            return false;
        }
    }

    /**
     * 生成随机验证码
     */
    private String generateRandomCode() {
        Random random = new Random();
        return String.format("%06d", random.nextInt(1000000));
    }

    /**
     * 发送短信到服务商（需要接入具体服务商）
     */
    private boolean sendSmsToProvider(String phone, String code, Integer type) {
        // TODO: 接入阿里云短信、腾讯云短信等服务商
        log.info("模拟发送短信验证码：phone={}, code={}, type={}", phone, code, type);
        return true;
    }

    /**
     * 检查每日发送限制
     */
    private boolean checkSendLimit(String phone) {
        try {
            String key = SMS_LIMIT_PREFIX + "daily:" + phone;
            String count = redisTemplate.opsForValue().get(key);
            return count == null || Integer.parseInt(count) < SMS_LIMIT_COUNT;
        } catch (Exception e) {
            log.error("检查发送限制失败：phone={}", phone, e);
            return true; // 异常时允许发送
        }
    }

    /**
     * 检查发送间隔
     */
    private boolean checkSendInterval(String phone, Integer type) {
        try {
            String key = SMS_LIMIT_PREFIX + "interval:" + phone + ":" + type;
            return !Boolean.TRUE.equals(redisTemplate.hasKey(key));
        } catch (Exception e) {
            log.error("检查发送间隔失败：phone={}, type={}", phone, type, e);
            return true; // 异常时允许发送
        }
    }

    /**
     * 更新发送限制计数
     */
    private void updateSendLimit(String phone) {
        try {
            String key = SMS_LIMIT_PREFIX + "daily:" + phone;
            redisTemplate.opsForValue().increment(key);
            redisTemplate.expire(key, Duration.ofDays(1));
        } catch (Exception e) {
            log.error("更新发送限制计数失败：phone={}", phone, e);
        }
    }

    /**
     * 更新发送间隔
     */
    private void updateSendInterval(String phone, Integer type) {
        try {
            String key = SMS_LIMIT_PREFIX + "interval:" + phone + ":" + type;
            redisTemplate.opsForValue().set(key, "1", Duration.ofSeconds(SMS_INTERVAL));
        } catch (Exception e) {
            log.error("更新发送间隔失败：phone={}, type={}", phone, type, e);
        }
    }

    /**
     * 清理过期验证码（定时任务调用）
     */
    public void clearExpiredCodes() {
        try {
            int count = smsCodeMapper.clearExpiredCodes();
            log.info("清理过期验证码：{} 条", count);
        } catch (Exception e) {
            log.error("清理过期验证码失败", e);
        }
    }
}
