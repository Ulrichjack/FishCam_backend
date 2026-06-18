package com.fishcam.infrastructure.aop;

import com.fishcam.application.audit.AuditLogService;
import com.fishcam.domain.user.User; // <-- IMPORT TRÈS IMPORTANT
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

    @AfterReturning(pointcut = "@annotation(logAudit)", returning = "result")
    public void logActivity(JoinPoint joinPoint, LogAudit logAudit, Object result) {

        // 1. Récupérer le VRAI nom de l'utilisateur (au lieu du téléphone)
        String performedBy = "Système";
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated() && !authentication.getPrincipal().equals("anonymousUser")) {
            Object principal = authentication.getPrincipal();
            // On vérifie si le principal est bien notre entité User
            if (principal instanceof User) {
                User user = (User) principal;
                // Résultat : "Theophile FOSSO"
                performedBy = user.getFirstName() + " " + user.getLastName();
            } else {
                performedBy = authentication.getName();
            }
        }

        // 2. Extraire l'ID de l'entité modifiée
        Long entityId = extractIdFromResult(result);

        // 3. Mettre les détails en Français !
        String methodName = joinPoint.getSignature().getName();
        String details = "Opération effectuée via : " + methodName;

        // 4. Sauvegarder en base de données
        auditLogService.logAction(
                logAudit.action(),
                logAudit.entityName(),
                entityId,
                performedBy,
                details
        );
    }

    private Long extractIdFromResult(Object result) {
        if (result == null) return null;
        try {
            Method getIdMethod = result.getClass().getMethod("getId");
            Object id = getIdMethod.invoke(result);
            if (id instanceof Long) {
                return (Long) id;
            }
        } catch (Exception e) {
            log.debug("Impossible d'extraire l'ID : {}", e.getMessage());
        }
        return null;
    }
}