// 5. 举报Mappee
package com.dating.mapper;

import com.dating.entity.Report;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ReportMapper {

    int insert(Report report);

    Report findById(@Param("id") Long id);

    Report findByReporterAndReported(@Param("reporterId") Long reporterId, @Param("reportedUserId") Long reportedUserId);

    List<Report> findByStatus(@Param("status") Integer status, @Param("offset") Integer offset, @Param("limit") Integer limit);

    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    int deleteByUserId(@Param("userId") Long userId);

    int deleteOrphanedRecords();
}
