package com.fishcam.adapter.web.controller;

import com.fishcam.adapter.web.dto.response.ApiResponse;
import com.fishcam.adapter.web.dto.response.NotificationResponse;
import com.fishcam.application.notification.NotificationService;
import com.fishcam.domain.user.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
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

    @GetMapping("/user/{userId}/page")
    @Operation(summary = "Récupérer les notifications paginées d'un utilisateur")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'PATRON', 'CAISSIERE', 'ENREGISTREUR')")
    public ApiResponse<Page<NotificationResponse>> getNotificationsPageByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication
    ) {
        User currentUser = (User) authentication.getPrincipal();
        Page<NotificationResponse> data =
                notificationService.getNotificationsPageByUser(userId, page, size, currentUser);

        return ApiResponse.<Page<NotificationResponse>>builder()
                .success(true).data(data)
                .message("Notifications paginées récupérées")
                .code(200).timestamp(LocalDateTime.now())
                .build();
    }

    @GetMapping("/user/{userId}/recent")
    @Operation(summary = "Récupérer les notifications récentes (dashboard)")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'PATRON', 'CAISSIERE', 'ENREGISTREUR')")
    public ApiResponse<List<NotificationResponse>> getRecent(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "5") int limit,
            Authentication authentication
    ) {
        User currentUser = (User) authentication.getPrincipal();
        List<NotificationResponse> data =
                notificationService.getRecentNotifications(userId, limit, currentUser);

        return ApiResponse.<List<NotificationResponse>>builder()
                .success(true).data(data)
                .message("Notifications récentes récupérées")
                .code(200).timestamp(LocalDateTime.now())
                .build();
    }

    @PutMapping("/user/{userId}/mark-all-as-read")
    @Operation(summary = "Marquer toutes les notifications comme lues")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'PATRON', 'CAISSIERE', 'ENREGISTREUR')")
    public ApiResponse<Map<String, Integer>> markAllAsRead(
            @PathVariable Long userId,
            Authentication authentication
    ) {
        User currentUser = (User) authentication.getPrincipal();
        int updated = notificationService.markAllAsRead(userId, currentUser);

        return ApiResponse.<Map<String, Integer>>builder()
                .success(true)
                .data(Map.of("updated", updated))
                .message("Notifications marquées comme lues")
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