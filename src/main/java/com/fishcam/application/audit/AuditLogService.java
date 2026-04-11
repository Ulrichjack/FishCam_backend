package com.fishcam.application.audit;

import com.fishcam.adapter.web.dto.response.AuditLogResponse;
import com.fishcam.adapter.web.mapper.AuditLogMapper;
import com.fishcam.domain.audit.AuditLog;
import com.fishcam.domain.audit.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditLogService {
    private final AuditLogRepository auditLogRepository;
    private final AuditLogMapper auditLogMapper;

    public void logAction(String action, String entityName, Long entityId, String performedBy, String details){
        AuditLog auditLog = AuditLog.builder()
                .action(action)
                .entityName(entityName)
                .entityId(entityId)
                .performedBy(performedBy)
                .details(details)
                .build();
        log.info("Audit Log saved: {} on {} by {}", action, entityName, performedBy);
        auditLogRepository.save(auditLog);
    }

    public Page<AuditLogResponse> getAllLogs(Pageable pageable) {
        Page<AuditLog> auditLogPage = auditLogRepository.findAll(pageable);

       return  auditLogPage.map(auditLogMapper::toResponse);

    }

}
