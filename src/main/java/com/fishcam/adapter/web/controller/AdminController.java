package com.fishcam.adapter.web.controller;


import com.fishcam.adapter.web.dto.response.ApiResponse;
import com.fishcam.adapter.web.dto.response.BackupStatusDto;
import com.fishcam.application.export.BackupStatusService;
import com.fishcam.infrastructure.scheduler.BackupScheduler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;


@Slf4j
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Tag(name = "Admin Backup", description = "Gestion des sauvegardes de la base de données")
public class AdminController {

    private final BackupScheduler backupScheduler;
    private final BackupStatusService backupStatusService;

    @PostMapping("/backup/telegram")
    @Operation(summary = "Pousser la sauvegarde hebdomadaire vers Telegram")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'PATRON', 'CAISSIERE', 'ENREGISTREUR')")
    public ResponseEntity<ApiResponse<String>> triggerTelegramBackup() {
        try {
            backupScheduler.sendWeeklyTelegramBackup();

            ApiResponse<String> response = ApiResponse.<String>builder()
                    .success(true)
                    .message("Sauvegarde hebdomadaire (Telegram) envoyée avec succès !")
                    .code(HttpStatus.OK.value())
                    .timestamp(LocalDateTime.now())
                    .build();

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Erreur lors de la sauvegarde vers Telegram", e);

            ApiResponse<String> response = ApiResponse.<String>builder()
                    .success(false)
                    .message("Erreur lors de la sauvegarde : " + e.getMessage())
                    .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .timestamp(LocalDateTime.now())
                    .build();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }


    @PostMapping("/backup/email")
    @Operation(summary = "Pousser la sauvegarde mensuel vers Email")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'PATRON', 'CAISSIERE', 'ENREGISTREUR')")
    public ResponseEntity<ApiResponse<String>> triggerEmailBackup() {
        try {
            backupScheduler.sendMonthlyEmailBackup();

            ApiResponse<String> response = ApiResponse.<String>builder()
                    .success(true)
                    .message(" Sauvegarde mensuel (Email) envoyée avec succès !")
                    .code(HttpStatus.OK.value())
                    .timestamp(LocalDateTime.now())
                    .build();

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Erreur lors de la sauvegarde vers Email", e);

            ApiResponse<String> response = ApiResponse.<String>builder()
                    .success(false)
                    .message(" Erreur lors de la sauvegarde : " + e.getMessage())
                    .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .timestamp(LocalDateTime.now())
                    .build();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }


    @GetMapping("/backup/status")
    @Operation(summary = "Vérifier si une sauvegarde a été manquée (Pour afficher le bouton Frontend)")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'PATRON', 'CAISSIERE', 'ENREGISTREUR')")
    public ResponseEntity<ApiResponse<BackupStatusDto>> checkBackupStatus() {
        try {
            // Un seul appel très propre au Service !
            BackupStatusDto statusDto = backupStatusService.getBackupStatus();

            ApiResponse<BackupStatusDto> response = ApiResponse.<BackupStatusDto>builder()
                    .success(true)
                    .message("Statut des sauvegardes récupéré avec succès")
                    .data(statusDto)
                    .code(HttpStatus.OK.value())
                    .timestamp(LocalDateTime.now())
                    .build();

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Erreur lors de la récupération du statut des sauvegardes", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<BackupStatusDto>builder()
                            .success(false)
                            .message("Erreur système : " + e.getMessage())
                            .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .timestamp(LocalDateTime.now())
                            .build());
        }
    }


}