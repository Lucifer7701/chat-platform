// 6. 举报服务
package com.dating.service;

import com.dating.entity.Report;
import com.dating.mapper.ReportMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ReportService {

    @Autowired
    private ReportMapper reportMapper;

    @Autowired
    private DataIntegrityService dataIntegrityService;

    /**
     * 提交举报
     */
    @Transactional
    public boolean submitReport(Report report) {
        // 验证举报者和被举报者都存在且不是同一个用户
        if (!dataIntegrityService.validateTwoDifferentUsers(
                report.getReporterId(), report.getReportedUserId())) {
            throw new IllegalArgumentException("用户不存在、状态异常或尝试举报自己");
        }

        // 检查是否已经举报过
        Report existingReport = reportMapper.findByReporterAndReported(
                report.getReporterId(), report.getReportedUserId());

        if (existingReport != null) {
            throw new IllegalArgumentException("您已经举报过该用户");
        }

        try {
            return reportMapper.insert(report) > 0;
        } catch (Exception e) {
            throw new RuntimeException("提交举报失败", e);
        }
    }

    /**
     * 获取举报列表（管理员使用）
     */
    public List<Report> getReports(Integer status, Integer page, Integer size) {
        int offset = (page - 1) * size;
        return reportMapper.findByStatus(status, offset, size);
    }

    /**
     * 处理举报
     */
    @Transactional
    public boolean handleReport(Long reportId, Integer status) {
        Report report = reportMapper.findById(reportId);
        if (report == null) {
            throw new IllegalArgumentException("举报记录不存在");
        }

        // 验证被举报用户仍然存在
        if (!dataIntegrityService.validateUser(report.getReportedUserId())) {
            throw new IllegalArgumentException("被举报用户不存在或已注销");
        }

        try {
            return reportMapper.updateStatus(reportId, status) > 0;
        } catch (Exception e) {
            throw new RuntimeException("处理举报失败", e);
        }
    }

    /**
     * 删除用户相关的所有举报记录（用户注销时调用）
     */
    @Transactional
    public void deleteReportsByUserId(Long userId) {
        reportMapper.deleteByUserId(userId);
    }
}