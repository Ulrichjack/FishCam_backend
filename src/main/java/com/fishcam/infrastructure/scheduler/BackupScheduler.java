package com.fishcam.infrastructure.scheduler;

import com.fishcam.application.export.CloudBackupService;
import com.fishcam.application.export.PostgresBackupService;
import com.fishcam.domain.backup.BackupRecord;
import com.fishcam.domain.backup.BackupRecordRepository;
import com.fishcam.domain.backup.TypeBackup;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class BackupScheduler {

    private final PostgresBackupService postgresBackupService;
    private final CloudBackupService cloudBackupService;
    private final BackupRecordRepository backupRecordRepository;

    // Tous les jours à 19h00 (Sauvegarde Locale uniquement - disque ephemere sur Render,
    // utile surtout sur le deploiement Docker Compose a disque persistant)
    @Scheduled(cron = "0 0 19 * * *")
    public void generateDailyLocalBackup() {
        log.info("⏰ Démarrage de la sauvegarde locale quotidienne...");
        try {
            postgresBackupService.generateSqlBackup();

            BackupRecord record = new BackupRecord();
            record.setDateExecution(LocalDateTime.now());
            record.setType(TypeBackup.LOCAL_DAILY);
            record.setSuccess(true);
            backupRecordRepository.save(record);

        } catch (Exception e) {
            log.error("❌ Erreur lors de la sauvegarde locale", e);
        }
    }

    // Chaque dimanche a 20h00 : sauvegarde reelle vers le Cloud (R2), la seule qui
    // survit a un redemarrage/redeploy sur un disque ephemere comme Render.
    @Scheduled(cron = "0 0 20 * * SUN")
    public void generateWeeklyCloudBackup() {
        log.info("☁️ Démarrage de la synchronisation Cloud hebdomadaire...");
        try {
            cloudBackupService.syncToCloud();
            log.info("✅ Synchronisation Cloud hebdomadaire réussie");
        } catch (Exception e) {
            log.error("❌ Erreur lors de la synchronisation Cloud hebdomadaire", e);
        }
    }
}