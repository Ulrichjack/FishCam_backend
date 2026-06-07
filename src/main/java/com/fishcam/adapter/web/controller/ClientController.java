package com.fishcam.adapter.web.controller;

import com.fishcam.adapter.web.dto.request.CreateClientRequest;
import com.fishcam.adapter.web.dto.request.UpdateClientRequest;
import com.fishcam.adapter.web.dto.response.ApiResponse;
import com.fishcam.adapter.web.dto.response.ClientDetailResponse;
import com.fishcam.adapter.web.dto.response.ClientResponse;
import com.fishcam.application.client.ClientService;
import com.fishcam.domain.user.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/api/v1/clients")
@RequiredArgsConstructor
@Tag(name = "Clients", description = "Gestion des clients")
public class ClientController {
    private final ClientService clientService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Créer un nouveau client")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'PATRON', 'CAISSIERE', 'ENREGISTREUR')")
    public ApiResponse<ClientResponse> createClient(
            @Valid @RequestBody CreateClientRequest request,
            Authentication authentication) {  // ← CHANGEMENT ICI

        User currentUser = (User) authentication.getPrincipal();
        log.info("Création client par user ID: {}", currentUser.getId());

        ClientResponse data = clientService.createClient(request, currentUser.getId());
        return ApiResponse.<ClientResponse>builder()
                .success(true)
                .data(data)
                .message("Client créé avec succès")
                .code(201)
                .timestamp(LocalDateTime.now())
                .build();
    }

    @GetMapping("/poissonnerie/{poissonnerieId}")
    @Operation(summary = "Lister les clients d'une poissonnerie")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'PATRON', 'CAISSIERE', 'ENREGISTREUR')")
    public ApiResponse<Page<ClientResponse>> getClientsByPoissonnerie(
            @PathVariable Long poissonnerieId,
            Pageable pageable) {

        log.debug("Récupération clients poissonnerie ID: {}", poissonnerieId);
        Page<ClientResponse> data = clientService.getClientsByPoissonnerie(poissonnerieId, pageable);
        return ApiResponse.<Page<ClientResponse>>builder()
                .success(true)
                .data(data)
                .message("Clients récupérés")
                .code(200)
                .timestamp(LocalDateTime.now())
                .build();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Récupérer le détail d'un client")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'PATRON', 'CAISSIERE', 'ENREGISTREUR')")
    public ApiResponse<ClientDetailResponse> getClientDetail(@PathVariable Long id) {
        log.debug("Récupération détail client ID: {}", id);
        ClientDetailResponse data = clientService.getClientDetail(id);
        return ApiResponse.<ClientDetailResponse>builder()
                .success(true)
                .data(data)
                .message("Détail client récupéré")
                .code(200)
                .timestamp(LocalDateTime.now())
                .build();
    }

    @GetMapping("/search")
    @Operation(summary = "Rechercher des clients par nom")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'PATRON', 'CAISSIERE', 'ENREGISTREUR')")
    public ApiResponse<Page<ClientResponse>> searchClients(
            @RequestParam Long poissonnerieId,
            @RequestParam(required = false) String term,
            Pageable pageable) {

        log.debug("Recherche clients: {}", term);
        Page<ClientResponse> data = clientService.searchClients(poissonnerieId, term, pageable);
        return ApiResponse.<Page<ClientResponse>>builder()
                .success(true)
                .data(data)
                .message("Recherche clients effectuée")
                .code(200)
                .timestamp(LocalDateTime.now())
                .build();
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier un client")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'PATRON', 'CAISSIERE')")
    public ApiResponse<ClientResponse> updateClient(
            @PathVariable Long id,
            @Valid @RequestBody UpdateClientRequest request) {

        log.info("Modification client ID: {}", id);
        ClientResponse data = clientService.updateClient(id, request);
        return ApiResponse.<ClientResponse>builder()
                .success(true)
                .data(data)
                .message("Client modifié avec succès")
                .code(200)
                .timestamp(LocalDateTime.now())
                .build();
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Désactiver un client")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'PATRON')")
    public ApiResponse<Void> deleteClient(@PathVariable Long id) {
        log.info("Désactivation client ID: {}", id);
        clientService.deleteClient(id);
        return ApiResponse.<Void>builder()
                .success(true)
                .message("Client désactivé avec succès")
                .code(204)
                .timestamp(LocalDateTime.now())
                .build();
    }

    @PatchMapping("/{id}/reactivate")
    @Operation(summary = "Réactiver un client désactivé")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'PATRON')")
    public ApiResponse<ClientResponse> reactivateClient(@PathVariable Long id) {
        log.info("Réactivation client ID: {}", id);
        ClientResponse data = clientService.reactivateClient(id);
        return ApiResponse.<ClientResponse>builder()
                .success(true)
                .data(data)
                .message("Client réactivé avec succès")
                .code(200)
                .timestamp(LocalDateTime.now())
                .build();
    }

    @GetMapping("/inactive/poissonnerie/{poissonnerieId}")
    @Operation(summary = "Lister les clients désactivés d'une poissonnerie")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'PATRON','CAISSIERE', 'ENREGISTREUR')")
    public ApiResponse<Page<ClientResponse>> getInactiveClients(
            @PathVariable Long poissonnerieId,
            Pageable pageable) {

        log.debug("Récupération clients inactifs poissonnerie ID: {}", poissonnerieId);
        Page<ClientResponse> data = clientService.getInactiveClients(poissonnerieId, pageable);
        return ApiResponse.<Page<ClientResponse>>builder()
                .success(true)
                .data(data)
                .message("Clients désactivés récupérés")
                .code(200)
                .timestamp(LocalDateTime.now())
                .build();
    }
}