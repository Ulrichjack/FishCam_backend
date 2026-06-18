package com.fishcam.adapter.web.controller;

import com.fishcam.adapter.web.dto.response.ApiResponse;
import com.fishcam.adapter.web.dto.response.BackupStatusDto;
import com.fishcam.application.export.*;
import com.fishcam.domain.backup.BackupRecord;
import com.fishcam.domain.backup.BackupRecordRepository;
import com.fishcam.domain.backup.TypeBackup;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Tag(name = "Admin Backup", description = "Gestion des sauvegardes Cloud")
public class AdminController {

    private final PostgresBackupService postgresBackupService;
    private final DataScienceExportService dataScienceExportService;
    private final CloudflareR2StorageService cloudflareR2StorageService;
    private final MinioStorageService minioStorageService;
    private final BackupStatusService backupStatusService;
    private final BackupRecordRepository backupRecordRepository;

    @PostMapping("/backup/sync-cloud")
    @Operation(summary = "Pousser la sauvegarde et le CSV vers Cloudflare R2 et MinIO")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'PATRON')")
    public ResponseEntity<ApiResponse<String>> syncToCloud() {
        try {
            // 1. Générer le SQL
            File sqlFile = postgresBackupService.generateSqlBackup();
            // 2. Générer le CSV pour le Machine Learning
            File csvFile = dataScienceExportService.generateSalesCsv();

            // 3. Envoyer sur Cloudflare R2
            cloudflareR2StorageService.uploadBackup(sqlFile);
            cloudflareR2StorageService.uploadBackup(csvFile);

            // 4. Envoyer sur MinIO
            minioStorageService.uploadBackup(sqlFile);
            minioStorageService.uploadBackup(csvFile);

            // 5. Enregistrer le succès en base de données
            BackupRecord record = new BackupRecord();
            record.setDateExecution(LocalDateTime.now());
            record.setType(TypeBackup.CLOUD_WEEKLY);
            record.setSuccess(true);
            backupRecordRepository.save(record);

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