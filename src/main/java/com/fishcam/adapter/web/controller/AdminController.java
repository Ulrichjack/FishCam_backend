package com.fishcam.adapter.web.controller;

import com.fishcam.adapter.web.dto.response.ApiResponse;
import com.fishcam.adapter.web.dto.response.BackupStatusDto;
import com.fishcam.application.export.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Tag(name = "Admin Backup", description = "Gestion des sauvegardes Cloud")
public class AdminController {

    private final CloudBackupService cloudBackupService;
    private final BackupStatusService backupStatusService;

    @PostMapping("/backup/sync-cloud")
    @Operation(summary = "Pousser la sauvegarde et le CSV vers Cloudflare R2")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'PATRON', 'CAISSIERE', 'ENREGISTREUR')")
    public ResponseEntity<ApiResponse<String>> syncToCloud() {
        try {
            cloudBackupService.syncToCloud();

            return ResponseEntity.ok(ApiResponse.<String>builder()
                    .success(true)
                    .message("Sauvegarde SQL et CSV envoyée avec succès sur le Cloud !")
                    .code(200)
                    .timestamp(LocalDateTime.now())
                    .build());

        } catch (Exception e) {
            log.error("Erreur lors de la synchronisation Cloud", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<String>builder()
                            .success(false)
                            .message("Erreur de synchronisation : " + e.getMessage())
                            .code(500)
                            .timestamp(LocalDateTime.now())
                            .build());
        }
    }

    @GetMapping("/backup/status")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'PATRON', 'CAISSIERE', 'ENREGISTREUR')")
    public ResponseEntity<ApiResponse<BackupStatusDto>> checkBackupStatus() {
        BackupStatusDto statusDto = backupStatusService.getBackupStatus();
        return ResponseEntity.ok(ApiResponse.<BackupStatusDto>builder()
                .success(true).data(statusDto).code(200).timestamp(LocalDateTime.now()).build());
    }
}