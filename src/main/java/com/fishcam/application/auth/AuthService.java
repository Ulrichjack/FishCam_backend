package com.fishcam.application.auth;

import com.fishcam.adapter.web.dto.request.LoginRequest;
import com.fishcam.adapter.web.dto.response.AuthResponse;
import com.fishcam.domain.user.User;
import com.fishcam.domain.user.UserRepository;
import com.fishcam.infrastructure.exception.BusinessException;
import com.fishcam.infrastructure.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthResponse login(LoginRequest request) {
        try {
            log.info("Tentative de connexion pour: {}", request.getPhone());

            // Authentifier
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getPhone(),  // ← PHONE
                            request.getPassword()
                    )
            );

            // Récupérer l'utilisateur
            User user = userRepository.findByPhone(request.getPhone())  // ← PHONE
                    .orElseThrow(() -> new BusinessException("Utilisateur non trouvé"));

            if (!user.getActive()) {
                log.warn("Tentative de connexion sur compte désactivé: {}", request.getPhone());
                throw new BusinessException("Ce compte est désactivé");
            }

            // Générer le token
            String token = jwtService.generateToken(user);

            log.info("Connexion réussie pour: {} (ID: {}, Role: {})",
                    user.getPhone(), user.getId(), user.getRole());

            // Construire la réponse
            return AuthResponse.builder()
                    .token(token)
                    .type("Bearer")
                    .user(AuthResponse.UserInfo.builder()
                            .id(user.getId())
                            .phone(user.getPhone())  // ← PHONE
                            .firstName(user.getFirstName())
                            .lastName(user.getLastName())
                            .role(user.getRole())
                            .poissonnerieId(user.getDefaultPoissonnerie() != null
                                    ? user.getDefaultPoissonnerie().getId()
                                    : null)
                            .poissonnerieName(user.getDefaultPoissonnerie() != null
                                    ? user.getDefaultPoissonnerie().getName()
                                    : null)
                            .build())
                    .build();

        } catch (AuthenticationException e) {
            log.error("Échec d'authentification pour: {}", request.getPhone());
            throw new BusinessException("Téléphone ou mot de passe incorrect");
        }
    }

    public AuthResponse.UserInfo getCurrentUserInfo(String authHeader) {
        String token = authHeader.substring(7); // Enlever "Bearer "
        String phone = jwtService.extractPhone(token);  // ← PHONE

        User user = userRepository.findByPhone(phone)  // ← PHONE
                .orElseThrow(() -> new BusinessException("Utilisateur non trouvé"));

        return AuthResponse.UserInfo.builder()
                .id(user.getId())
                .phone(user.getPhone())  // ← PHONE
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole())
                .poissonnerieId(user.getDefaultPoissonnerie() != null
                        ? user.getDefaultPoissonnerie().getId()
                        : null)
                .poissonnerieName(user.getDefaultPoissonnerie() != null
                        ? user.getDefaultPoissonnerie().getName()
                        : null)
                .build();
    }
}