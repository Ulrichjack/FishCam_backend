package com.fishcam.application.export;

import com.fishcam.domain.backup.BackupRecord;
import com.fishcam.domain.backup.BackupRecordRepository;
import com.fishcam.domain.backup.TypeBackup;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CloudBackupService {

    private final PostgresBackupService postgresBackupService;
    private final DataScienceExportService dataScienceExportService;
    private final CloudflareR2StorageService cloudflareR2StorageService;
    private final BackupRecordRepository backupRecordRepository;

    public void syncToCloud() throws Exception {
        File sqlFile = postgresBackupService.generateSqlBackup();
        File csvFile = dataScienceExportService.generateSalesCsv();

        cloudflareR2StorageService.uploadBackup(sqlFile);
        cloudflareR2StorageService.uploadBackup(csvFile);

        BackupRecord record = new BackupRecord();
        record.setDateExecution(LocalDateTime.now());
        record.setType(TypeBackup.CLOUD_WEEKLY);
        record.setSuccess(true);
        backupRecordRepository.save(record);
    }
}
