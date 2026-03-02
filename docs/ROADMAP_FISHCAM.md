# 🗺️ ROADMAP COMPLÈTE — FISH-CAM ERP BACKEND

> **Application Spring Boot de gestion de poissonnerie**
> Repo : `Ulrichjack/FishCam_backend`
> Dernière mise à jour : 2026-03-01

---

## 📌 RÉSUMÉ GLOBAL — CE QUI EST FAIT ET CE QUI MANQUE

### ✅ DÉJÀ FAIT

| # | Module | Détails |
|---|--------|---------|
| 1 | **Auth & Sécurité** | JWT (login par téléphone), Spring Security, sessions stateless |
| 2 | **Gestion Clients** | CRUD complet, lien Client → Poissonnerie |
| 3 | **Comptes Courants (Dettes)** | Emprunts, remboursements, alerte seuil -5000 FCFA, limite crédit |
| 4 | **Épargnes** | Dépôts, retraits, transfert épargne → compte courant |
| 5 | **Notifications** | Alertes auto (seuil dette, compte soldé), rapport journalier |
| 6 | **Multi-poissonneries** | Entité Poissonnerie, UserScope (SINGLE/MULTI) |
| 7 | **Gestion Utilisateurs** | Rôles (SUPER_ADMIN, PATRON, CAISSIERE, ENREGISTREUR) |

### ❌ CE QUI MANQUE

| Phase | Module | Priorité | Durée |
|-------|--------|----------|-------|
| 0 | **Nettoyage Git + Corrections bugs** | 🔴 CRITIQUE | 1-2 jours |
| 1 | **Achats, Produits & Historique des Prix** | 🔴 HAUTE | 1-2 semaines |
| 2 | **Dépenses Quotidiennes** | 🔴 HAUTE | 3-4 jours |
| 3 | **Bilan Journalier** | 🔴 HAUTE | 1 semaine |
| 4 | **Clôture Mensuelle (bouton fin de mois)** | 🟡 MOYENNE | 1 semaine |
| 5 | **Fournisseur & Livreurs** | 🟡 MOYENNE | 3-4 jours |
| 6 | **Prêts Multi-Boutiques** | 🟡 MOYENNE | 3-4 jours |
| 7 | **Rapports, Statistiques & Export PDF** | 🟢 BASSE | 1 semaine |
| 8 | **Tests Unitaires + Swagger + Release** | 🟢 BASSE | 3-4 jours |

**Durée totale estimée : 6-8 semaines**

---

## 🌿 STRATÉGIE GIT

### Règles

1. **JAMAIS** coder sur `main`
2. Créer des branches depuis `develop`
3. Merger dans `develop` quand la feature est finie
4. Merger `develop` dans `main` quand tout est stable

### Branches prévues

```
main (ne JAMAIS coder ici)
  └── develop (branche de travail quotidien)
        ├── chore/cleanup-repo
        ├── fix/data-initializer-phone
        ├── fix/client-notes-length
        ├── fix/remove-duplicate-poissonnerie
        ├── fix/notification-startup-check
        ├── refactor/fournisseur-package
        ├── docs/update-readme
        ├── feature/user-avatar
        ├── feature/security-swagger
        ├── feature/achats-produits
        ├── feature/depenses-quotidiennes
        ├── feature/bilan-journalier
        ├── feature/cloture-mensuelle
        ├── feature/livreurs-evaluation
        ├── feature/prets-multi-boutiques
        ├── feature/rapports-statistiques
        └── feature/tests-finaux
```

### Convention des commits

| Préfixe | Usage | Exemple |
|---------|-------|---------|
| `feat:` | Nouvelle fonctionnalité | `feat: add Produit entity` |
| `fix:` | Correction de bug | `fix: correct phone in DataInitializer` |
| `refactor:` | Restructuration | `refactor: move Fournisseur to own package` |
| `chore:` | Maintenance | `chore: remove temp files` |
| `test:` | Tests | `test: add CompteCourant tests` |
| `docs:` | Documentation | `docs: update README` |

---

## ═══════════════════════════════════════════════════════
## PHASE 0 : NETTOYAGE + CORRECTIONS (1-2 jours)
## ═══════════════════════════════════════════════════════

### 0.1 — Créer branche `develop`

```bash
git checkout main && git pull origin main
git checkout -b develop
git push origin develop
```

### 0.2 — Supprimer fichiers inutiles

**Branche** : `chore/cleanup-repo`

Fichiers à supprimer du repo :

| Fichier | Pourquoi |
|---------|----------|
| `commit.txt` | Notes personnelles |
| `question.txt` | Questions perso |
| `fishcam_terminal.txt` | Raccourcis terminal perso |
| `supprimer.sh` | Script temporaire |
| `EVALUATION_ET_SUGGESTIONS.md` | Évaluation externe |
| `GUIDE_IMPLEMENTATION_PRATIQUE.md` | Guide perso |
| `How to Write Good Git Commits.md` | Guide perso |
| `guide.odt` | Document LibreOffice |
| `.bashrc` | Config terminal perso |

Commandes :

```bash
git checkout develop
git checkout -b chore/cleanup-repo
git rm commit.txt question.txt fishcam_terminal.txt supprimer.sh
git rm EVALUATION_ET_SUGGESTIONS.md GUIDE_IMPLEMENTATION_PRATIQUE.md
git rm "How to Write Good Git Commits.md" guide.odt .bashrc
git commit -m "chore: remove personal/temporary files from repository"
```

Ajouter au `.gitignore` :

```
# Personal files
*.odt
*.txt
!README.txt
.bashrc
```

```bash
git add .gitignore
git commit -m "chore: update .gitignore to exclude personal files"
git checkout develop
git merge chore/cleanup-repo
git push origin develop
```

### 0.3 — Corriger bug DataInitializer

**Branche** : `fix/data-initializer-phone`

**Bug** : `existsByPhone("677000000")` mais user créé avec `setPhone("692087724")`
→ Super admin recréé à chaque redémarrage de l'application !

**Corrections** :
- Mettre le même numéro dans le check ET la création
- Ajouter `@Profile("dev")` pour que ça ne tourne pas en production

```bash
git checkout develop
git checkout -b fix/data-initializer-phone
# Faire les corrections dans DataInitializer.java
git commit -m "fix: correct phone number mismatch in DataInitializer"
git commit -m "feat: add @Profile dev to DataInitializer for safety"
git checkout develop
git merge fix/data-initializer-phone
git push origin develop
```

