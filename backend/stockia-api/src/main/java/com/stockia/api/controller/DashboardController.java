
package com.stockia.api.controller;

import com.stockia.api.dto.response.DashboardResumoResponse;
import com.stockia.api.service.DashboardService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/empresas/{empresaId}/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    public ResponseEntity<DashboardResumoResponse> buscarResumo(
            @PathVariable Long empresaId) {

        DashboardResumoResponse resumo = dashboardService.buscarResumo(empresaId);

        return ResponseEntity.ok(resumo);
    }
}
