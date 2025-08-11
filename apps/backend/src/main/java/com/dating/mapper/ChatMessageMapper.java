// 4. 聊天消息Mapper
package com.dating.mapper;

import com.dating.dto.ChatContact;
import com.dating.entity.ChatMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ChatMessageMapper {

    int insert(ChatMessage chatMessage);

    List<ChatMessage> findBetweenUsers(@Param("userId1") Long userId1,
                                       @Param("userId2") Long userId2,
                                       @Param("offset") Integer offset,
                                       @Param("limit") Integer limit);

    int markAsRead(@Param("toUserId") Long toUserId, @Param("fromUserId") Long fromUserId);

    int deleteByUserId(@Param("userId") Long userId);

    int deleteOrphanedRecords();


    List<ChatMessage> getChatHistory(@Param("userId1") Long userId1,
                                     @Param("userId2") Long userId2,
                                     @Param("offset") Integer offset,
                                     @Param("limit") Integer limit);


    int getUnreadCount(@Param("userId") Long userId);

    List<ChatContact> getChatContacts(@Param("userId") Long userId);
}