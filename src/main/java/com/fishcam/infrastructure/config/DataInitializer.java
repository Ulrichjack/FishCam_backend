package com.fishcam.infrastructure.config;

import com.fishcam.domain.client.Client;
import com.fishcam.domain.client.ClientRepository;
import com.fishcam.domain.comptecourant.CompteCourant;
import com.fishcam.domain.comptecourant.CompteCourantRepository;
import com.fishcam.domain.comptecourant.StatutCompteCourant;
import com.fishcam.domain.epargne.Epargne;
import com.fishcam.domain.epargne.EpargneRepository;
import com.fishcam.domain.fournisseur.Fournisseur;
import com.fishcam.domain.fournisseur.FournisseurRepository;
import com.fishcam.domain.livreur.Livreur;
import com.fishcam.domain.livreur.LivreurRepository;
import com.fishcam.domain.poissonnerie.Poissonnerie;
import com.fishcam.domain.poissonnerie.PoissonnerieRepository;
import com.fishcam.domain.produit.Produit;
import com.fishcam.domain.produit.ProduitRepository;
import com.fishcam.domain.produit.Unite;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
@Profile("dev")
public class DataInitializer implements CommandLineRunner {

    private final PoissonnerieRepository poissonnerieRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final FournisseurRepository fournisseurRepository;
    private final ProduitRepository produitRepository;
    private final ClientRepository clientRepository;
    private final CompteCourantRepository compteCourantRepository;
    private final EpargneRepository epargneRepository;
    private final LivreurRepository livreurRepository;

    @Override
    public void run(String... args) {
        log.info("🚀 [DEV] Initialisation des données de test...");

        if (poissonnerieRepository.count() == 0) {

            // ==========================================
            // 1. POISSONNERIES
            // ==========================================
            Poissonnerie p1 = new Poissonnerie();
            p1.setName("FISH CAM VILLE (TEST)");
            p1.setAddress("La Petite mosquée");
            p1.setPhone("676028800");
            p1.setPretActif(true);
            p1.setLoyer(BigDecimal.valueOf(50000));
            p1.setFondDeCaisseDefaut(BigDecimal.valueOf(10000));
            p1.setActive(true);
            poissonnerieRepository.save(p1);

            Poissonnerie p2 = new Poissonnerie();
            p2.setName("Poissonnerie La Référence (Bonamoussadi)");
            p2.setAddress("Marché Bonamoussadi");
            p2.setPhone("+237690000001");
            p2.setPretActif(false);
            p2.setLoyer(BigDecimal.valueOf(45000));
            p2.setFondDeCaisseDefaut(BigDecimal.valueOf(15000));
            p2.setActive(true);
            poissonnerieRepository.save(p2);

            // ==========================================
            // 2. UTILISATEURS (Tes données exactes)
            // ==========================================
            User patron = createTestUsers(p1);

            // ==========================================
            // 3. FOURNISSEURS
            // ==========================================
            Fournisseur f1 = new Fournisseur();
            f1.setNom("CONGELCAM SA");
            f1.setVille("Douala");
            f1.setTelephone("670000000");
            f1.setActif(true);
            fournisseurRepository.save(f1);

            Fournisseur f2 = new Fournisseur();
            f2.setNom("QUEEN FISH");
            f2.setVille("Yaoundé");
            f2.setTelephone("690000000");
            f2.setActif(true);
            fournisseurRepository.save(f2);

            // ==========================================
            // 4. LIVREURS
            // ==========================================
            Livreur l1 = new Livreur();
            l1.setNom("Ndjana");
            l1.setPrenom("Paul");
            l1.setTelephone("671000001");
            l1.setFournisseur(f1);
            l1.setActif(true);
            livreurRepository.save(l1);

            Livreur l2 = new Livreur();
            l2.setNom("Talla");
            l2.setPrenom("Serge");
            l2.setTelephone("671000002");
            l2.setFournisseur(f2);
            l2.setActif(true);
            livreurRepository.save(l2);

            // ==========================================
            // 5. PRODUITS
            // ==========================================
            creerProduit("Maquereau 20+ (Chinchard)", 20.00);
            creerProduit("Maquereau 25+ (Chinchard)", 20.00);
            creerProduit("Bar (Grand)", 15.00);
            creerProduit("Bar (Moyen)", 10.00);
            creerProduit("Silure (Machoiron)", 20.00);
            creerProduit("Carpe", 10.00);
            creerProduit("Saucisse (Saucisson de mer)", 10.00);

            // ==========================================
            // 6. CLIENTS, COMPTES COURANTS ET EPARGNES
            // ==========================================
            creerClientComplet("Maman", "Clementine", "690000011", "123456789", "Derrière le marché", "Akwa", p1, patron, -15000, 50000, StatutCompteCourant.ACTIF, 25000);
            creerClientComplet("Papa", "Simon", "690000012", "987654321", "Carrefour", "Deido", p1, patron, 0, 20000, StatutCompteCourant.ACTIF, 100000);
            creerClientComplet("Tante", "Marie", "690000013", "456123789", "Avenue principale", "Bonamoussadi", p2, patron, -55000, 50000, StatutCompteCourant.BLOQUE, 0);

            log.info("✅ Base de données initialisée avec succès avec toutes les données de test !");

            log.info("═══════════════════════════════════════════════════");
            log.info("📱 COMPTES DE TEST DISPONIBLES :");
            log.info("═══════════════════════════════════════════════════");
            log.info("SUPER_ADMIN → Phone: 692087724 | Password: admin123");
            log.info("PATRON      → Phone: 676028800 | Password: patron123");
            log.info("CAISSIÈRE   → Phone: 690950871 | Password: caissier123");
            log.info("ENREGISTREUR→ Phone: 655032752 | Password: vendeur123");
            log.info("═══════════════════════════════════════════════════");

        } else {
            log.info("✅ Données déjà présentes. Aucune initialisation requise.");
        }
    }

