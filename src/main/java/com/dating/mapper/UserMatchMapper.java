// 3. 匹配记录Mapper
package com.dating.mapper;

import com.dating.entity.UserMatch;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserMatchMapper {

    int insert(UserMatch userMatch);

    UserMatch findByUserAndTarget(@Param("userId") Long userId, @Param("targetUserId") Long targetUserId);

    int updateById(UserMatch userMatch);

    int updateMutualStatus(@Param("userId") Long userId, @Param("targetUserId") Long targetUserId, @Param("isMutual") Integer isMutual);

    List<UserMatch> findMutualMatches(@Param("userId") Long userId);

    int deleteByUserId(@Param("userId") Long userId);

    int deleteOrphanedRecords();
}