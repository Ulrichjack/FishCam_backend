package com.fishcam.application.auth;

import com.fishcam.adapter.web.dto.request.ChangePasswordRequest;
import com.fishcam.adapter.web.dto.request.LoginRequest;
import com.fishcam.adapter.web.dto.request.ResetPasswordRequest;
import com.fishcam.adapter.web.dto.response.AuthResponse;
import com.fishcam.domain.user.Role;
import com.fishcam.domain.user.User;
import com.fishcam.domain.user.UserRepository;
import com.fishcam.infrastructure.exception.BusinessException;
import com.fishcam.infrastructure.exception.ResourceNotFoundException;
import com.fishcam.infrastructure.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;

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
                            .scope(user.getScope())
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
                .scope(user.getScope())
                .poissonnerieId(user.getDefaultPoissonnerie() != null
                        ? user.getDefaultPoissonnerie().getId()
                        : null)
                .poissonnerieName(user.getDefaultPoissonnerie() != null
                        ? user.getDefaultPoissonnerie().getName()
                        : null)
                .build();
    }

    /**
     * Changer SON PROPRE mot de passe.
     * Accessible par TOUT LE MONDE (connecté).
     * L'utilisateur doit connaître son ancien mot de passe.
     *
     * @param request     ancien + nouveau mot de passe
     * @param currentUser l'utilisateur connecté (depuis JWT)
     * @return message de succès
     */
    @Transactional
    public String changePassword(ChangePasswordRequest request, User currentUser) {
        // verifier ancine mot de passe est correct
        if (!passwordEncoder.matches(request.getAncienMotDePasse(), currentUser.getPassword())) {
            throw new BusinessException("Ancien mot de passe incorrect");
        }

        //2. verifier le nouveau est different de ancien
        if (passwordEncoder.matches(request.getNouveauMotDePasse(), currentUser.getPassword())) {
            throw new BusinessException("Le nouveau mot de passe doit etre different de l'ancien");
        }
        //3.encoder et sauvegarder le nouveua mot de passe
        currentUser.setPassword(passwordEncoder.encode(request.getNouveauMotDePasse()));
        userRepository.save(currentUser);
        log.info("Mot de passe change pour {} ({}) ", currentUser.getPhone(), currentUser.getRole());

        return "Mot de passe changé avec succès";
    }

    /**
     * Réinitialiser le mot de passe D'UN AUTRE utilisateur.
     * Accessible uniquement par PATRON et SUPER_ADMIN.
     * Pas besoin de l'ancien mot de passe (c'est le chef qui reset).
     * <p>
     * Cas d'usage : un employé a oublié son mot de passe,
     * le patron le réinitialise pour lui.
     * <p>
     * Sécurité : un PATRON ne peut pas reset un SUPER_ADMIN.
     *
     * @param request userId + nouveau mot de passe
     * @param admin   l'utilisateur admin connecté (depuis JWT)
     * @return message de succès avec le nom de l'utilisateur
     */
    @Transactional
    public String resetPassword(ResetPasswordRequest request, User admin) {

        // 1. Trouver l'utilisateur cible
        User targetUser = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Utilisateur non trouvé avec l'ID: " + request.getUserId()));

        // 2. Un PATRON ne peut pas reset le mdp d'un SUPER_ADMIN
        if (targetUser.getRole() == Role.SUPER_ADMIN
                && admin.getRole() != Role.SUPER_ADMIN) {
            throw new BusinessException(
                    "Seul un SUPER_ADMIN peut réinitialiser le mot de passe d'un autre SUPER_ADMIN");
        }

        // 3. Un utilisateur ne peut pas se reset lui-même via cet endpoint
        //    (il doit utiliser change-password à la place)
        if (targetUser.getId().equals(admin.getId())) {
            throw new BusinessException(
                    "Utilisez l'endpoint /change-password pour changer votre propre mot de passe");
        }

        // 4. Encoder et sauvegarder
        targetUser.setPassword(passwordEncoder.encode(request.getNouveauMotDePasse()));
        userRepository.save(targetUser);

        log.info("🔑 Mot de passe réinitialisé pour {} ({}) par {} ({})",
                targetUser.getPhone(), targetUser.getRole(),
                admin.getPhone(), admin.getRole());

        return "Mot de passe réinitialisé pour "
                + targetUser.getFirstName() + " " + targetUser.getLastName();
    }
}

