package com.univ.momcare.controller;

import com.univ.momcare.common.R;
import com.univ.momcare.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** 经营驾驶舱数据：8 个指标 + 6 张图表 */
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/overview")
    public R<Map<String, Object>> overview() {
        return R.ok(dashboardService.overview());
    }

    @GetMapping("/charts")
    public R<Map<String, Object>> charts() {
        return R.ok(dashboardService.charts());
    }
}
