package com.fishcam.infrastructure.config;

import com.fishcam.domain.fournisseur.Fournisseur;
import com.fishcam.domain.fournisseur.FournisseurRepository;
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
    private final FournisseurRepository fournisseurRepository;

    @Override
    public void run(String... args) {
        log.info(" [DEV] Initialisation des données de test...");

        if (poissonnerieRepository.count() == 0) {
            Poissonnerie poissonnerie = new Poissonnerie();
            poissonnerie.setName("FISH CAM VILLE (TEST)");
            poissonnerie.setAddress("La Petite mosquée");
            poissonnerie.setPhone("676028800");
            poissonnerie.setActive(true);
            poissonnerie.setPretActif(true);
            poissonnerieRepository.save(poissonnerie);
            log.info(" Poissonnerie TEST créée (ID: {})", poissonnerie.getId());

            createTestUsers(poissonnerie);
        }

        if (fournisseurRepository.count() == 0) {
            Fournisseur f = new Fournisseur();
            f.setNom("CONGELCAM");
            f.setVille("Nkongsamba");
            f.setActif(true);
            fournisseurRepository.save(f);
            log.info("✅ Fournisseur CONGELCAM créé");
        }

        log.info("");
        log.info("═══════════════════════════════════════════════════");
        log.info("📱 COMPTES DE TEST DISPONIBLES :");
        log.info("═══════════════════════════════════════════════════");
        log.info("SUPER_ADMIN → Phone: 692087724 | Password: admin123");
        log.info("PATRON      → Phone: 676028800 | Password: patron123");
        log.info("CAISSIÈRE   → Phone: 690950871 | Password: caissier123");
        log.info("ENREGISTREUR→ Phone: 655032752 | Password: vendeur123");
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
            log.info(" SUPER_ADMIN créé (692087724)");
        }

        // PATRON
        if (!userRepository.existsByPhone("676028800")) {
            User patron = new User();
            patron.setFirstName("Theophile");
            patron.setLastName("FOSSO");
            patron.setPhone("676028800");
            patron.setPassword(passwordEncoder.encode("patron123"));
            patron.setRole(Role.PATRON);
            patron.setScope(UserScope.SINGLE_POISSONNERIE);
            patron.setDefaultPoissonnerie(poissonnerie);
            patron.setActive(true);
            userRepository.save(patron);
            log.info(" PATRON créé (676028800)");
        }

        // CAISSIERE
        if (!userRepository.existsByPhone("690950871")) {
            User caissier = new User();
            caissier.setFirstName("Alerte");
            caissier.setLastName("DJOKO");
            caissier.setPhone("690950871");
            caissier.setPassword(passwordEncoder.encode("caissier123"));
            caissier.setRole(Role.CAISSIERE);
            caissier.setScope(UserScope.SINGLE_POISSONNERIE);
            caissier.setDefaultPoissonnerie(poissonnerie);
            caissier.setActive(true);
            userRepository.save(caissier);
            log.info(" CAISSIERE créée (690950871)");
        }

        // ENREGISTREUR (Secrétaire)
        if (!userRepository.existsByPhone("655032752")) {
            User vendeur = new User();
            vendeur.setFirstName("Christine");
            vendeur.setLastName("Inconnu");
            vendeur.setPhone("655032752");
            vendeur.setPassword(passwordEncoder.encode("vendeur123"));
            vendeur.setRole(Role.ENREGISTREUR);
            vendeur.setScope(UserScope.SINGLE_POISSONNERIE);
            vendeur.setDefaultPoissonnerie(poissonnerie);
            vendeur.setActive(true);
            userRepository.save(vendeur);
            log.info(" ENREGISTREUR créé (655032752)");
        }
    }
}