### 0.4 — Corriger Client notes length

**Branche** : `fix/client-notes-length`

**Bug** : `@Column(length = 20)` pour des notes, c'est trop court (20 caractères !)

**Correction** : Changer en `@Column(length = 1000)`

```bash
git checkout develop
git checkout -b fix/client-notes-length
# Modifier Client.java
git commit -m "fix: increase Client notes column length from 20 to 1000"
git checkout develop
git merge fix/client-notes-length
git push origin develop
```

### 0.5 — Supprimer poissonnerie en double

**Branche** : `fix/remove-duplicate-poissonnerie`

**Problème** : `CompteCourant` et `Epargne` ont chacun un lien direct vers
`Poissonnerie`, alors qu'on peut récupérer via `client.getPoissonnerie()`.
C'est une duplication inutile.

**Correction** :
- Supprimer `poissonnerie_id` de `CompteCourant` et `Epargne`
- Utiliser `getClient().getPoissonnerie()` à la place dans les services
- Mettre à jour les requêtes dans les repositories

```bash
git checkout develop
git checkout -b fix/remove-duplicate-poissonnerie
# Modifier CompteCourant.java, Epargne.java et leurs services
git commit -m "refactor: remove duplicate poissonnerie link from CompteCourant and Epargne"
git checkout develop
git merge fix/remove-duplicate-poissonnerie
git push origin develop
```

### 0.6 — Réorganiser Fournisseur

**Branche** : `refactor/fournisseur-package`

**Problème** : `Fournisseur.java` est directement dans `domain/` au lieu de
`domain/fournisseur/`. Et il manque des champs importants.

**Corrections** :
- Déplacer `domain/Fournisseur.java` → `domain/fournisseur/Fournisseur.java`
- Déplacer `FournisseurRepository` aussi
- Ajouter champs : `telephone`, `createdAt`, lien `Poissonnerie`

```bash
git checkout develop
git checkout -b refactor/fournisseur-package
git commit -m "refactor: move Fournisseur to domain/fournisseur package"
git commit -m "feat: add phone, createdAt and Poissonnerie link to Fournisseur"
git checkout develop
git merge refactor/fournisseur-package
git push origin develop
```

### 0.7 — Améliorer sécurité + Swagger sur code existant

**Branche** : `feat/security-swagger`

**Ce qu'il faut faire** :
- Ajouter `@PreAuthorize` sur TOUS les controllers existants (Client, CompteCourant, Epargne, Notification, User)
- Ajouter `@Operation` et `@ApiResponse` Swagger/OpenAPI sur les endpoints existants
- Vérifier que les rôles sont bien appliqués partout

```bash
git checkout develop
git checkout -b feat/security-swagger
git commit -m "feat: add @PreAuthorize role-based access on all existing controllers"
git commit -m "docs: add Swagger @Operation annotations on all existing endpoints"
git checkout develop
git merge feat/security-swagger
git push origin develop
```

### 0.8 — Ajouter image profil utilisateur

**Branche** : `feature/user-avatar`

**Fonctionnalité** : Chaque utilisateur peut avoir une photo de profil.
Comme c'est sur un seul PC, on stocke les images localement.

**Implémentation** :
- Ajouter champ `avatarPath` (String) sur l'entité `User`
- Créer dossier `fishcam-data/avatars/` configurable dans `application.yml`
- Nom fichier = `UUID.randomUUID()` + extension (jamais de doublon)
- Endpoint `POST /api/v1/users/{id}/avatar` → upload image
- Endpoint `GET /api/v1/users/{id}/avatar` → récupérer image
- Le frontend envoie l'image, le backend la sauvegarde et renvoie le chemin

Structure dossier sur le PC :

```
fishcam-data/
  └── avatars/
      ├── 550e8400-e29b-41d4-a716-446655440000.jpg
      ├── 6ba7b810-9dad-11d1-80b4-00c04fd430c8.jpg
      └── f47ac10b-58cc-4372-a567-0e02b2c3d479.jpg
```

Configuration dans `application.yml` :

```yaml
fishcam:
  upload:
    avatar-dir: fishcam-data/avatars
```

```bash
git checkout develop
git checkout -b feature/user-avatar
git commit -m "feat: add avatarPath field to User entity"
git commit -m "feat: add avatar upload and download endpoints"
git commit -m "feat: configure local file storage for avatars"
git checkout develop
git merge feature/user-avatar
git push origin develop
```

### 0.9 — Corriger les notifications pour PC pas toujours allumé

**Branche** : `fix/notification-startup-check`

**Problème** : `@Scheduled(cron = "0 0 19 * * *")` ne se déclenche que si
le PC est allumé et l'application tourne à 19h. Si le PC est éteint → pas
de rapport.

**Solution** :
1. Garder le `@Scheduled` pour quand le PC est allumé à 19h
2. Ajouter `@EventListener(ApplicationReadyEvent.class)` au démarrage
   → Vérifie si les journées précédentes ont été clôturées
   → Si non → crée une notification "⚠️ Journée du XX non clôturée"
3. Le rapport se génère AUSSI quand le patron clique "Clôturer la journée"
   → Comme ça, peu importe l'heure ou si le PC était éteint

**Pour les tests** (changer temporairement) :

```java
// Toutes les 2 minutes (pour tester) :
@Scheduled(cron = "0 */2 * * * *")

// Toutes les 30 secondes (pour tester vite) :
@Scheduled(fixedRate = 30000)

// Production (tous les jours à 19h) :
@Scheduled(cron = "0 0 19 * * *")
```

```bash
git checkout develop
git checkout -b fix/notification-startup-check
git commit -m "feat: add startup check for unclosed previous days"
git commit -m "feat: generate daily report on manual day close"
git checkout develop
git merge fix/notification-startup-check
git push origin develop
```

### 0.10 — Mettre à jour README

**Branche** : `docs/update-readme`

Mettre à jour le README.MD avec :
- Description complète du projet Fish-Cam ERP
- Liste des modules (faits et à venir)
- Technologies utilisées
- Comment lancer le projet
- Structure du projet

```bash
git checkout develop
git checkout -b docs/update-readme
git commit -m "docs: update README with complete project description and modules"
git checkout develop
git merge docs/update-readme
git push origin develop
```

### ✅ Fin Phase 0 — Merger develop dans main

```bash
git checkout main
git merge develop
git push origin main
```

**Résultat** : Repo propre, bugs corrigés, sécurité renforcée, prêt pour
les nouvelles fonctionnalités.

