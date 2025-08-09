package com.dating.mapper;

import com.dating.entity.UserAuth;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;

@Mapper
public interface UserAuthMapper {

    /**
     * 插入认证信息
     */
    int insert(UserAuth userAuth);

    /**
     * 根据ID查询
     */
    UserAuth findById(@Param("id") Long id);

    /**
     * 根据手机号和登录类型查询认证信息
     */
    UserAuth findByPhoneAndLoginType(@Param("phone") String phone, @Param("loginType") Integer loginType);

    /**
     * 更新登录信息
     */
    int updateLoginInfo(@Param("id") Long id, @Param("loginTime") Date loginTime, @Param("loginIp") String loginIp);

    /**
     * 检查手机号是否已注册
     */
    int countByPhone(@Param("phone") String phone);

    /**
     * 根据用户ID删除认证信息
     */
    int deleteByUserId(@Param("userId") Long userId);

    /**
     * 更新密码
     */
    int updatePassword(@Param("id") Long id, @Param("password") String password);

    /**
     * 根据用户ID查询认证信息
     */
    UserAuth findByUserId(@Param("userId") Long userId);
}