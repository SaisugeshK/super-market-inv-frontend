package com.example.InventoryManagementSystem.controllor;

import com.example.InventoryManagementSystem.dto.DashboardSummaryDto;
import com.example.InventoryManagementSystem.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * GET /api/dashboard/summary
     * Returns today's sales, bill count, purchase total, supplier pending, low-stock products.
     */
    @GetMapping("/summary")
    public ResponseEntity<DashboardSummaryDto> getSummary() {
        return ResponseEntity.ok(dashboardService.getSummary());
    }
}
