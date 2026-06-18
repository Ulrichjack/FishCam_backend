package com.fishcam.adapter.web.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class BackupStatusDto {
    private boolean weeklyMissed;
    private boolean monthlyMissed;
    private List<BackupHistoryItemDto> history;
}