    private User createTestUsers(Poissonnerie poissonnerie) {
        // SUPER_ADMIN
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

        // PATRON
        User patron = new User();
        patron.setFirstName("Theophile");
        patron.setLastName("FOSSO");
        patron.setPhone("676028800");
        patron.setPassword(passwordEncoder.encode("patron123"));
        patron.setRole(Role.PATRON);
        patron.setScope(UserScope.MULTI_POISSONNERIE);
        patron.setDefaultPoissonnerie(poissonnerie);
        patron.setActive(true);
        userRepository.save(patron);

        // CAISSIERE
        User caissier = new User();
        caissier.setFirstName("Alerte");
        caissier.setLastName("DJOKO");
        caissier.setPhone("690950871");
        caissier.setPassword(passwordEncoder.encode("caissier123"));
        caissier.setRole(Role.CAISSIERE);
        caissier.setScope(UserScope.MULTI_POISSONNERIE);
        caissier.setDefaultPoissonnerie(poissonnerie);
        caissier.setActive(true);
        userRepository.save(caissier);

        // ENREGISTREUR
        User vendeur = new User();
        vendeur.setFirstName("Christine");
        vendeur.setLastName("Inconnu");
        vendeur.setPhone("655032752");
        vendeur.setPassword(passwordEncoder.encode("vendeur123"));
        vendeur.setRole(Role.ENREGISTREUR);
        vendeur.setScope(UserScope.MULTI_POISSONNERIE);
        vendeur.setDefaultPoissonnerie(poissonnerie);
        vendeur.setActive(true);
        userRepository.save(vendeur);

        return patron; // On retourne le patron pour l'utiliser comme créateur des clients
    }

    private void creerProduit(String nom, double poids) {
        Produit p = new Produit();
        p.setNom(nom);
        p.setUnite(Unite.KG);
        p.setPoidsParCarton(BigDecimal.valueOf(poids));
        p.setActif(true);
        produitRepository.save(p);
    }

    private void creerClientComplet(String prenom, String nom, String phone, String cni, String adresse, String quartier, Poissonnerie p, User createur, double soldeCC, double limiteCC, StatutCompteCourant statutCC, double soldeEpargne) {
        Client c = new Client();
        c.setFirstName(prenom);
        c.setLastName(nom);
        c.setPhone(phone);
        c.setCni(cni);
        c.setAddress(adresse);
        c.setQuartier(quartier);
        c.setPoissonnerie(p);
        c.setCreatedBy(createur);
        c.setActive(true);
        clientRepository.save(c);

        CompteCourant cc = new CompteCourant();
        cc.setClient(c);
        cc.setSolde(BigDecimal.valueOf(soldeCC));
        cc.setLimiteCreditMax(BigDecimal.valueOf(limiteCC));
        cc.setStatut(statutCC);
        cc.setDateOuverture(LocalDateTime.now());
        compteCourantRepository.save(cc);

        Epargne e = new Epargne();
        e.setClient(c);
        e.setCurrentBalance(BigDecimal.valueOf(soldeEpargne));
        e.setCreatedBy(createur);
        epargneRepository.save(e);
    }
}