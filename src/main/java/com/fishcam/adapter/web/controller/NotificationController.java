package com.fishcam.adapter.web.controller;

import com.fishcam.adapter.web.dto.response.ApiResponse;
import com.fishcam.adapter.web.dto.response.NotificationResponse;
import com.fishcam.application.notification.NotificationService;
import com.fishcam.domain.user.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Gestion des notifications utilisateur")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/user/{userId}")
    @Operation(summary = "Récupérer toutes les notifications d'un utilisateur")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'PATRON', 'CAISSIERE', 'ENREGISTREUR')")
    public ApiResponse<List<NotificationResponse>> getNotificationsByUser(
            @PathVariable Long userId,
            Authentication authentication) {

        User currentUser = (User) authentication.getPrincipal();
        List<NotificationResponse> data = notificationService.getNotificationsByUser(userId, currentUser);

        return ApiResponse.<List<NotificationResponse>>builder()
                .success(true).data(data)
                .message("Notifications récupérées")
                .code(200).timestamp(LocalDateTime.now())
                .build();
    }

    @PutMapping("/{id}/mark-as-read")
    @Operation(summary = "Marquer une notification comme lue")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'PATRON', 'CAISSIERE', 'ENREGISTREUR')")
    public ApiResponse<Void> markAsRead(
            @PathVariable Long id,
            Authentication authentication) {

        User currentUser = (User) authentication.getPrincipal();
        notificationService.markAsRead(id, currentUser);

        return ApiResponse.<Void>builder()
                .success(true)
                .message("Notification marquée comme lue")
                .code(200).timestamp(LocalDateTime.now())
                .build();
    }

    @GetMapping("/user/{userId}/unread-count")
    @Operation(
            summary = "Compter les notifications non lues",
            description = "Retourne le nombre de notifications non lues pour afficher le badge rouge"
    )
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'PATRON', 'CAISSIERE', 'ENREGISTREUR')")
    public ApiResponse<Map<String, Long>> countUnreadNotifications(
            @PathVariable Long userId,
            Authentication authentication) {

        User currentUser = (User) authentication.getPrincipal();
        long count = notificationService.countUnreadNotifications(userId, currentUser);

        return ApiResponse.<Map<String, Long>>builder()
                .success(true)
                .data(Map.of("count", count))
                .message("Nombre de notifications non lues récupéré")
                .code(200).timestamp(LocalDateTime.now())
                .build();
    }
}