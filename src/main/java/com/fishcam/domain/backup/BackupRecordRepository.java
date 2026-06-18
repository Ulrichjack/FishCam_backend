package com.fishcam.domain.backup;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BackupRecordRepository extends JpaRepository<BackupRecord, Long> {

    Optional<BackupRecord> findTopByTypeOrderByDateExecutionDesc(TypeBackup type);

    List<BackupRecord> findTop10ByTypeOrderByDateExecutionDesc(TypeBackup type);
    List<BackupRecord> findTop10ByOrderByDateExecutionDesc();

}