---

## ═══════════════════════════════════════════════════════
## PHASE 1 : ACHATS, PRODUITS & HISTORIQUE DES PRIX (1-2 semaines)
## Remplacer le carnet des achats (Image 2)
## ═══════════════════════════════════════════════════════

**Branche** : `feature/achats-produits`

```bash
git checkout develop
git checkout -b feature/achats-produits
```

### 1.1 — Entité Produit (Catalogue des poissons)

| Champ | Type | Exemple |
|-------|------|---------|
| id | Long (auto) | 1 |
| nom | String (100) | "JAX 23+" |
| categorie | String (50) | "Poisson congelé" |
| unite | String (20) | "kg" |
| dernierPrixAchat | BigDecimal | 100 500 |
| dernierPrixVente | BigDecimal | 107 500 |
| actif | Boolean | true |
| poissonnerie | ManyToOne → Poissonnerie | Boutique Centrale |
| createdAt | LocalDateTime | auto |
| updatedAt | LocalDateTime | auto |

### 1.2 — Entité HistoriquePrix (Tracer les changements de prix)

| Champ | Type | Exemple |
|-------|------|---------|
| id | Long (auto) | 1 |
| produit | ManyToOne → Produit | JAX 23+ |
| dateChangement | LocalDate | 2026-03-08 |
| ancienPrixAchat | BigDecimal | 100 500 |
| nouveauPrixAchat | BigDecimal | 95 000 |
| ancienPrixVente | BigDecimal | 107 500 |
| nouveauPrixVente | BigDecimal | 102 000 |
| modifiePar | ManyToOne → User | Secrétaire |
| createdAt | LocalDateTime | auto |

**Fonctionnement** : Quand la secrétaire enregistre un achat et que le prix
a changé par rapport au dernier prix enregistré, le système :
1. Sauvegarde l'ancien prix dans HistoriquePrix automatiquement
2. Met à jour dernierPrixAchat et dernierPrixVente sur le Produit
3. La prochaine fois, le nouveau prix est prérempli

### 1.3 — Entité AchatJournalier (En-tête = une page du carnet)

| Champ | Type | Source |
|-------|------|--------|
| id | Long (auto) | Auto |
| dateAchat | LocalDate | Saisi |
| poissonnerie | ManyToOne → Poissonnerie | Saisi (secrétaire choisit la boutique) |
| totalPrixAchat | BigDecimal | Calculé auto (somme des lignes) |
| totalPrixVentePrevisible | BigDecimal | Calculé auto (somme des lignes) |
| enregistrePar | ManyToOne → User | Auto (depuis JWT) |
| createdAt | LocalDateTime | Auto |

### 1.4 — Entité LigneAchat (Chaque ligne du carnet)

| Champ | Type | Exemple |
|-------|------|---------|
| id | Long (auto) | 1 |
| achatJournalier | ManyToOne → AchatJournalier | (la page du jour) |
| produit | ManyToOne → Produit | JAX 23+ |
| quantiteCartons | Integer | 3 |
| poidsKg | BigDecimal | 63.0 |
| prixAchat | BigDecimal | 100 500 |
| prixVente | BigDecimal | 107 500 |

### 1.5 — Fonctionnalités clés UX pour la secrétaire

**A. Recherche rapide (autocomplétion)** :
- La secrétaire tape "JA" → le système propose "JAX 23+", "JAX 15+"
- Elle tape "MA" → "MAC 315", "MAC 10+"
- Elle tape "B" → "BW", "BARS Holl"
- Endpoint : `GET /api/v1/produits/search?q=JA&poissonnerieId=1`

**B. Préremplissage des prix** :
- Quand elle sélectionne un produit, le dernier prix d'achat et de vente
  sont préremplis automatiquement
- Elle n'a qu'à modifier si le prix a changé cette semaine

**C. Mise à jour automatique des prix** :
- Si elle modifie le prix → l'historique est créé automatiquement
- Le produit est mis à jour avec le nouveau prix
- La prochaine saisie utilisera ce nouveau prix

**D. Totaux automatiques** :
- totalPrixAchat = somme de toutes les lignes.prixAchat
- totalPrixVentePrevisible = somme de toutes les lignes.prixVente
- Calculés automatiquement à l'enregistrement

### 1.6 — Fichiers à créer

```
domain/produit/Produit.java
domain/produit/ProduitRepository.java
domain/produit/HistoriquePrix.java
domain/produit/HistoriquePrixRepository.java
domain/achat/AchatJournalier.java
domain/achat/AchatJournalierRepository.java
domain/achat/LigneAchat.java
domain/achat/LigneAchatRepository.java
application/produit/ProduitService.java
application/achat/AchatJournalierService.java
adapter/web/controller/ProduitController.java
adapter/web/controller/AchatJournalierController.java
adapter/web/dto/request/CreateProduitRequest.java
adapter/web/dto/request/UpdatePrixRequest.java
adapter/web/dto/request/CreateAchatJournalierRequest.java
adapter/web/dto/request/LigneAchatRequest.java
adapter/web/dto/response/ProduitResponse.java
adapter/web/dto/response/HistoriquePrixResponse.java
adapter/web/dto/response/AchatJournalierResponse.java
adapter/web/dto/response/LigneAchatResponse.java
adapter/web/mapper/ProduitMapper.java
adapter/web/mapper/AchatMapper.java
```

### 1.7 — Endpoints

| Méthode | URL | Rôle | Description |
|---------|-----|------|-------------|
| POST | `/api/v1/produits` | PATRON | Créer un produit |
| GET | `/api/v1/produits/poissonnerie/{id}` | Tous | Liste complète des produits |
| GET | `/api/v1/produits/search?q=&poissonnerieId=` | Tous | Recherche rapide (autocomplétion) |
| PUT | `/api/v1/produits/{id}/prix` | PATRON, ENREGISTREUR | Changer le prix manuellement |
| GET | `/api/v1/produits/{id}/historique-prix?mois=&annee=` | PATRON | Voir l'évolution du prix |
| POST | `/api/v1/achats` | PATRON, ENREGISTREUR | Créer achat journalier avec lignes |
| GET | `/api/v1/achats/poissonnerie/{id}/jour?date=` | Tous | Achats d'un jour |
| GET | `/api/v1/achats/poissonnerie/{id}/mois?mois=&annee=` | PATRON | Tous les achats du mois |
| GET | `/api/v1/achats/{id}` | Tous | Détail d'un achat avec ses lignes |

