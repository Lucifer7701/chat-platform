// 4. 举报控制器
package com.dating.controller;

import com.dating.entity.Report;
import com.dating.service.ReportService;
import com.dating.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/report")
@CrossOrigin
public class ReportController {

    @Autowired
    private ReportService reportService;

    /**
     * 提交举报
     */
    @PostMapping("/submit")
    public Result submitReport(@RequestAttribute("userId") Long userId,
                               @RequestBody Report report) {
        try {
            report.setReporterId(userId);
            report.setStatus(1); // 待处理

            reportService.submitReport(report);
            return Result.success("举报成功");
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("举报失败");
        }
    }
}