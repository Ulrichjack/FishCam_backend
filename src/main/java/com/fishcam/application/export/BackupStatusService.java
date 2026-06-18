package com.fishcam.application.export;

import com.fishcam.adapter.web.dto.response.BackupHistoryItemDto;
import com.fishcam.adapter.web.dto.response.BackupStatusDto;
import com.fishcam.domain.backup.BackupRecord;
import com.fishcam.domain.backup.BackupRecordRepository;
import com.fishcam.domain.backup.TypeBackup;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BackupStatusService {

    private final BackupRecordRepository backupRecordRepository;

    public BackupStatusDto getBackupStatus() {
        var lastCloudSync = backupRecordRepository.findTopByTypeOrderByDateExecutionDesc(TypeBackup.CLOUD_WEEKLY);

        boolean isCloudSyncMissed = lastCloudSync.isEmpty() ||
                lastCloudSync.get().getDateExecution().isBefore(LocalDateTime.now().minusDays(7));

        // 1. Récupérer l'historique depuis la BDD
        List<BackupRecord> recentBackups = backupRecordRepository.findTop10ByOrderByDateExecutionDesc();

        // 2. Transformer les entités en DTOs
        List<BackupHistoryItemDto> history = recentBackups.stream()
                .map(record -> BackupHistoryItemDto.builder()
                        .dateExecution(record.getDateExecution())
                        .type(record.getType())
                        .success(record.getSuccess())
                        .build())
                .collect(Collectors.toList());

        return BackupStatusDto.builder()
                .weeklyMissed(isCloudSyncMissed)
                .monthlyMissed(false)
                .history(history) // 3. Ajouter l'historique au DTO final
                .build();
    }
}