### 1.8 — Tests unitaires

1. ✅ Créer un achat journalier avec 5 lignes → totaux calculés correctement
2. ✅ Les totaux correspondent à la somme des lignes
3. ✅ Changement de prix → historique créé automatiquement
4. ✅ Recherche rapide "JA" → trouve "JAX 23+"
5. ❌ Créer un achat pour une boutique inexistante → ResourceNotFoundException
6. ❌ Créer un achat avec montant négatif → BusinessException
7. ✅ Récupérer les achats du mois → liste correcte

```bash
git commit -m "feat: add Produit entity with dernierPrix fields"
git commit -m "feat: add HistoriquePrix entity for price tracking"
git commit -m "feat: add AchatJournalier and LigneAchat entities"
git commit -m "feat: add ProduitService with search and price update"
git commit -m "feat: add AchatJournalierService with auto totals and price history"
git commit -m "feat: add ProduitController with search endpoint"
git commit -m "feat: add AchatController with CRUD endpoints"
git commit -m "test: add Produit and Achat unit tests"
git checkout develop
git merge feature/achats-produits
git push origin develop
```

---

## ═══════════════════════════════════════════════════════
## PHASE 2 : DÉPENSES QUOTIDIENNES (3-4 jours)
## Transport, ration, glace... saisis par Patron + Vendeuse
## ═══════════════════════════════════════════════════════

**Branche** : `feature/depenses-quotidiennes`

```bash
git checkout develop
git checkout -b feature/depenses-quotidiennes
```

### 2.1 — Entité DepenseQuotidienne

| Champ | Type | Exemple |
|-------|------|---------|
| id | Long (auto) | 1 |
| dateDepense | LocalDate | 2026-03-15 |
| type | Enum (TypeDepenseQuotidienne) | TRANSPORT |
| montant | BigDecimal | 5 000 |
| description | String (500) | "Transport marchandise depuis Douala" |
| poissonnerie | ManyToOne → Poissonnerie | Boutique Centrale |
| saisiPar | ManyToOne → User | Patron ou Vendeuse |
| createdAt | LocalDateTime | auto |

### 2.2 — Enum TypeDepenseQuotidienne

```java
TRANSPORT("Transport des marchandises"),
RATION("Repas des employés"),
GLACE("Glace pour congélateurs"),
CARBURANT("Carburant"),
AUTRE("Autre dépense")
```

### 2.3 — Fichiers à créer

```
domain/depense/DepenseQuotidienne.java
domain/depense/TypeDepenseQuotidienne.java
domain/depense/DepenseQuotidienneRepository.java
application/depense/DepenseQuotidienneService.java
adapter/web/controller/DepenseQuotidienneController.java
adapter/web/dto/request/CreateDepenseRequest.java
adapter/web/dto/response/DepenseResponse.java
adapter/web/mapper/DepenseMapper.java
```

### 2.4 — Endpoints

| Méthode | URL | Rôle | Description |
|---------|-----|------|-------------|
| POST | `/api/v1/depenses` | PATRON, CAISSIERE | Ajouter une dépense |
| GET | `/api/v1/depenses/poissonnerie/{id}/jour?date=` | PATRON, CAISSIERE | Dépenses du jour |
| GET | `/api/v1/depenses/poissonnerie/{id}/mois?mois=&annee=` | PATRON | Total par type sur le mois |
| DELETE | `/api/v1/depenses/{id}` | PATRON | Supprimer une dépense (erreur) |

### 2.5 — Repository avec requêtes agrégées

```java
// Somme des dépenses par type pour un mois
@Query("SELECT COALESCE(SUM(d.montant), 0) FROM DepenseQuotidienne d " +
       "WHERE d.poissonnerie.id = :poissonnerieId " +
       "AND d.type = :type " +
       "AND MONTH(d.dateDepense) = :mois " +
       "AND YEAR(d.dateDepense) = :annee")
BigDecimal sumByPoissonnerieAndTypeAndMois(Long poissonnerieId,
    TypeDepenseQuotidienne type, int mois, int annee);
```

### 2.6 — Tests

1. ✅ Créer dépense transport → OK
2. ✅ Total par type sur le mois → correct
3. ❌ Montant négatif → erreur
4. ✅ Patron peut supprimer, vendeuse ne peut pas

```bash
git commit -m "feat: add DepenseQuotidienne entity, enum and repository"
git commit -m "feat: add DepenseQuotidienneService with monthly aggregation"
git commit -m "feat: add DepenseController with CRUD endpoints"
git commit -m "test: add DepenseQuotidienneService unit tests"
git checkout develop
git merge feature/depenses-quotidiennes
git push origin develop
```

---

## ═══════════════════════════════════════════════════════
## PHASE 3 : BILAN JOURNALIER (1 semaine)
## Le tableau du soir du patron — Remplacer Image 1
## ═══════════════════════════════════════════════════════

**Branche** : `feature/bilan-journalier`

```bash
git checkout develop
git checkout -b feature/bilan-journalier
```

### 3.1 — Entité BilanJournalier

| Champ | Type | Source | Qui |
|-------|------|--------|-----|
| id | Long (auto) | Auto | |
| dateBilan | LocalDate | Auto | |
| poissonnerie | ManyToOne → Poissonnerie | Auto | |
| montantAchat | BigDecimal | **Calculé auto** depuis AchatJournalier | Système |
| ventePrevisible | BigDecimal | **Calculé auto** depuis AchatJournalier | Système |
| venteRealisee | BigDecimal | **Saisi manuellement** | **Patron le soir** |
| totalDepensesJour | BigDecimal | **Calculé auto** depuis DepenseQuotidienne | Système |
| beneficeBrutJour | BigDecimal | **Calculé auto** (vente - achat - dépenses) | Système |
| nombreDettesCreees | Integer | **Calculé auto** depuis CompteCourant | Système |
| montantDettesCreees | BigDecimal | **Calculé auto** depuis CompteCourant | Système |
| montantRemboursementsJour | BigDecimal | **Calculé auto** depuis CompteCourant | Système |
| saisiPar | ManyToOne → User | Auto (JWT) | Patron |
| cloture | Boolean | false → true | Patron |
| createdAt | LocalDateTime | Auto | |

### 3.2 — Fichiers à créer

```
domain/bilan/BilanJournalier.java
domain/bilan/BilanJournalierRepository.java
application/bilan/BilanJournalierService.java
adapter/web/controller/BilanJournalierController.java
adapter/web/dto/request/CloturerJourneeRequest.java
adapter/web/dto/response/BilanJournalierResponse.java
adapter/web/dto/response/BilanConsolideResponse.java
adapter/web/mapper/BilanMapper.java
```

