package com.fishcam.infrastructure.config;

import com.fishcam.domain.poissonnerie.Poissonnerie;
import com.fishcam.domain.poissonnerie.PoissonnerieRepository;
import com.fishcam.domain.user.Role;
import com.fishcam.domain.user.User;
import com.fishcam.domain.user.UserRepository;
import com.fishcam.domain.user.UserScope;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@Profile("prod")
public class ProductionInitializer implements CommandLineRunner {

    private final PoissonnerieRepository poissonnerieRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    @Value("${fishcam.admin.initial-password:}")
    private String initialPassword;

    @Override
    public void run(String... args) {
        if (poissonnerieRepository.count() == 0) {
            log.info("📦 Premier lancement détecté — Création des données initiales...");

            Poissonnerie boutiqueCentrale = new Poissonnerie();
            boutiqueCentrale.setName("FISH CAM - Boutique VILLE");
            boutiqueCentrale.setAddress("La Petite mosquée");
            boutiqueCentrale.setPhone("676028800");
            boutiqueCentrale.setActive(true);
            boutiqueCentrale.setPretActif(false); // NOUVEAU
            boutiqueCentrale.setLoyer(BigDecimal.ZERO); // NOUVEAU
            boutiqueCentrale.setFondDeCaisseDefaut(BigDecimal.valueOf(10000)); // NOUVEAU
            poissonnerieRepository.save(boutiqueCentrale);

            Poissonnerie boutique2 = new Poissonnerie();
            boutique2.setName("FISH CAM - Boutique LELE");
            boutique2.setAddress("Adresse Boutique 2");
            boutique2.setPhone("677000002");
            boutique2.setActive(true);
            boutique2.setPretActif(false); // NOUVEAU
            boutique2.setLoyer(BigDecimal.ZERO); // NOUVEAU
            boutique2.setFondDeCaisseDefaut(BigDecimal.valueOf(10000)); // NOUVEAU
            poissonnerieRepository.save(boutique2);

            Poissonnerie boutique3 = new Poissonnerie();
            boutique3.setName("FISH CAM - Boutique BARE");
            boutique3.setAddress("Adresse Boutique 3");
            boutique3.setPhone("677000003");
            boutique3.setActive(true);
            boutique3.setPretActif(false); // NOUVEAU
            boutique3.setLoyer(BigDecimal.ZERO); // NOUVEAU
            boutique3.setFondDeCaisseDefaut(BigDecimal.valueOf(10000)); // NOUVEAU
            poissonnerieRepository.save(boutique3);

            createProductionUsers(boutiqueCentrale);
            log.info("✅ Initialisation terminée !");
        }
    }

    private void createProductionUsers(Poissonnerie boutiqueCentrale) {
        if (initialPassword == null || initialPassword.isBlank()) {
            log.error("Aucun compte SUPER_ADMIN n'a été créé : la variable d'environnement "
                    + "ADMIN_INITIAL_PASSWORD est absente ou vide. L'application est inutilisable "
                    + "tant qu'aucun administrateur n'existe. Corriger : définir "
                    + "ADMIN_INITIAL_PASSWORD dans les variables d'environnement Render, "
                    + "puis redéployer.");
            return;
        }
        if (!userRepository.existsByPhone("692087724")) {
            User superAdmin = new User();
            superAdmin.setFirstName("Super");
            superAdmin.setLastName("Admin");
            superAdmin.setPhone("692087724");
            superAdmin.setPassword(passwordEncoder.encode(initialPassword));
            superAdmin.setRole(Role.SUPER_ADMIN);
            superAdmin.setScope(UserScope.MULTI_POISSONNERIE);
            superAdmin.setDefaultPoissonnerie(boutiqueCentrale);
            superAdmin.setActive(true);
            userRepository.save(superAdmin);
        }
    }
}