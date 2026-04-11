package com.fishcam.infrastructure.aop;

import com.fishcam.application.audit.AuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class AuditAspect {

    private final AuditLogService auditLogService;

    // This tells Spring: "Run this code AFTER any method that has @LogAudit finishes successfully"
    @AfterReturning(pointcut = "@annotation(logAudit)", returning = "result")
    public void logActivity(JoinPoint joinPoint, LogAudit logAudit, Object result) {

        // 1. Get the currently logged-in user from Spring Security
        String username = "System"; // Default fallback
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !authentication.getPrincipal().equals("anonymousUser")) {
            username = authentication.getName(); // Usually the email or username
        }

        // 2. Try to guess the ID of the entity that was created/updated
        Long entityId = extractIdFromResult(result);

        // 3. Create the detail message
        String methodName = joinPoint.getSignature().getName();
        String details = "Action executed via method: " + methodName;

        // 4. Save to the database using the service you just built!
        auditLogService.logAction(
                logAudit.action(),
                logAudit.entityName(),
                entityId,
                username,
                details
        );
    }

    // Helper method to try and find an "getId()" method on the returned object
    private Long extractIdFromResult(Object result) {
        if (result == null) return null;
        try {
            Method getIdMethod = result.getClass().getMethod("getId");
            Object id = getIdMethod.invoke(result);
            if (id instanceof Long) {
                return (Long) id;
            }
        } catch (Exception e) {
            log.debug("Could not extract ID from result: {}", e.getMessage());
        }
        return null;
    }
}