### 3.3 — Endpoints

| Méthode | URL | Rôle | Description |
|---------|-----|------|-------------|
| GET | `/api/v1/bilans/preparer?poissonnerieId=&date=` | PATRON | Voir calculs auto avant clôture |
| POST | `/api/v1/bilans/cloturer` | PATRON | Saisir vente réalisée + valider |
| GET | `/api/v1/bilans/poissonnerie/{id}/mois?mois=&annee=` | PATRON | Tableau mensuel (= Image 1) |
| GET | `/api/v1/bilans/consolide/jour?date=` | PATRON | Vue 3 boutiques du jour |

### 3.4 — Vue consolidée 3 boutiques

```
┌─────────────────────────────────────────────────────────────────┐
│  📊 BILAN CONSOLIDÉ — 15/03/2026                               │
│                                                                  │
│  Boutique Centrale : Ventes 183 500 | Dépenses 10 000 | Dettes 15 000
│  Boutique 2        : Ventes  80 000 | Dépenses  0     | Dettes 0
│  Boutique 3        : Ventes  65 000 | Dépenses  2 000 | Dettes 0
│  ────────────────────────────────────────────────────────────── │
│  TOTAL             : Ventes 328 500 | Dépenses 12 000 | Dettes 15 000
│                                                                  │
│  💰 Total à récupérer par le patron : 328 500 - 12 000 = 316 500
└─────────────────────────────────────────────────────────────────┘
```

### 3.5 — Tests

1. ✅ Préparer un bilan → montantAchat et ventePrevisible calculés depuis achats
2. ✅ Clôturer une journée → bénéfice brut correct
3. ✅ Vue consolidée → total des 3 boutiques correct
4. ❌ Clôturer une journée déjà clôturée → erreur
5. ✅ Dettes du jour visibles dans le bilan

```bash
git commit -m "feat: add BilanJournalier entity and repository"
git commit -m "feat: add BilanJournalierService with auto-calculation"
git commit -m "feat: add BilanController with daily close and monthly view"
git commit -m "feat: add consolidated view for all 3 poissonneries"
git commit -m "test: add BilanJournalierService unit tests"
git checkout develop
git merge feature/bilan-journalier
git push origin develop
```

---

## ═══════════════════════════════════════════════════════
## PHASE 4 : CLÔTURE MENSUELLE (1 semaine)
## Bouton fin de mois — 2 étapes séparées
## ═══════════════════════════════════════════════════════

**Branche** : `feature/cloture-mensuelle`

```bash
git checkout develop
git checkout -b feature/cloture-mensuelle
```

### 4.1 — Entité ClotureMensuelle

**PARTIE 1 — Calculé automatiquement par le système** :

| Champ | Source |
|-------|--------|
| totalVentesRealisees | Somme BilanJournalier.venteRealisee du mois |
| totalVentesPrevisibles | Somme BilanJournalier.ventePrevisible du mois |
| totalAchats | Somme BilanJournalier.montantAchat du mois |
| totalTransport | Somme DepenseQuotidienne type=TRANSPORT du mois |
| totalRation | Somme DepenseQuotidienne type=RATION du mois |
| totalGlace | Somme DepenseQuotidienne type=GLACE du mois |
| totalAutresDepensesQuotidiennes | Somme DepenseQuotidienne type=AUTRE du mois |
| beneficeAvantCharges | Calculé auto |

**PARTIE 2 — Saisi par le patron via le bouton fin de mois** :

| Champ | Exemple |
|-------|---------|
| electricite | 45 000 FCFA |
| loyer | 100 000 FCFA |
| salaires | 150 000 FCFA |
| autresChargesFixes | 25 000 FCFA |
| descriptionAutresCharges | "Réparation congélateur" |

**PARTIE 3 — Résultat final** :

| Champ | Formule |
|-------|---------|
| totalChargesFixes | electricite + loyer + salaires + autresChargesFixes |
| beneficeNet | Ventes - Achats - Dépenses quotidiennes - Charges fixes |
| cloturee | true (VERROUILLÉ après validation) |
| cloturePar | Le patron (User) |
| dateCloture | Quand il a validé |

### 4.2 — Flux en 2 étapes

```
ÉTAPE 1 : GET /api/v1/clotures/preparer?poissonnerieId=1&mois=3&annee=2026

Le patron clique "📊 Clôture du Mois"
→ Le système calcule TOUT automatiquement
→ Affiche les totaux + champs vides pour charges fixes

┌──────────────────────────────────────────────────┐
│  DÉJÀ CALCULÉ (ne touche à rien) :              │
│  Ventes Réalisées : 4 850 000 FCFA  ✅          │
│  Achats :          -4 200 000 FCFA  ✅          │
│  Transport :          -85 000 FCFA  ✅          │
│  Ration :             -62 000 FCFA  ✅          │
│  Glace :              -28 000 FCFA  ✅          │
│  Bénéfice AVANT charges : 475 000 FCFA         │
│                                                  │
│  À SAISIR :                                      │
│  ⚡ Électricité :  [________]                    │
│  🏠 Loyer :        [________]                    │
│  💰 Salaires :     [________]                    │
│  📦 Autres :       [________]                    │
└──────────────────────────────────────────────────┘

ÉTAPE 2 : POST /api/v1/clotures/valider

Le patron remplit et clique "🔒 Valider"
→ Le système calcule le bénéfice NET
→ VERROUILLE le mois (impossible de modifier)
→ Crée notification "Mars 2026 clôturé : +155 000 FCFA"
```

### 4.3 — Fichiers à créer

```
domain/cloture/ClotureMensuelle.java
domain/cloture/ClotureMensuelleRepository.java
application/cloture/ClotureMensuelleService.java
adapter/web/controller/ClotureMensuelleController.java
adapter/web/dto/request/ValiderClotureRequest.java
adapter/web/dto/response/PreparationClotureResponse.java
adapter/web/dto/response/ClotureMensuelleResponse.java
adapter/web/mapper/ClotureMapper.java
```

### 4.4 — Endpoints

