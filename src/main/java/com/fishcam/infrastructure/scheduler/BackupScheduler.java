package com.fishcam.infrastructure.scheduler;

import com.fishcam.application.export.CloudBackupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class BackupScheduler {

    private final CloudBackupService cloudBackupService;

    // Tous les jours a 19h00 : sauvegarde reelle vers le Cloud (R2).
    // CloudBackupService.syncToCloud() genere deja le dump SQL localement avant de
    // l'uploader (PostgresBackupService) - pas besoin d'un job local separe : sur un
    // disque ephemere comme Render, un dump jamais uploade ne protege rien.
    @Scheduled(cron = "0 0 19 * * *")
    public void generateDailyCloudBackup() {
        log.info("☁️ Démarrage de la sauvegarde Cloud quotidienne...");
        try {
            cloudBackupService.syncToCloud();
            log.info("✅ Sauvegarde Cloud quotidienne réussie");
        } catch (Exception e) {
            log.error("❌ Erreur lors de la sauvegarde Cloud quotidienne", e);
        }
    }
}