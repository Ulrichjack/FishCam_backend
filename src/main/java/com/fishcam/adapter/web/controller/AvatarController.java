package com.fishcam.adapter.web.controller;

import com.fishcam.adapter.web.dto.response.ApiResponse;
import com.fishcam.application.user.AvatarService;
import com.fishcam.domain.user.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Avatars", description = "Gestion des photos de profil")
public class AvatarController {

    private final AvatarService avatarService;

    @PostMapping("/{id}/avatar")
    @Operation(summary = "Uploader une photo de profil")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'PATRON', 'CAISSIERE', 'ENREGISTREUR')")
    public ResponseEntity<ApiResponse<String>> uploadAvatar(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {

        // Un user peut changer son propre avatar, SUPER_ADMIN peut changer n'importe lequel
        User currentUser = (User) authentication.getPrincipal();
        boolean isSuperAdmin = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN"));

        if (!currentUser.getId().equals(id) && !isSuperAdmin) {
            return ResponseEntity.status(403).body(
                    ApiResponse.<String>builder()
                            .success(false)
                            .code(403)
                            .message("Vous ne pouvez modifier que votre propre photo de profil")
                            .timestamp(LocalDateTime.now())
                            .build()
            );
        }

        String path = avatarService.uploadAvatar(id, file);

        return ResponseEntity.ok(
                ApiResponse.<String>builder()
                        .success(true)
                        .data(path)
                        .message("Photo de profil mise à jour avec succès")
                        .code(200)
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }

    @GetMapping("/{id}/avatar")
    @Operation(summary = "Récupérer la photo de profil")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'PATRON', 'CAISSIERE', 'ENREGISTREUR')")
    public ResponseEntity<Resource> getAvatar(@PathVariable Long id) {
        Resource resource = avatarService.getAvatar(id);

        // Détecter le type MIME depuis le nom du fichier
        String contentType = "image/jpeg"; // fallback
        String filename = resource.getFilename();
        if (filename != null) {
            if (filename.endsWith(".png")) contentType = "image/png";
            else if (filename.endsWith(".webp")) contentType = "image/webp";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                .body(resource);
    }
}