package com.fishcam.adapter.web.mapper;

import com.fishcam.adapter.web.dto.response.AuditLogResponse;

import com.fishcam.domain.audit.AuditLog;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AuditLogMapper  {

    AuditLogResponse toResponse(AuditLog auditLog);

}