| Méthode | URL | Rôle | Description |
|---------|-----|------|-------------|
| GET | `/api/v1/clotures/preparer?poissonnerieId=&mois=&annee=` | PATRON | Étape 1 : voir calculs auto |
| POST | `/api/v1/clotures/valider` | PATRON | Étape 2 : saisir charges + valider |
| GET | `/api/v1/clotures/poissonnerie/{id}?annee=` | PATRON | Historique des clôtures |
| GET | `/api/v1/clotures/consolide?mois=&annee=` | PATRON | Vue consolidée 3 boutiques |

### 4.5 — Sécurité

**SEUL le patron peut clôturer** : `@PreAuthorize("hasAnyRole('PATRON', 'SUPER_ADMIN')")`

### 4.6 — Tests

1. ✅ Préparer clôture → totaux auto corrects
2. ✅ Valider clôture → bénéfice NET correct
3. ❌ Mois déjà clôturé → erreur
4. ❌ Vendeuse essaye de clôturer → accès refusé
5. ✅ Vue consolidée 3 boutiques → total correct

```bash
git commit -m "feat: add ClotureMensuelle entity with auto and manual fields"
git commit -m "feat: add ClotureMensuelleService with 2-step process"
git commit -m "feat: add ClotureMensuelleController with prepare and validate"
git commit -m "test: add ClotureMensuelleService unit tests"
git checkout develop
git merge feature/cloture-mensuelle
git push origin develop
```

---

## ═══════════════════════════════════════════════════════
## PHASE 5 : FOURNISSEUR & LIVREURS (3-4 jours)
## Évaluer les livreurs qui changent chaque semaine
## ═══════════════════════════════════════════════════════

**Branche** : `feature/livreurs-evaluation`

```bash
git checkout develop
git checkout -b feature/livreurs-evaluation
```

### 5.1 — Entité Livreur

| Champ | Type | Exemple |
|-------|------|---------|
| id | Long (auto) | 1 |
| nom | String | "Jean" |
| prenom | String | "Mballa" |
| telephone | String | "677445566" |
| fournisseur | ManyToOne → Fournisseur | Fournisseur Principal |
| actif | Boolean | true |
| createdAt | LocalDateTime | auto |

### 5.2 — Entité EvaluationLivreur

| Champ | Type | Exemple |
|-------|------|---------|
| id | Long (auto) | 1 |
| livreur | ManyToOne → Livreur | Jean Mballa |
| achatJournalier | ManyToOne → AchatJournalier | Achat du 15/03 |
| dateEvaluation | LocalDate | 2026-03-15 |
| qualiteProduit | Integer (1-5) | 4 ⭐ |
| respectQuantite | Integer (1-5) | 3 ⭐ |
| ponctualite | Integer (1-5) | 5 ⭐ |
| respectPoids | Integer (1-5) | 4 ⭐ |
| commentaire | String (1000) | "Le JAX était pas frais" |
| problemeSignale | Boolean | false |
| evaluePar | ManyToOne → User | Patron |
| createdAt | LocalDateTime | auto |

### 5.3 — Verdict automatique

```
Note moyenne = (qualité + quantité + ponctualité + poids) / 4

>= 4.0  → 🟢 EXCELLENT — À garder
>= 3.0  → 🟡 CORRECT — À surveiller
>= 2.0  → 🟠 MÉDIOCRE — Avertissement
<  2.0  → 🔴 MAUVAIS — À remplacer
```

### 5.4 — Fichiers à créer

```
domain/livreur/Livreur.java
domain/livreur/LivreurRepository.java
domain/livreur/EvaluationLivreur.java
domain/livreur/EvaluationLivreurRepository.java
application/livreur/LivreurService.java
adapter/web/controller/LivreurController.java
adapter/web/dto/request/CreateLivreurRequest.java
adapter/web/dto/request/CreateEvaluationRequest.java
adapter/web/dto/response/LivreurResponse.java
adapter/web/dto/response/EvaluationResponse.java
adapter/web/dto/response/BilanLivreurResponse.java
adapter/web/mapper/LivreurMapper.java
```

### 5.5 — Endpoints

| Méthode | URL | Rôle | Description |
|---------|-----|------|-------------|
| POST | `/api/v1/livreurs` | PATRON | Créer un livreur |
| GET | `/api/v1/livreurs` | PATRON, ENREGISTREUR | Liste des livreurs |
| POST | `/api/v1/livreurs/{id}/evaluations` | PATRON, ENREGISTREUR | Évaluer après livraison |
| GET | `/api/v1/livreurs/{id}/evaluations` | PATRON | Historique évaluations |
| GET | `/api/v1/livreurs/{id}/bilan?mois=&annee=` | PATRON | Bilan mensuel + verdict |

### 5.6 — Tests

1. ✅ Créer livreur → OK
2. ✅ Évaluer livreur → note moyenne correcte
3. ✅ Bilan mensuel → verdict correct (EXCELLENT si >= 4.0)
4. ❌ Évaluer avec note > 5 → erreur

```bash
git commit -m "feat: add Livreur entity and repository"
git commit -m "feat: add EvaluationLivreur entity with rating criteria"
git commit -m "feat: add LivreurService with evaluation and monthly report"
git commit -m "feat: add LivreurController with CRUD and evaluation endpoints"
git commit -m "test: add LivreurService and evaluation unit tests"
git checkout develop
git merge feature/livreurs-evaluation
git push origin develop
```

---

## ═══════════════════════════════════════════════════════
## PHASE 6 : PRÊTS MULTI-BOUTIQUES (3-4 jours)
## Adapter les dettes pour les 3 boutiques
## ═══════════════════════════════════════════════════════

**Branche** : `feature/prets-multi-boutiques`

```bash
git checkout develop
git checkout -b feature/prets-multi-boutiques
```

### 6.1 — Ajouter le flag `pretActif` sur Poissonnerie

| Boutique | pretActif | Raison |
|----------|-----------|--------|
| Boutique Centrale | `true` | Le patron gère les prêts ici |
| Boutique 2 | `false` | Pas de PC, pas de prêts pour le moment |
| Boutique 3 | `false` | Pas de PC, pas de prêts pour le moment |

### 6.2 — Modifier CompteCourantService

Avant de créer un emprunt, vérifier :
```java
if (!client.getPoissonnerie().getPretActif()) {
    throw new BusinessException("Les prêts ne sont pas activés pour cette boutique");
}
```

### 6.3 — Matrice complète des permissions

