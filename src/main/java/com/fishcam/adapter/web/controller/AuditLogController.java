package com.fishcam.adapter.web.controller;

import com.fishcam.adapter.web.dto.response.ApiResponse;
import com.fishcam.adapter.web.dto.response.AuditLogResponse;
import com.fishcam.adapter.web.dto.response.StatistiquesPoissonnerieResponse;
import com.fishcam.application.audit.AuditLogService;
import com.fishcam.domain.audit.AuditLog;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/audit-logs")
@RequiredArgsConstructor
@Tag(name = "Audit", description = "Gestion des audits")
public class AuditLogController {

    private final AuditLogService auditLogService;

    @PreAuthorize("hasAnyRole('PATRON', 'SUPER_ADMIN')")
    @GetMapping
    public ApiResponse<Page<AuditLogResponse>> getAllAudit(
           @PageableDefault(sort = "timestamp", direction = Sort.Direction.DESC) Pageable pageable){

        Page <AuditLogResponse> response = auditLogService.getAllLogs(pageable);
        return ApiResponse.<Page<AuditLogResponse>>builder()
                .success(true)
                .data(response)
                .message("Audit log générée avec succès")
                .code(200)
                .timestamp(LocalDateTime.now())
                .build();
    }

}
