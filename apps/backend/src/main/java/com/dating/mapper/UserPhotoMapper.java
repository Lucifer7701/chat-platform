// 2. 用户照片Mapper
package com.dating.mapper;

import com.dating.entity.UserPhoto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserPhotoMapper {

    int insert(UserPhoto userPhoto);

    UserPhoto findById(@Param("id") Long id);

    List<UserPhoto> findByUserId(@Param("userId") Long userId);

    int clearAvatarFlag(@Param("userId") Long userId);

    int setAsAvatar(@Param("photoId") Long photoId);

    int deleteByUserId(@Param("userId") Long userId);

    int deleteById(@Param("id") Long id);

    /**
     * 删除孤立的用户照片记录
     */
    int deleteOrphanedRecords();
}