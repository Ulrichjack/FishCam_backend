package com.fishcam.application.user;

import com.fishcam.domain.user.User;
import com.fishcam.domain.user.UserRepository;
import com.fishcam.infrastructure.exception.BusinessException;
import com.fishcam.infrastructure.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AvatarService {

    private final UserRepository userRepository;

    @Value("${fishcam.upload.avatar-dir}")
    private String avatarDir;

    // Extensions autorisées
    private static final List<String> ALLOWED_TYPES = List.of(
            "image/jpeg", "image/jpg", "image/png", "image/webp"
    );

    private static final List<String> ALLOWED_EXTENSIONS = List.of(
            "jpg", "jpeg", "png", "webp"
    );

    @Transactional
    public String uploadAvatar(Long userId, MultipartFile file) {
        // Vérifier que l'utilisateur existe
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        // Vérifier le type de fichier
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            throw new BusinessException("Format non supporté. Utilisez JPG, PNG ou WEBP.");
        }

        String extension = getExtension(file.getOriginalFilename());
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BusinessException("Extension non autorisée. Utilisez .jpg, .png ou .webp");
        }

        // Vérifier la taille (max 5MB)
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new BusinessException("L'image ne doit pas dépasser 5MB");
        }

        try {
            // Créer le dossier si inexistant
            Path uploadPath = Paths.get(avatarDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                log.info("Dossier avatars créé : {}", uploadPath.toAbsolutePath());
            }

            // Supprimer l'ancien avatar si existant
            if (user.getAvatarPath() != null) {
                Path oldFile = Paths.get(user.getAvatarPath());
                Files.deleteIfExists(oldFile);
                log.debug("Ancien avatar supprimé : {}", oldFile);
            }

            // Générer un nom unique
            String fileName = UUID.randomUUID() + "." + extension;
            Path filePath = uploadPath.resolve(fileName);

            // Sauvegarder le fichier
            Files.copy(file.getInputStream(), filePath);

            // Mettre à jour l'entité
            String relativePath = avatarDir + "/" + fileName;
            user.setAvatarPath(relativePath);
            userRepository.save(user);

            log.info("Avatar uploadé pour user {} : {}", userId, relativePath);
            return relativePath;

        } catch (IOException e) {
            log.error("Erreur lors de l'upload de l'avatar", e);
            throw new BusinessException("Erreur lors de la sauvegarde de l'image");
        }
    }

    public Resource getAvatar(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        if (user.getAvatarPath() == null) {
            throw new ResourceNotFoundException("Cet utilisateur n'a pas de photo de profil");
        }

        try {
            Path filePath = Paths.get(user.getAvatarPath());
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists()) {
                // Le fichier a été supprimé manuellement → nettoyer la BDD
                user.setAvatarPath(null);
                userRepository.save(user);
                throw new ResourceNotFoundException("Fichier image introuvable");
            }

            return resource;

        } catch (MalformedURLException e) {
            throw new ResourceNotFoundException("Chemin d'image invalide");
        }
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "jpg";
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }
}