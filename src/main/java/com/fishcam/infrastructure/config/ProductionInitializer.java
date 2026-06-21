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
        if (!userRepository.existsByPhone("692087724")) {
            User superAdmin = new User();
            superAdmin.setFirstName("Super");
            superAdmin.setLastName("Admin");
            superAdmin.setPhone("692087724");
            superAdmin.setPassword(passwordEncoder.encode("admin123"));
            superAdmin.setRole(Role.SUPER_ADMIN);
            superAdmin.setScope(UserScope.MULTI_POISSONNERIE);
            superAdmin.setDefaultPoissonnerie(boutiqueCentrale);
            superAdmin.setActive(true);
            userRepository.save(superAdmin);
        }

        if (!userRepository.existsByPhone("676028800")) {
            User patron = new User();
            patron.setFirstName("Theophile");
            patron.setLastName("FOSSO");
            patron.setPhone("676028800");
            patron.setPassword(passwordEncoder.encode("patron123"));
            patron.setRole(Role.PATRON);
            patron.setScope(UserScope.MULTI_POISSONNERIE);
            patron.setDefaultPoissonnerie(boutiqueCentrale);
            patron.setActive(true);
            userRepository.save(patron);
        }

        if (!userRepository.existsByPhone("690950871")) {
            User caissiere = new User();
            caissiere.setFirstName("Alerte");
            caissiere.setLastName("DJOKO");
            caissiere.setPhone("690950871");
            caissiere.setPassword(passwordEncoder.encode("caissier123"));
            caissiere.setRole(Role.CAISSIERE);
            caissiere.setScope(UserScope.MULTI_POISSONNERIE);
            caissiere.setDefaultPoissonnerie(boutiqueCentrale);
            caissiere.setActive(true);
            userRepository.save(caissiere);
        }

        if (!userRepository.existsByPhone("655032752")) {
            User vendeur = new User();
            vendeur.setFirstName("Christine");
            vendeur.setLastName("Inconnu");
            vendeur.setPhone("655032752");
            vendeur.setPassword(passwordEncoder.encode("vendeur123"));
            vendeur.setRole(Role.ENREGISTREUR);
            vendeur.setScope(UserScope.MULTI_POISSONNERIE);
            vendeur.setDefaultPoissonnerie(boutiqueCentrale);
            vendeur.setActive(true);
            userRepository.save(vendeur);
        }
    }
}