package com.dating.mapper;

import com.dating.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface UserMapper {

    /**
     * 插入用户
     */
    int insert(User user);

    /**
     * 根据ID查询用户
     */
    User findById(@Param("id") Long id);

    /**
     * 根据手机号查询用户
     */
    User findByPhone(@Param("phone") String phone);

    /**
     * 检查用户名是否存在
     */
    int countByUsername(@Param("username") String username);

    /**
     * 更新用户信息
     */
    int updateById(User user);

    /**
     * 更新用户状态
     */
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    /**
     * 更新实名认证状态
     */
    int updateRealNameVerified(@Param("id") Long id, @Param("realNameVerified") Integer realNameVerified);

    /**
     * 更新用户位置
     */
    int updateLocation(@Param("id") Long id, @Param("latitude") java.math.BigDecimal latitude, @Param("longitude") java.math.BigDecimal longitude);

    /**
     * 获取附近用户
     */
    List<User> findNearbyUsers(@Param("userId") Long userId, @Param("latitude") java.math.BigDecimal latitude, @Param("longitude") java.math.BigDecimal longitude, @Param("limit") Integer limit);

    /**
     * 获取同城用户
     */
    List<User> findSameCityUsers(@Param("userId") Long userId, @Param("city") String city, @Param("limit") Integer limit);
}