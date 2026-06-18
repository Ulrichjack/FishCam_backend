package com.fishcam.adapter.web.controller;

import com.fishcam.adapter.web.dto.request.CreateUserRequest;
import com.fishcam.adapter.web.dto.request.UpdateUserRequest;
import com.fishcam.adapter.web.dto.response.ApiResponse;
import com.fishcam.adapter.web.dto.response.UserResponse;
import com.fishcam.application.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Utilisateurs", description = "Gestion des accès au système (Équipe)")
public class UserController {

    private final UserService userService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Créer un utilisateur")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'PATRON')")
    public ApiResponse<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        UserResponse response = userService.createUser(request);
        return ApiResponse.<UserResponse>builder()
                .success(true).data(response).message("Utilisateur créé").code(201).timestamp(LocalDateTime.now()).build();
    }

    @GetMapping
    @Operation(summary = "Lister les utilisateurs")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'PATRON')")
    public ApiResponse<Page<UserResponse>> getAllUsers(Pageable pageable) {
        Page<UserResponse> response = userService.getAllUsers(pageable);
        return ApiResponse.<Page<UserResponse>>builder()
                .success(true).data(response).message("Liste récupérée").code(200).timestamp(LocalDateTime.now()).build();
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier un utilisateur")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'PATRON')")
    public ApiResponse<UserResponse> updateUser(@PathVariable Long id, @Valid @RequestBody UpdateUserRequest request) {
        UserResponse response = userService.updateUser(id, request);
        return ApiResponse.<UserResponse>builder()
                .success(true).data(response).message("Utilisateur modifié").code(200).timestamp(LocalDateTime.now()).build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Désactiver un utilisateur")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'PATRON')")
    public ApiResponse<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ApiResponse.<Void>builder()
                .success(true).message("Utilisateur désactivé").code(200).timestamp(LocalDateTime.now()).build();
    }


    @PatchMapping("/{id}/reactivate")
    @Operation(summary = "Réactiver un utilisateur")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'PATRON')")
    public ApiResponse<UserResponse> reactivateUser(@PathVariable Long id) {
        UserResponse response = userService.reactivateUser(id);
        return ApiResponse.<UserResponse>builder()
                .success(true)
                .data(response)
                .message("Utilisateur réactivé")
                .code(200)
                .timestamp(LocalDateTime.now())
                .build();
    }



}