| Action | PATRON | CAISSIERE (Vendeuse) | ENREGISTREUR (Secrétaire) |
|--------|--------|----------------------|---------------------------|
| Créer emprunt | ✅ | ✅ | ❌ |
| Remboursement | ✅ | ✅ | ❌ |
| Épargne (dépôt/retrait) | ✅ | ✅ | ❌ |
| Voir les comptes | ✅ | ✅ | ✅ (lecture seule) |
| Créer produit | ✅ | ❌ | ❌ |
| Créer achat journalier | ✅ | ❌ | ✅ |
| Ajouter dépense | ✅ | ✅ | ❌ |
| Supprimer dépense | ✅ | ❌ | ❌ |
| Clôturer journée | ✅ | ❌ | ❌ |
| Clôturer mois | ✅ | ❌ | ❌ |
| Évaluer livreur | ✅ | ❌ | ✅ |
| Voir rapports/stats | ✅ | ❌ | ❌ |
| Gérer utilisateurs | ✅ | ❌ | ❌ |

### 6.4 — Lier les dettes au bilan journalier

Quand le patron clôture la journée, le système ajoute automatiquement :
- Nombre de nouvelles dettes créées aujourd'hui
- Montant total des nouvelles dettes
- Montant total des remboursements du jour
- Ça explique l'écart entre vente prévisible et cash encaissé

### 6.5 — Tests

1. ✅ Emprunt sur boutique avec pretActif=true → OK
2. ❌ Emprunt sur boutique avec pretActif=false → BusinessException
3. ✅ Secrétaire ne peut pas créer de dettes → accès refusé
4. ✅ Résumé dettes dans le bilan journalier correct

```bash
git commit -m "feat: add pretActif flag to Poissonnerie entity"
git commit -m "feat: add loan verification in CompteCourantService"
git commit -m "feat: add @PreAuthorize role-based access for all operations"
git commit -m "feat: link daily debt summary to BilanJournalier"
git commit -m "test: add multi-boutique debt management tests"
git checkout develop
git merge feature/prets-multi-boutiques
git push origin develop
```

---

## ═══════════════════════════════════════════════════════
## PHASE 7 : RAPPORTS, STATISTIQUES & EXPORT PDF (1 semaine)
## ═══════════════════════════════════════════════════════

**Branche** : `feature/rapports-statistiques`

```bash
git checkout develop
git checkout -b feature/rapports-statistiques
```

### 7.1 — Statistiques Produits

| Endpoint | Description |
|----------|-------------|
| `GET /api/v1/stats/top-produits?poissonnerieId=&mois=&annee=` | Top 10 produits les plus achetés |
| `GET /api/v1/stats/produit/{id}/historique?periode=` | Évolution d'un produit sur le temps |
| `GET /api/v1/stats/produits/evolution-prix?mois=&annee=` | Produits dont le prix a changé |

### 7.2 — Statistiques Financières

| Endpoint | Description |
|----------|-------------|
| `GET /api/v1/stats/financier/semaine?poissonnerieId=&date=` | Bilan d'une semaine |
| `GET /api/v1/stats/financier/mois?poissonnerieId=&mois=&annee=` | Bilan du mois |
| `GET /api/v1/stats/financier/comparaison?mois1=&mois2=&annee=` | Comparer 2 mois |
| `GET /api/v1/stats/financier/consolide?annee=` | Vue annuelle 3 boutiques |

### 7.3 — Statistiques Livreurs

| Endpoint | Description |
|----------|-------------|
| `GET /api/v1/stats/livreurs/classement?mois=&annee=` | Classement des livreurs |
| `GET /api/v1/stats/livreurs/problemes?mois=&annee=` | Livreurs avec problèmes signalés |

### 7.4 — Export PDF

| Endpoint | Description |
|----------|-------------|
| `GET /api/v1/exports/inventaire-mensuel/{poissonnerieId}?mois=&annee=` | Générer Image 1 en PDF |
| `GET /api/v1/exports/bilan-mensuel/{poissonnerieId}?mois=&annee=` | Bilan complet du mois PDF |
| `GET /api/v1/exports/livreur/{id}/bilan?mois=&annee=` | Bilan d'un livreur PDF |
| `GET /api/v1/exports/consolide?mois=&annee=` | Bilan consolidé 3 boutiques PDF |

**Technologie** : iTextPDF ou Apache POI pour générer les PDF

### 7.5 — Fichiers à créer

```
application/stats/StatistiquesService.java
application/export/ExportPdfService.java
adapter/web/controller/StatistiquesController.java
adapter/web/controller/ExportController.java
adapter/web/dto/response/TopProduitsResponse.java
adapter/web/dto/response/BilanFinancierResponse.java
adapter/web/dto/response/ComparaisonMoisResponse.java
adapter/web/dto/response/ClassementLivreursResponse.java
```

### 7.6 — Tests

1. ✅ Top produits → classement correct
2. ✅ Bilan mensuel → chiffres corrects
3. ✅ Comparaison mois → écarts calculés
4. ✅ Export PDF → fichier généré sans erreur

```bash
git commit -m "feat: add top products statistics endpoint"
git commit -m "feat: add financial statistics with weekly and monthly views"
git commit -m "feat: add month comparison statistics"
git commit -m "feat: add delivery person ranking statistics"
git commit -m "feat: add PDF export for monthly inventory"
git commit -m "feat: add PDF export for monthly financial report"
git commit -m "test: add statistics and export unit tests"
git checkout develop
git merge feature/rapports-statistiques
git push origin develop
```

---

## ═══════════════════════════════════════════════════════
## PHASE 8 : TESTS + SWAGGER + RELEASE (3-4 jours)
## ═══════════════════════════════════════════════════════

**Branche** : `feature/tests-finaux`

```bash
git checkout develop
git checkout -b feature/tests-finaux
```

### 8.1 — Tests manquants pour le code existant (Modules 1-3)

| Fichier test | Ce qu'il teste |
|-------------|-------|
| `CompteCourantServiceTest.java` | Emprunts, remboursements, alertes seuil -5000, limite crédit |
| `EpargneServiceTest.java` | Dépôts, retraits, transfert épargne → compte courant |
| `NotificationServiceTest.java` | Rapport journalier, alertes auto |
| `AuthServiceTest.java` | Login OK, mauvais mot de passe, compte désactivé |
| `ClientServiceTest.java` | CRUD client, recherche |
| `UserServiceTest.java` | CRUD utilisateur, gestion rôles |

### 8.2 — Swagger/OpenAPI complet

Ajouter les annotations sur TOUS les controllers :
- `@Operation(summary = "...", description = "...")`
- `@ApiResponse(responseCode = "200", description = "...")`
- `@ApiResponse(responseCode = "400", description = "...")`
- `@ApiResponse(responseCode = "404", description = "...")`
- `@Parameter(description = "...")`

