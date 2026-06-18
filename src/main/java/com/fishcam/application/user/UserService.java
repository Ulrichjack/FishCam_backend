package com.fishcam.application.user;

import com.fishcam.adapter.web.dto.request.CreateUserRequest;
import com.fishcam.adapter.web.dto.request.UpdateUserRequest;
import com.fishcam.adapter.web.dto.response.UserResponse;
import com.fishcam.adapter.web.mapper.UserMapper;
import com.fishcam.domain.poissonnerie.Poissonnerie;
import com.fishcam.domain.poissonnerie.PoissonnerieRepository;
import com.fishcam.domain.user.User;
import com.fishcam.domain.user.UserRepository;
import com.fishcam.infrastructure.aop.LogAudit;
import com.fishcam.infrastructure.exception.BusinessException;
import com.fishcam.infrastructure.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PoissonnerieRepository poissonnerieRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @LogAudit(action = "CREATE", entityName = "User")
    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        log.info("Création utilisateur: {} {} ({})", request.getFirstName(), request.getLastName(), request.getPhone());

        // Vérifier si le téléphone existe déjà
        if (userRepository.existsByPhone(request.getPhone())) {
            throw new BusinessException("Ce numéro de téléphone est déjà utilisé");
        }

        User user = userMapper.toEntity(request);

        // ═══════════════════════════════════════════════════════════
        // HASHER LE MOT DE PASSE
        // ═══════════════════════════════════════════════════════════
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        user.setActive(true);

        // Gérer la poissonnerie
        if (request.getDefaultPoissonnerieId() != null) {
            Poissonnerie poissonnerie = poissonnerieRepository
                    .findById(request.getDefaultPoissonnerieId())
                    .orElseThrow(() -> new ResourceNotFoundException("Poissonnerie non trouvée"));
            user.setDefaultPoissonnerie(poissonnerie);
        }

        User savedUser = userRepository.save(user);
        log.info("✅ Utilisateur créé avec succès: ID {} - {}", savedUser.getId(), savedUser.getPhone());

        return userMapper.toResponse(savedUser);
    }

    public Page<UserResponse> getAllUsers(Pageable pageable) {
        log.debug("Récupération de tous les utilisateurs");
        return userRepository.findAll(pageable).map(userMapper::toResponse);
    }

    public UserResponse getUserById(Long id) {
        log.debug("Récupération utilisateur ID: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));
        return userMapper.toResponse(user);
    }

    @LogAudit(action = "UPDATE", entityName = "User")
    @Transactional
    public UserResponse updateUser(Long id, UpdateUserRequest request) {
        log.info("Modification utilisateur ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        // Vérifier si le nouveau téléphone n'est pas déjà utilisé
        if (request.getPhone() != null && !request.getPhone().equals(user.getPhone())) {
            if (userRepository.existsByPhone(request.getPhone())) {
                throw new BusinessException("Ce numéro de téléphone est déjà utilisé");
            }
        }

        userMapper.updateEntityFromRequest(request, user);

        // Si changement de poissonnerie
        if (request.getDefaultPoissonnerieId() != null) {
            Poissonnerie poissonnerie = poissonnerieRepository
                    .findById(request.getDefaultPoissonnerieId())
                    .orElseThrow(() -> new ResourceNotFoundException("Poissonnerie non trouvée"));
            user.setDefaultPoissonnerie(poissonnerie);
        }

        User updatedUser = userRepository.save(user);
        log.info("✅ Utilisateur modifié: ID {}", updatedUser.getId());

        return userMapper.toResponse(updatedUser);
    }

    @LogAudit(action = "DELETE", entityName = "User")
    @Transactional
    public void deleteUser(Long id) {
        log.info("Désactivation utilisateur ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        user.setActive(false);
        userRepository.save(user);

        log.info("✅ Utilisateur désactivé: ID {}", id);
    }

    @LogAudit(action = "REACTIVATE", entityName = "User")
    @Transactional
    public UserResponse reactivateUser(Long id) {
        log.info("Réactivation utilisateur ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        user.setActive(true);
        User savedUser = userRepository.save(user);

        log.info("✅ Utilisateur réactivé: ID {}", id);
        return userMapper.toResponse(savedUser);
    }


}