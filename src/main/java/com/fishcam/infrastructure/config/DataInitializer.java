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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final PoissonnerieRepository poissonnerieRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        log.info("🚀 Initialisation des données...");

        // Créer une poissonnerie par défaut si elle n'existe pas
        if (poissonnerieRepository.count() == 0) {
            Poissonnerie poissonnerie = new Poissonnerie();
            poissonnerie.setName("AKWA CENTRE");
            poissonnerie.setAddress("Rue de la Joie, Akwa");
            poissonnerie.setPhone("677112233");
            poissonnerie.setActive(true);
            poissonnerieRepository.save(poissonnerie);
            log.info("✅ Poissonnerie créée: AKWA CENTRE (ID: {})", poissonnerie.getId());

            // Créer des utilisateurs de test
            createDefaultUsers(poissonnerie);
        }

        log.info("✅ Initialisation terminée");
        log.info("");
        log.info("═══════════════════════════════════════════════════");
        log.info("📱 COMPTES DE TEST DISPONIBLES :");
        log.info("═══════════════════════════════════════════════════");
        log.info("SUPER_ADMIN → Phone: 677000000 | Password: admin123");
        log.info("PATRON      → Phone: 677111111 | Password: patron123");
        log.info("CAISSIER    → Phone: 677222222 | Password: caissier123");
        log.info("VENDEUR     → Phone: 677333333 | Password: vendeur123");
        log.info("═══════════════════════════════════════════════════");
        log.info("");
    }

    private void createDefaultUsers(Poissonnerie poissonnerie) {
        // SUPER_ADMIN
        if (!userRepository.existsByPhone("677000000")) {
            User superAdmin = new User();
            superAdmin.setFirstName("Super");
            superAdmin.setLastName("Admin");
            superAdmin.setPhone("692087724");
            superAdmin.setPassword(passwordEncoder.encode("admin123"));
            superAdmin.setRole(Role.SUPER_ADMIN);
            superAdmin.setScope(UserScope.MULTI_POISSONNERIE);  // ← Accès à toutes
            superAdmin.setDefaultPoissonnerie(poissonnerie);
            superAdmin.setActive(true);
            userRepository.save(superAdmin);
            log.info("✅ SUPER_ADMIN créé");
        }

        // PATRON
        if (!userRepository.existsByPhone("677111111")) {
            User patron = new User();
            patron.setFirstName("Jean");
            patron.setLastName("Dupont");
            patron.setPhone("677111111");
            patron.setPassword(passwordEncoder.encode("patron123"));
            patron.setRole(Role.PATRON);
            patron.setScope(UserScope.SINGLE_POISSONNERIE);  // ← Une seule
            patron.setDefaultPoissonnerie(poissonnerie);
            patron.setActive(true);
            userRepository.save(patron);
            log.info("✅ PATRON créé");
        }

        // CAISSIER
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
            log.info("✅ CAISSIER créé");
        }

        // VENDEUR
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
            log.info("✅ VENDEUR créé");
        }
    }
}