### 8.3 — Release finale

```bash
git commit -m "test: add unit tests for existing CompteCourant module"
git commit -m "test: add unit tests for existing Epargne module"
git commit -m "test: add unit tests for existing Auth module"
git commit -m "test: add unit tests for existing Notification module"
git commit -m "docs: add complete Swagger annotations on all endpoints"
git checkout develop
git merge feature/tests-finaux
git push origin develop

# Quand TOUT est stable et testé :
git checkout main
git merge develop
git tag -a v1.0.0 -m "Version 1.0.0 - Fish-Cam ERP complet"
git push origin main --tags
```

---

## 👥 LES 3 EMPLOYÉS ET LEURS RÔLES

| Rôle dans l'entreprise | Personne | Ce qu'elle fait | Rôle dans l'app |
|------------------------|----------|-----------------|-----------------|
| **Patron** | Propriétaire des 3 boutiques | Tout vérifier, clôturer jour/mois, gérer users | `PATRON` |
| **Vendeuse principale** | Proche du patron, de confiance | Ventes, dettes, épargnes, dépenses quotidiennes | `CAISSIERE` |
| **Secrétaire d'achat** | Employée qui vient à 15h | Saisir achats pour les 3 boutiques, évaluer livreurs | `ENREGISTREUR` |

---

## 🏗️ ARCHITECTURE FINALE DES MODULES

```
╔═══════════════════════════════════════════════╗
║           FISH-CAM ERP v1.0                   ║
║   Système de Gestion de Poissonnerie          ║
╠═══════════════════════════════════════════════╣
║                                               ║
║  MODULE 1  : Auth & Utilisateurs        ✅   ║
║  MODULE 2  : Clients & Comptes Courants ✅   ║
║  MODULE 3  : Épargnes                   ✅   ║
║  MODULE 4  : Achats, Produits & Prix    📦   ║
║  MODULE 5  : Dépenses Quotidiennes      💸   ║
║  MODULE 6  : Bilan Journalier           📊   ║
║  MODULE 7  : Clôture Mensuelle          📋   ║
║  MODULE 8  : Fournisseur & Livreurs     🚚   ║
║  MODULE 9  : Prêts Multi-Boutiques      🏪   ║
║  MODULE 10 : Rapports & Export PDF      📈   ║
║                                               ║
╚═══════════════════════════════════════════════╝
```

## ⚙️ TECHNOLOGIES

- Java 17 + Spring Boot 3.x
- Spring Security + JWT
- PostgreSQL
- Swagger/OpenAPI 3
- iTextPDF (exports PDF)
- JUnit 5 + Mockito (tests)
- Lombok
- Bean Validation (jakarta.validation)

---

## 📂 STRUCTURE DES PACKAGES (Clean Architecture)

```
com.fishcam/
├── domain/                          ← Entités métier pures
│   ├── user/
│   ├── client/
│   ├── poissonnerie/
│   ├── comptecourant/
│   ├── epargne/
│   ├── produit/                     ← NOUVEAU (Phase 1)
│   │   ├── Produit.java
│   │   ├── ProduitRepository.java
│   │   ├── HistoriquePrix.java
│   │   └── HistoriquePrixRepository.java
│   ├── achat/                       ← NOUVEAU (Phase 1)
│   │   ├── AchatJournalier.java
│   │   ├── AchatJournalierRepository.java
│   │   ├── LigneAchat.java
│   │   └── LigneAchatRepository.java
│   ├── depense/                     ← NOUVEAU (Phase 2)
│   │   ├── DepenseQuotidienne.java
│   │   ├── TypeDepenseQuotidienne.java
│   │   └── DepenseQuotidienneRepository.java
│   ├── bilan/                       ← NOUVEAU (Phase 3)
│   │   ├── BilanJournalier.java
│   │   └── BilanJournalierRepository.java
│   ├── cloture/                     ← NOUVEAU (Phase 4)
│   │   ├── ClotureMensuelle.java
│   │   └── ClotureMensuelleRepository.java
│   ├── fournisseur/                 ← RÉORGANISÉ (Phase 0.6)
│   │   ├── Fournisseur.java
│   │   └── FournisseurRepository.java
│   └── livreur/                     ← NOUVEAU (Phase 5)
│       ├── Livreur.java
│       ├── LivreurRepository.java
│       ├── EvaluationLivreur.java
│       └── EvaluationLivreurRepository.java
│
├── application/                     ← Services métier
│   ├── auth/
│   ├── user/
│   ├── client/
│   ├── comptecourant/
│   ├── epargne/
│   ├── notification/
│   ├── produit/                     ← NOUVEAU
│   │   └── ProduitService.java
│   ├── achat/                       ← NOUVEAU
│   │   └── AchatJournalierService.java
│   ├── depense/                     ← NOUVEAU
│   │   └── DepenseQuotidienneService.java
│   ├── bilan/                       ← NOUVEAU
│   │   └── BilanJournalierService.java
│   ├── cloture/                     ← NOUVEAU
│   │   └── ClotureMensuelleService.java
│   ├── livreur/                     ← NOUVEAU
│   │   └── LivreurService.java
│   ├── stats/                       ← NOUVEAU
│   │   └── StatistiquesService.java
│   └── export/                      ← NOUVEAU
│       └── ExportPdfService.java
│
├── adapter/web/                     ← Controllers REST + DTOs
│   ├── controller/
│   │   ├── AuthController.java
│   │   ├── ClientController.java
│   │   ├── CompteCourantController.java
│   │   ├── EpargneController.java
│   │   ├── NotificationController.java
│   │   ├── UserController.java
│   │   ├── ProduitController.java          ← NOUVEAU
│   │   ├── AchatJournalierController.java  ← NOUVEAU
│   │   ├── DepenseQuotidienneController.java ← NOUVEAU
│   │   ├── BilanJournalierController.java  ← NOUVEAU
│   │   ├── ClotureMensuelleController.java ← NOUVEAU
│   │   ├── LivreurController.java          ← NOUVEAU
│   │   ├── StatistiquesController.java     ← NOUVEAU
│   │   └── ExportController.java           ← NOUVEAU
│   ├── dto/
│   │   ├── request/
│   │   └── response/
│   └── mapper/
│
└── infrastructure/                  ← Config, Sécurité, Exceptions
    ├── config/
    ├── security/
    └── exception/
```