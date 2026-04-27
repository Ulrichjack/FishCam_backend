package com.fishcam.infrastructure.scheduler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fishcam.application.export.DatabaseExportService;
import com.fishcam.application.export.EmailBackupService;
import com.fishcam.application.export.PostgresBackupService;
import com.fishcam.application.export.TelegramBackupService;
import com.fishcam.domain.backup.BackupRecord;
import com.fishcam.domain.backup.BackupRecordRepository;
import com.fishcam.domain.backup.TypeBackup;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class BackupScheduler {

    private final DatabaseExportService databaseExportService;
    private final TelegramBackupService telegramBackupService;
    private final EmailBackupService  emailBackupService;
    private final PostgresBackupService postgresBackupService;
    private final BackupRecordRepository backupRecordRepository;

    @Scheduled(cron = "0 30 19 * * SUN")
    public void sendWeeklyTelegramBackup() {
        log.info(" Démarrage de la sauvegarde hebdomadaire Telegram...");
        try {
            // 1. Get the Map
            Map<String, Object> allData = databaseExportService.exportAllData();

            // 2. Convert to JSON String
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
            String jsonDatabase = objectMapper.writeValueAsString(allData);

            //3.sql
            File sqlFile = postgresBackupService.generateSqlBackup();
            // 3. Send it
            telegramBackupService.sendBackup(jsonDatabase);
            telegramBackupService.sendSqlBackup(sqlFile);

            BackupRecord backupRecord = new BackupRecord();
            backupRecord.setDateExecution(LocalDateTime.now());
            backupRecord.setType(TypeBackup.HEBDOMADAIRE);
            backupRecord.setSuccess(true);

            backupRecordRepository.save(backupRecord);

            sqlFile.delete();
        } catch (Exception e) {
            log.error("❌ Erreur lors de la sauvegarde hebdomadaire Telegram", e);
        }
    }

    @Scheduled(cron = "0 0 19 1 * ?")
    public void sendMonthlyEmailBackup() {
        log.info(" Démarrage de la sauvegarde du mois Email...");
        try {
            // 1. Get the Map, File
            Map<String, Object> allData = databaseExportService.exportAllData();
            File sqlFile = postgresBackupService.generateSqlBackup();

            // 2. Convert to JSON String
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
            String jsonDatabase = objectMapper.writeValueAsString(allData);

            // 3. Send it
            emailBackupService.sendMonthlyBackup(jsonDatabase, sqlFile);
            BackupRecord backupRecord = new BackupRecord();
            backupRecord.setDateExecution(LocalDateTime.now());
            backupRecord.setType(TypeBackup.MENSUEL);
            backupRecord.setSuccess(true);

            backupRecordRepository.save(backupRecord);

            sqlFile.delete();

        } catch (Exception e) {
            log.error("❌ Erreur lors de la sauvegarde du mois Email", e);
        }
    }

    @EventListener(org.springframework.boot.context.event.ApplicationReadyEvent.class)
    public void checkMissedBackupsOnStartup() {
        if (LocalDateTime.now().getDayOfWeek() == DayOfWeek.MONDAY) {
            Optional<BackupRecord> lastBackup = backupRecordRepository.findTopByTypeOrderByDateExecutionDesc(TypeBackup.HEBDOMADAIRE);

            if (lastBackup.isEmpty() || lastBackup.get().getDateExecution().isBefore(LocalDateTime.now().minusDays(1))) {

                log.warn("⚠️ Missed Sunday backup detected! Running catch-up backup now...");
                log.warn("⚠️ Sauvegarde Hebdomadaire manquée ! Lancement...");
                sendWeeklyTelegramBackup();

            }
        }
        if (LocalDateTime.now().getDayOfMonth() <= 5){
            Optional<BackupRecord> lastMonthly = backupRecordRepository.findTopByTypeOrderByDateExecutionDesc(TypeBackup.MENSUEL);
            if (lastMonthly.isEmpty() || lastMonthly.get().getDateExecution().getMonth() != LocalDateTime.now().getMonth()) {
                log.warn("⚠️ Sauvegarde mensuelle manquée ! Lancement...");
                sendMonthlyEmailBackup();
            }
        }
    }

}
