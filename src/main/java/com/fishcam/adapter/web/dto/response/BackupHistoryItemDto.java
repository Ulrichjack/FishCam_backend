package com.fishcam.adapter.web.dto.response;

import com.fishcam.domain.backup.TypeBackup;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BackupHistoryItemDto {
    private LocalDateTime dateExecution;
    private TypeBackup type;
    private boolean success;
}