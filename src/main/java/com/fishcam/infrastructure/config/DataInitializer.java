package com.fishcam.infrastructure.config;

import com.fishcam.domain.poissonnerie.Poissonnerie;
import com.fishcam.domain.poissonnerie.PoissonnerieRepository;
import com.fishcam.domain.user.Role;
import com.fishcam.domain.user.User;
import com.fishcam.domain.user.UserRepository;
import com.fishcam.domain.user.UserScope;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Données de TEST uniquement.
 * Ne tourne QU'EN DEV (jamais en production).
 * Crée des faux utilisateurs pour tester l'API.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@Profile("dev")
public class DataInitializer implements CommandLineRunner {

    private final PoissonnerieRepository poissonnerieRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        log.info("🚀 [DEV] Initialisation des données de test...");

        if (poissonnerieRepository.count() == 0) {
            Poissonnerie poissonnerie = new Poissonnerie();
            poissonnerie.setName("FISH CAM VILLE (TEST)");
            poissonnerie.setAddress("La Petite mosquée");
            poissonnerie.setPhone("677112233");
            poissonnerie.setActive(true);
            poissonnerieRepository.save(poissonnerie);
            log.info("✅ Poissonnerie TEST créée (ID: {})", poissonnerie.getId());

            createTestUsers(poissonnerie);
        }

        log.info("");
        log.info("═══════════════════════════════════════════════════");
        log.info("📱 COMPTES DE TEST DISPONIBLES :");
        log.info("═══════════════════════════════════════════════════");
        log.info("SUPER_ADMIN → Phone: 692087724 | Password: admin123");
        log.info("PATRON      → Phone: 677111111 | Password: patron123");
        log.info("CAISSIERE   → Phone: 677222222 | Password: caissier123");
        log.info("ENREGISTREUR→ Phone: 677333333 | Password: vendeur123");
        log.info("═══════════════════════════════════════════════════");
    }

    private void createTestUsers(Poissonnerie poissonnerie) {
        // SUPER_ADMIN
        if (!userRepository.existsByPhone("692087724")) {
            User superAdmin = new User();
            superAdmin.setFirstName("Super");
            superAdmin.setLastName("Admin");
            superAdmin.setPhone("692087724");
            superAdmin.setPassword(passwordEncoder.encode("admin123"));
            superAdmin.setRole(Role.SUPER_ADMIN);
            superAdmin.setScope(UserScope.MULTI_POISSONNERIE);
            superAdmin.setDefaultPoissonnerie(poissonnerie);
            superAdmin.setActive(true);
            userRepository.save(superAdmin);
            log.info("✅ SUPER_ADMIN créé (692087724)");
        }

        // PATRON
        if (!userRepository.existsByPhone("677111111")) {
            User patron = new User();
            patron.setFirstName("Jean");
            patron.setLastName("Dupont");
            patron.setPhone("677111111");
            patron.setPassword(passwordEncoder.encode("patron123"));
            patron.setRole(Role.PATRON);
            patron.setScope(UserScope.SINGLE_POISSONNERIE);
            patron.setDefaultPoissonnerie(poissonnerie);
            patron.setActive(true);
            userRepository.save(patron);
            log.info("✅ PATRON créé (677111111)");
        }

        // CAISSIERE
        if (!userRepository.existsByPhone("677222222")) {
            User caissier = new User();
            caissier.setFirstName("Marie");
            caissier.setLastName("Kamga");
            caissier.setPhone("677222222");
            caissier.setPassword(passwordEncoder.encode("caissier123"));
            caissier.setRole(Role.CAISSIERE);
            caissier.setScope(UserScope.SINGLE_POISSONNERIE);
            caissier.setDefaultPoissonnerie(poissonnerie);
            caissier.setActive(true);
            userRepository.save(caissier);
            log.info("✅ CAISSIERE créée (677222222)");
        }

        // ENREGISTREUR (Secrétaire)
        if (!userRepository.existsByPhone("677333333")) {
            User vendeur = new User();
            vendeur.setFirstName("Paul");
            vendeur.setLastName("Ndongo");
            vendeur.setPhone("677333333");
            vendeur.setPassword(passwordEncoder.encode("vendeur123"));
            vendeur.setRole(Role.ENREGISTREUR);
            vendeur.setScope(UserScope.SINGLE_POISSONNERIE);
            vendeur.setDefaultPoissonnerie(poissonnerie);
            vendeur.setActive(true);
            userRepository.save(vendeur);
            log.info("✅ ENREGISTREUR créé (677333333)");
        }
    }
}