package com.fishcam.adapter.web.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BackupStatusDto {
    private boolean weeklyMissed;
    private boolean monthlyMissed;
}