package com.fishcam.application.export;

import com.fishcam.adapter.web.dto.response.BackupStatusDto;
import com.fishcam.domain.backup.BackupRecordRepository;
import com.fishcam.domain.backup.TypeBackup;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class BackupStatusService {

    private final BackupRecordRepository backupRecordRepository;

    public BackupStatusDto getBackupStatus() {
        // 1. Vérifier la sauvegarde de la semaine (Est-elle plus vieille que 7 jours ?)
        var lastWeekly = backupRecordRepository.findTopByTypeOrderByDateExecutionDesc(TypeBackup.HEBDOMADAIRE);
        boolean isWeeklyMissed = lastWeekly.isEmpty() ||
                lastWeekly.get().getDateExecution().isBefore(LocalDateTime.now().minusDays(7));

        // 2. Vérifier la sauvegarde du mois (A-t-elle été faite ce mois-ci ?)
        var lastMonthly = backupRecordRepository.findTopByTypeOrderByDateExecutionDesc(TypeBackup.MENSUEL);
        boolean isMonthlyMissed = lastMonthly.isEmpty() ||
                lastMonthly.get().getDateExecution().getMonth() != LocalDateTime.now().getMonth();

        // 3. Retourner le DTO propre
        return BackupStatusDto.builder()
                .weeklyMissed(isWeeklyMissed)
                .monthlyMissed(isMonthlyMissed)
                .build();
    }
}