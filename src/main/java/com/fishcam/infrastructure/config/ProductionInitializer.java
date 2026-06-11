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
 * Initialisation PRODUCTION.
 * Crée les 3 boutiques + les 4 comptes de base UNE SEULE FOIS
 * au premier lancement.
 * <p>
 * Les utilisateurs devront CHANGER leur mot de passe après la
 * première connexion.
 * <p>
 * SÉCURITÉ :
 * - Les mots de passe par défaut sont simples exprès (première connexion)
 * - Le patron DOIT les changer immédiatement
 * - Le check existsByPhone() empêche la recréation si ça existe déjà
 */
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
        log.info("🚀 [PROD] Vérification de l'initialisation...");

        // Ne crée les données que si la base est vide (premier lancement)
        if (poissonnerieRepository.count() == 0) {
            log.info("📦 Premier lancement détecté — Création des données initiales...");

            // ═══════════════════════════════════════════
            // ÉTAPE 1 : Créer les 3 boutiques
            // ═══════════════════════════════════════════

            Poissonnerie boutiqueCentrale = new Poissonnerie();
            boutiqueCentrale.setName("FISH CAM - Boutique VILLE");
            boutiqueCentrale.setAddress("La Petite mosquée");
            boutiqueCentrale.setPhone("677112233");
            boutiqueCentrale.setActive(true);
            poissonnerieRepository.save(boutiqueCentrale);
            log.info("✅ Boutique Centrale créée (ID: {})", boutiqueCentrale.getId());

            Poissonnerie boutique2 = new Poissonnerie();
            boutique2.setName("FISH CAM - Boutique LELE");
            boutique2.setAddress("Adresse Boutique 2");  // Le patron modifiera
            boutique2.setPhone("677000002");
            boutique2.setActive(true);
            poissonnerieRepository.save(boutique2);
            log.info("✅ Boutique 2 créée (ID: {})", boutique2.getId());

            Poissonnerie boutique3 = new Poissonnerie();
            boutique3.setName("FISH CAM - Boutique BARE");
            boutique3.setAddress("Adresse Boutique 3");  // Le patron modifiera
            boutique3.setPhone("677000003");
            boutique3.setActive(true);
            poissonnerieRepository.save(boutique3);
            log.info("✅ Boutique 3 créée (ID: {})", boutique3.getId());

            // ═══════════════════════════════════════════
            // ÉTAPE 2 : Créer les 4 comptes de base
            // ═══════════════════════════════════════════

            createProductionUsers(boutiqueCentrale);

            log.info("");
            log.info("╔═══════════════════════════════════════════════════════╗");
            log.info("║  🐟 FISH-CAM ERP — PREMIER LANCEMENT RÉUSSI !       ║");
            log.info("╠═══════════════════════════════════════════════════════╣");
            log.info("║                                                       ║");
            log.info("║  4 comptes ont été créés avec des mots de passe       ║");
            log.info("║  temporaires. CHANGEZ-LES IMMÉDIATEMENT !            ║");
            log.info("║                                                       ║");
            log.info("║  📱 SUPER_ADMIN  → 692087724 | mdp: changermoi1     ║");
            log.info("║  📱 PATRON       → 677XXXXXX | mdp: changermoi2     ║");
            log.info("║  📱 CAISSIERE    → 677XXXXXX | mdp: changermoi3     ║");
            log.info("║  📱 ENREGISTREUR → 677XXXXXX | mdp: changermoi4     ║");
            log.info("║                                                       ║");
            log.info("║  ⚠️  CHANGEZ CES MOTS DE PASSE MAINTENANT !         ║");
            log.info("║                                                       ║");
            log.info("╚═══════════════════════════════════════════════════════╝");
        } else {
            log.info("✅ [PROD] Base déjà initialisée — Rien à faire.");
        }
    }

    private void createProductionUsers(Poissonnerie boutiqueCentrale) {

        // SUPER_ADMIN — Le développeur / administrateur technique
        if (!userRepository.existsByPhone("692087724")) {
            User superAdmin = new User();
            superAdmin.setFirstName("Admin");
            superAdmin.setLastName("FishCam");
            superAdmin.setPhone("692087724");
            superAdmin.setPassword(passwordEncoder.encode("changermoi1"));
            superAdmin.setRole(Role.SUPER_ADMIN);
            superAdmin.setScope(UserScope.MULTI_POISSONNERIE);
            superAdmin.setDefaultPoissonnerie(boutiqueCentrale);
            superAdmin.setActive(true);
            userRepository.save(superAdmin);
            log.info("✅ SUPER_ADMIN créé (692087724) — mdp temporaire !");
        }

        // PATRON — Le propriétaire des 3 boutiques
        // ⚠️ REMPLACE 677XXXXXX par le VRAI numéro du patron !
        if (!userRepository.existsByPhone("677XXXXXX")) {
            User patron = new User();
            patron.setFirstName("Prénom");       // Le patron modifiera
            patron.setLastName("Du Patron");     // Le patron modifiera
            patron.setPhone("677XXXXXX");        // ← VRAI NUMÉRO ICI
            patron.setPassword(passwordEncoder.encode("changermoi2"));
            patron.setRole(Role.PATRON);
            patron.setScope(UserScope.MULTI_POISSONNERIE); // Voit les 3 boutiques
            patron.setDefaultPoissonnerie(boutiqueCentrale);
            patron.setActive(true);
            userRepository.save(patron);
            log.info("✅ PATRON créé — mdp temporaire !");
        }

        // CAISSIERE — La vendeuse principale
        // ⚠️ REMPLACE par le VRAI numéro de la vendeuse !
        if (!userRepository.existsByPhone("677YYYYYY")) {
            User caissiere = new User();
            caissiere.setFirstName("Prénom");
            caissiere.setLastName("De la Vendeuse");
            caissiere.setPhone("677YYYYYY");     // ← VRAI NUMÉRO ICI
            caissiere.setPassword(passwordEncoder.encode("changermoi3"));
            caissiere.setRole(Role.CAISSIERE);
            caissiere.setScope(UserScope.MULTI_POISSONNERIE);
            caissiere.setDefaultPoissonnerie(boutiqueCentrale);
            caissiere.setActive(true);
            userRepository.save(caissiere);
            log.info("✅ CAISSIERE créée — mdp temporaire !");
        }

        // ENREGISTREUR — La secrétaire d'achat (15h)
        // ⚠️ REMPLACE par le VRAI numéro de la secrétaire !
        if (!userRepository.existsByPhone("677ZZZZZZ")) {
            User enregistreur = new User();
            enregistreur.setFirstName("Prénom");
            enregistreur.setLastName("De la Secrétaire");
            enregistreur.setPhone("677ZZZZZZ");  // ← VRAI NUMÉRO ICI
            enregistreur.setPassword(passwordEncoder.encode("changermoi4"));
            enregistreur.setRole(Role.ENREGISTREUR);
            enregistreur.setScope(UserScope.MULTI_POISSONNERIE); // Elle saisit pour les 3
            enregistreur.setDefaultPoissonnerie(boutiqueCentrale);
            enregistreur.setActive(true);
            userRepository.save(enregistreur);
            log.info("✅ ENREGISTREUR créée — mdp temporaire !");
        }
    }
}