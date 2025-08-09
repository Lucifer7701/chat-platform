package com.dating.mapper;

import com.dating.entity.SmsCode;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface SmsCodeMapper {

    /**
     * 插入验证码
     */
    int insert(SmsCode smsCode);

    /**
     * 查询最新的有效验证码
     */
    SmsCode findLatestValidCode(@Param("phone") String phone, @Param("type") Integer type);

    /**
     * 标记验证码为已使用
     */
    int markAsUsed(@Param("id") Long id);

    /**
     * 清理过期验证码
     */
    int clearExpiredCodes();

    /**
     * 查询今日发送次数
     */
    int countTodaySendTimes(@Param("phone") String phone);

    /**
     * 查询最近发送的验证码
     */
    SmsCode findLatestByPhoneAndType(@Param("phone") String phone, @Param("type") Integer type);
}