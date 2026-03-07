# 🗺️ ROADMAP COMPLÈTE — FISH-CAM ERP BACKEND (V2)

> **Application Spring Boot de gestion de poissonnerie**
> Repo : `Ulrichjack/FishCam_backend`
> Dernière mise à jour : 2026-03-05
> Basé sur le cahier réel du patron + optimisations système

---

## 📌 PRINCIPES FONDAMENTAUX

```
1. ON NUMÉRISE LE CAHIER DU PATRON — On ne change pas sa méthode, on l'améliore
2. MINIMUM DE SAISIE — Le système calcule et préremplie au maximum
3. PAS DE TABLE INUTILE — Si on peut calculer, on ne stocke pas
4. 1 FACTURE = 1 BOUTIQUE = 1 JOUR — La secrétaire choisit la boutique UNE FOIS
5. LigneAchat EST l'historique des prix — Pas besoin de table séparée
```

---

## 📌 RÉSUMÉ GLOBAL

### ✅ DÉJÀ FAIT (Phase 0 terminée)

| # | Module | Détails |
|---|--------|---------|
| 1 | **Auth & Sécurité** | JWT (login par téléphone), Spring Security, sessions stateless |
| 2 | **Gestion Clients** | CRUD complet, lien Client → Poissonnerie |
| 3 | **Comptes Courants (Dettes)** | Emprunts, remboursements, alerte seuil -5000 FCFA, limite crédit |
| 4 | **Épargnes** | Dépôts, retraits, transfert épargne → compte courant |
| 5 | **Notifications** | Alertes auto (seuil dette, compte soldé), rapport journalier, rattrapage au démarrage |
| 6 | **Multi-poissonneries** | Entité Poissonnerie, UserScope (SINGLE/MULTI) |
| 7 | **Gestion Utilisateurs** | Rôles (SUPER_ADMIN, PATRON, CAISSIERE, ENREGISTREUR), avatar photo |
| 8 | **Nettoyage repo** | Fichiers perso supprimés, bugs corrigés, .gitignore à jour |
| 9 | **Profils dev/prod** | application-dev.properties, application-prod.properties séparés |

### ❌ CE QUI RESTE À FAIRE

| Phase | Module | Priorité | Durée |
|-------|--------|----------|-------|
| 1 | **Produits, Employés & Factures d'achat** | 🔴 HAUTE | 1-2 semaines |
| 2 | **Clôture Journalière** | 🔴 HAUTE | 3-4 jours |
| 3 | **Récapitulatif & Bilan** | 🔴 HAUTE | 1 semaine |
| 4 | **Fournisseur & Livreurs** | 🟡 MOYENNE | 3-4 jours |
| 5 | **Prêts Multi-Boutiques** | 🟡 MOYENNE | 3-4 jours |
| 6 | **Rapports, Export PDF & Admin** | 🟢 BASSE | 1 semaine |
| 7 | **Tests Finaux + Swagger + Release** | 🟢 BASSE | 3-4 jours |

**Durée totale estimée : 5-7 semaines**

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
        ├── feature/produits-employes
        ├── feature/factures-achats
        ├── feature/cloture-journaliere
        ├── feature/recapitulatif-bilan
        ├── feature/livreurs-evaluation
        ├── feature/prets-multi-boutiques
        ├── feature/rapports-export
        └── feature/tests-finaux
```

### Convention des commits

| Préfixe | Usage | Exemple |
|---------|-------|---------|
| `feat:` | Nouvelle fonctionnalité | `feat: add Produit entity` |
| `fix:` | Correction de bug | `fix: correct price calculation` |
| `refactor:` | Restructuration | `refactor: move Fournisseur to own package` |
| `chore:` | Maintenance | `chore: remove temp files` |
| `test:` | Tests | `test: add LigneAchat tests` |
| `docs:` | Documentation | `docs: update README` |

---

## ═══════════════════════════════════════════════════════
## PHASE 1 : PRODUITS, EMPLOYÉS & FACTURES D'ACHAT (1-2 semaines)
## Remplacer le carnet des achats journaliers
## ═══════════════════════════════════════════════════════

### 1.1 — Enum Unite

```java
package com.fishcam.domain.produit;

public enum Unite {
    KG("Kilogramme"),
    CARTON("Carton"),
    PIECE("Pièce");
}
```

### 1.2 — Entité Produit (Catalogue des poissons)

**GLOBAL** : pas lié à une boutique. Les 3 boutiques vendent les mêmes produits.

| Champ | Type | Exemple | Notes |
|-------|------|---------|-------|
| id | Long (auto) | 1 | |
| nom | String (100, unique) | "JAX 23+" | |
| unite | Enum Unite | KG | Par défaut KG |
| poidsParCarton | BigDecimal | 21.0 | Poids moyen d'1 carton |
| actif | Boolean | true | Par défaut true |
| createdAt | LocalDateTime | auto | updatable = false |
| updatedAt | LocalDateTime | auto | |

**Ce qui N'EST PAS sur Produit :**
- ❌ Pas de prix (le dernier prix vient de LigneAchat)
- ❌ Pas de poissonnerie (produit global)
- ❌ Pas de catégorie (tout est poisson congelé)

### 1.3 — Entité Employe (Employés avec ou sans accès système)

**Pourquoi ?** Certains employés (vendeurs, gardiens) travaillent en boutique
mais n'ont PAS accès au système. Il faut quand même compter leur salaire.

| Champ | Type | Exemple | Notes |
|-------|------|---------|-------|
| id | Long (auto) | 1 | |
| prenom | String (50) | "Marie" | |
| nom | String (50) | "Ngono" | |
| poste | String (50) | "Caissière" | |
| salaire | BigDecimal | 40 000 | Fixe, modifiable par patron |
| telephone | String (20) | "677445566" | Optionnel |
| poissonnerie | ManyToOne → Poissonnerie | Boutique LELE | Obligatoire |
| user | ManyToOne → User (nullable) | null | Lié SI accès système |
| actif | Boolean | true | |
| createdAt | LocalDateTime | auto | |
| updatedAt | LocalDateTime | auto | |

**Exemples :**

| Employé | Boutique | Poste | Salaire | Accès système ? |
|---------|----------|-------|---------|-----------------|
| Marie | Centrale | Caissière | 40 000 | ✅ Oui (User lié) |
| Paul | Centrale | Vendeur | 35 000 | ✅ Oui (User lié) |
| Jean | LELE | Vendeur | 30 000 | ❌ Non |
| Pierre | BARE | Vendeur | 30 000 | ❌ Non |
| Amadou | BARE | Gardien | 20 000 | ❌ Non |

### 1.4 — Modifier Poissonnerie (ajouter champs)

| Champ ajouté | Type | Exemple | Notes |
|-------------|------|---------|-------|
| loyer | BigDecimal | 50 000 | Prérempli dans bilan mensuel |
| fondDeCaisseDefaut | BigDecimal | 10 000 | Prérempli chaque soir |

### 1.5 — Modifier Fournisseur

- Retirer le lien ManyToOne → Poissonnerie (fournisseur est GLOBAL)
- Ajouter champ `actif` (Boolean, défaut true)
- Ajouter `updatedAt`

### 1.6 — Entité AchatJournalier (= Facture du jour pour 1 boutique)

**1 facture = 1 boutique = 1 jour**

La secrétaire choisit la boutique UNE SEULE FOIS au début.
Tout ce qu'elle ajoute ensuite est pour cette boutique.

| Champ | Type | Source | Notes |
|-------|------|--------|-------|
| id | Long (auto) | Auto | |
| dateAchat | LocalDate | Saisi | Date du jour |
| poissonnerie | ManyToOne → Poissonnerie | Saisi | Choisie AU DÉBUT |
| fournisseur | ManyToOne → Fournisseur | Saisi | Ex: CONGELCAM |
| enregistrePar | ManyToOne → User | Auto (JWT) | La secrétaire |
| cloture | Boolean | false | true = plus modifiable |
| createdAt | LocalDateTime | auto | |
| updatedAt | LocalDateTime | auto | |

**Ce qui N'EST PAS sur AchatJournalier :**
- ❌ Pas de totalAchat (calculé dans le service/DTO)
- ❌ Pas de totalVentePrevisible (calculé dans le service/DTO)

### 1.7 — Entité LigneAchat (Chaque carton dans la facture)

**C'est LE cœur du système. LigneAchat EST aussi l'historique des prix.**

| Champ | Type | Source | Exemple |
|-------|------|--------|---------|
| id | Long (auto) | Auto | 1 |
| achatJournalier | ManyToOne → AchatJournalier | Auto | La facture |
| produit | ManyToOne → Produit | Saisi | JAX 23+ |
| quantiteCartons | Integer | Saisi | 3 |
| poidsKg | BigDecimal | Saisi (prérempli) | 63.0 |
| montantCarton | BigDecimal | Saisi (prérempli) | 100 500 |
| prixVenteKilo | BigDecimal | Saisi (prérempli) | 1 706 |
| createdAt | LocalDateTime | auto | |
| updatedAt | LocalDateTime | auto | |

**Ce qui N'EST PAS sur LigneAchat (calculé dans DTO Response) :**
- ❌ prixAchatKilo → `montantCarton / poidsKg`
- ❌ prixVenteTotal → `prixVenteKilo × poidsKg`
- ❌ margeKilo → `prixVenteKilo - prixAchatKilo`
- ❌ margeTotal → `prixVenteTotal - montantCarton`
- ❌ poissonnerie → c'est sur la facture, pas sur chaque ligne

**Ce qui N'EST PAS en base (table supprimée) :**
- ❌ Table HistoriquePrix → LigneAchat EST l'historique
- ❌ Table PrixBoutique → Le dernier prix vient de la dernière LigneAchat

### 1.8 — Préremplissage des prix (logique clé)

Quand la secrétaire choisit un produit dans le formulaire :

```
REQUÊTE :
  GET /api/v1/lignes/dernier-prix?produitId=1&poissonnerieId=1

SQL DU BACKEND :
  SELECT la.quantite_cartons, la.poids_kg, la.montant_carton, la.prix_vente_kilo
  FROM ligne_achat la
  JOIN achat_journalier aj ON la.achat_journalier_id = aj.id
  WHERE la.produit_id = 1
    AND aj.poissonnerie_id = 1
  ORDER BY la.id DESC
  LIMIT 1

RÉPONSE :
  {
    "quantiteCartons": 3,
    "poidsKg": 63.0,
    "montantCarton": 100500,
    "prixVenteKilo": 1706
  }

→ Le frontend préremplie TOUT
→ La secrétaire modifie seulement ce qui a changé
→ Elle valide → POST → nouvelle LigneAchat
→ Cette ligne DEVIENT le nouveau "dernier prix"
→ Demain elle sera préremplie
```

**CAS SPÉCIAUX :**

```
Nouveau produit, jamais acheté dans cette boutique :
  → Pas de données → champs vides → la secrétaire tape tout
  → La prochaine fois ce sera prérempli

Prix d'achat a changé chez CONGELCAM :
  → La secrétaire modifie le montant → valide
  → Nouvelle ligne avec nouveau prix → historique conservé

Patron change le prix de vente :
  → La secrétaire modifie prixVenteKilo → valide
  → Demain le nouveau prix sera prérempli pour cette boutique
  
   Le poids PAR CARTON vient du Produit.poidsParCarton
  Le poids TOTAL est calculé : quantiteCartons × poidsParCarton
  Le prix d'achat est prérempli depuis la dernière LigneAchat de CETTE boutique
  Le prix de vente/kg est prérempli depuis la dernière LigneAchat de CETTE boutique
```

### 1.9 — L'écran de la secrétaire

```
ÉTAPE 1 — Elle choisit la boutique :

┌─────────────────────────────────────────────┐
│  📋 FACTURES DU JOUR — 15/03/2026          │
│                                              │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐
│  │ 🏪 Boutique  │  │ 🏪 Boutique  │  │ 🏪 Boutique  │
│  │  Centrale    │  │   LELE       │  │   BARE       │
│  │  [Ouvrir]    │  │  [Ouvrir]    │  │  [Ouvrir]    │
│  └──────────────┘  └──────────────┘  └──────────────┘
└─────────────────────────────────────────────┘

ÉTAPE 2 — Formulaire + preview :

┌──────────────────────────────────────────────────────────────┐
│  📋 FACTURE — Boutique Centrale — 15/03/2026                │
│  Fournisseur : [CONGELCAM ▼]                                 │
│                                                               │
│  ┌─── FORMULAIRE ──────────────┐  ┌─── FACTURE (preview) ───┐│
│  │                              │  │                          ││
│  │ Produit: [JAX 23+  ▼]       │  │ Cartons│Produit │Poids  ││
│  │                              │  │ │Achat  │Vente/kg│Vente ││
│  │ → Prérempli auto :   │  │ ───────────────────────│││
│  │ Poids/carton : 21 kg (du Produit)     │
│  │ Cartons:    [3]              │  │ 3│JAX23+│63kg│100500   ││
│  │ Poids:      [63] kg         │  │  │      │    │107478   ││
│  │ Prix achat: [100 500]       │  │ 2│MAC315│40kg│51000    ││
│  │ Vente/kg:   [1 706]         │  │  │      │    │68000    ││
│  │        
                      │  │ ────────────────────── ││
│  │ Vente total: 107 478 (auto) │  │ Total achat: 151 500   ││
│  │                              │  │ Total vente: 175 478   ││
│  │ [+ Ajouter à la facture]    │  │                          ││
│  └──────────────────────────────┘  └──────────────────────────┘│
│                                                                │
│  [ 💾 Clôturer la facture ]                                   │
└────────────────────────────────────────────────────────────────┘
```

### 1.10 — La facture vue par le patron (fidèle au cahier)

```
📋 FACTURE — Boutique Centrale — 15/03/2026
Fournisseur : CONGELCAM Nkongsamba
Enregistré par : Marie (Secrétaire)
Statut : ✅ Clôturée

┌────────┬──────────────┬────────┬──────────┬──────────┬──────────┬─────────┐
│Cartons │ Produit      │ Poids  │ Achat    │ Vente/kg │ Vente    │ Marge   │
├────────┼──────────────┼────────┼──────────┼──────────┼──────────┼─────────┤
│   03   │ JAX 23+      │ 63 kg  │ 100 500  │  1 706   │ 107 478  │  6 978  │
│   02   │ MAC 315      │ 40 kg  │  51 000  │  1 700   │  68 000  │ 17 000  │
│   01   │ SYLUGA 1/1   │ 10 kg  │  13 000  │  1 500   │  15 000  │  2 000  │
│   01   │ SYLUR SMO    │ 10 kg  │  11 000  │  1 300   │  13 000  │  2 000  │
│   01   │ BW           │ 20 kg  │  17 000  │    950   │  19 000  │  2 000  │
│   01   │ MAC 16+      │ 21 kg  │  28 500  │  1 500   │  31 500  │  3 000  │
├────────┼──────────────┼────────┼──────────┼──────────┼──────────┼─────────┤
│   09   │ TOTAL        │164 kg  │ 221 000  │          │ 253 978  │ 32 978  │
└────────┴──────────────┴────────┴──────────┴──────────┴──────────┴─────────┘

Marge prévisible : 253 978 - 221 000 = 32 978 FCFA
```

### 1.11 — Fichiers à créer

```
domain/produit/Produit.java
domain/produit/ProduitRepository.java
domain/produit/Unite.java
domain/employe/Employe.java
domain/employe/EmployeRepository.java
domain/achat/AchatJournalier.java
domain/achat/AchatJournalierRepository.java
domain/achat/LigneAchat.java
domain/achat/LigneAchatRepository.java
application/produit/ProduitService.java
application/employe/EmployeService.java
application/achat/AchatJournalierService.java
adapter/web/controller/ProduitController.java
adapter/web/controller/EmployeController.java
adapter/web/controller/AchatJournalierController.java
adapter/web/dto/request/CreateProduitRequest.java
adapter/web/dto/request/UpdateProduitRequest.java
adapter/web/dto/request/CreateEmployeRequest.java
adapter/web/dto/request/UpdateEmployeRequest.java
adapter/web/dto/request/CreateFactureRequest.java
adapter/web/dto/request/CreateLigneRequest.java
adapter/web/dto/request/UpdateLigneRequest.java
adapter/web/dto/response/ProduitResponse.java
adapter/web/dto/response/EmployeResponse.java
adapter/web/dto/response/FactureResponse.java
adapter/web/dto/response/FactureDetailResponse.java
adapter/web/dto/response/LigneAchatResponse.java
adapter/web/dto/response/DernierPrixResponse.java
adapter/web/mapper/ProduitMapper.java
adapter/web/mapper/EmployeMapper.java
adapter/web/mapper/AchatMapper.java
```

### 1.12 — Endpoints

**PRODUITS :**

| Méthode | URL | Rôle | Description |
|---------|-----|------|-------------|
| POST | /api/v1/produits | PATRON, ENREGISTREUR, CAISSIERE | Créer |
| PUT  | /api/v1/produits/{id} | PATRON, ENREGISTREUR, CAISSIERE | Modifier |
| DELETE | /api/v1/produits/{id} | PATRON | Désactiver || GET | `/api/v1/produits` | Tous | Lister tous les produits |
| GET | `/api/v1/produits/search?q=JA` | Tous | Recherche rapide (autocomplétion) |
| GET | `/api/v1/produits/{id}` | Tous | Détail d'un produit |
| PUT | `/api/v1/produits/{id}` | PATRON | Modifier un produit |
| DELETE | `/api/v1/produits/{id}` | PATRON | Désactiver (soft delete) |

**EMPLOYÉS :**

| Méthode | URL | Rôle | Description |
|---------|-----|------|-------------|
| POST | `/api/v1/employes` | PATRON | Ajouter un employé |
| GET | `/api/v1/employes?poissonnerieId=1` | PATRON | Lister par boutique |
| GET | `/api/v1/employes/{id}` | PATRON | Détail employé |
| PUT | `/api/v1/employes/{id}` | PATRON | Modifier salaire/poste |
| DELETE | `/api/v1/employes/{id}` | PATRON | Désactiver |

**FACTURES (AchatJournalier) :**

| Méthode | URL | Rôle | Description |
|---------|-----|------|-------------|
| POST | `/api/v1/factures` | PATRON, ENREGISTREUR | Créer facture du jour |
| GET | `/api/v1/factures?poissonnerieId=1&date=2026-03-15` | Tous | Factures du jour |
| GET | `/api/v1/factures/{id}` | Tous | Détail facture avec toutes les lignes |
| PUT | `/api/v1/factures/{id}/cloturer` | PATRON, ENREGISTREUR | Clôturer la facture |

**LIGNES DE FACTURE :**

| Méthode | URL | Rôle | Description |
|---------|-----|------|-------------|
| POST | `/api/v1/factures/{id}/lignes` | PATRON, ENREGISTREUR | Ajouter une ligne |
| PUT | `/api/v1/factures/{id}/lignes/{lid}` | PATRON, ENREGISTREUR | Modifier (prix, poids...) |
| DELETE | `/api/v1/factures/{id}/lignes/{lid}` | PATRON, ENREGISTREUR | Supprimer une ligne |
| GET | `/api/v1/lignes/dernier-prix?produitId=1&poissonnerieId=1` | Tous | Dernier prix pour préremplir |
| GET | /api/v1/lignes/historique-prix?produitId=1 | PATRON | Évolution des prix |

### 1.13 — Tests

1. ✅ Créer un produit → OK
2. ✅ Recherche "JA" → trouve "JAX 23+"
3. ✅ Créer un employé sans accès système → OK
4. ✅ Créer facture + ajouter 5 lignes → totaux calculés correctement dans DTO
5. ✅ Modifier une ligne (changer prix) → OK tant que facture pas clôturée
6. ✅ Supprimer une ligne → OK tant que facture pas clôturée
7. ❌ Modifier une ligne sur facture clôturée → BusinessException
8. ✅ Dernier prix par produit et boutique → correct
9. ✅ Nouveau produit sans historique → réponse vide
10. ❌ Créer achat avec montant négatif → BusinessException

```bash
git checkout develop
git checkout -b feature/produits-employes
# Créer Produit, Unite, Employe, modifier Poissonnerie
git commit -m "feat: add Produit entity with Unite enum"
git commit -m "feat: add Employe entity for employees with/without system access"
git commit -m "feat: add loyer and fondDeCaisseDefaut to Poissonnerie"
git commit -m "feat: add ProduitService with search"
git commit -m "feat: add EmployeService with CRUD"
git commit -m "feat: add ProduitController and EmployeController"
git checkout develop
git merge feature/produits-employes

git checkout -b feature/factures-achats
# Créer AchatJournalier, LigneAchat
git commit -m "feat: add AchatJournalier entity (daily invoice per shop)"
git commit -m "feat: add LigneAchat entity with price prefill logic"
git commit -m "feat: add AchatJournalierService with line management"
git commit -m "feat: add dernier-prix endpoint for price prefill"
git commit -m "feat: add facture cloturer logic"
git commit -m "test: add Produit, Employe and Facture unit tests"
git checkout develop
git merge feature/factures-achats
git push origin develop
```

---

## ═══════════════════════════════════════════════════════
## PHASE 2 : CLÔTURE JOURNALIÈRE (3-4 jours)
## Le patron ferme la journée — Remplace le cahier du soir
## ═══════════════════════════════════════════════════════

### 2.1 — Entité ClotureJournaliere

Le patron remplit ça CHAQUE SOIR pour chaque boutique.

| Champ | Type | Source | Notes |
|-------|------|--------|-------|
| id | Long (auto) | Auto | |
| dateCloture | LocalDate | Auto | Date du jour |
| poissonnerie | ManyToOne → Poissonnerie | Auto | La boutique |
| **CALCULÉS AUTO (depuis les factures du jour)** | | | |
| totalAchat | BigDecimal | Calculé | Somme montantCarton des lignes |
| totalVentePrevisible | BigDecimal | Calculé | Somme prixVenteKilo × poidsKg |
| **SAISIS PAR LE PATRON** | | | |
| argentCaisse | BigDecimal | Saisi | Tout l'argent dans la caisse |
| fondDeCaisse | BigDecimal | Prérempli | Monnaie laissée (défaut depuis Poissonnerie) |
| transport | BigDecimal | Saisi | 0 si pas de transport |
| ration | BigDecimal | Saisi | Repas des employés |
| autresFrais | BigDecimal | Saisi | 0 si rien |
| descriptionAutres | String (500) | Saisi | Optionnel |
| **CALCULÉS AUTO** | | | |
| venteRealisee | BigDecimal | Calculé | argentCaisse - fondDeCaisse |
| totalDepenses | BigDecimal | Calculé | transport + ration + autresFrais |
| beneficeNet | BigDecimal | Calculé | venteRealisee - totalAchat - totalDepenses |
| **MÉTADONNÉES** | | | |
| cloturePar | ManyToOne → User | Auto (JWT) | Le patron |
| createdAt | LocalDateTime | Auto | |
dette :
ClotureJournaliere.java :
... (tout ce qu'on a déjà prévu)
+ montantDettesJour           → BigDecimal (calculé auto)
+ montantRemboursementsJour   → BigDecimal (calculé auto)
+ nombreDettesJour            → Integer (calculé auto)
**Les dépenses sont DIRECTEMENT ici, pas dans une table séparée.**

### 2.2 — L'écran du patron le soir

```
🌙 CLÔTURE — Boutique Centrale — 15/03/2026

── CALCULÉ AUTO (depuis les factures) ─────────────
Total achat du jour      :  221 000 FCFA
Vente prévisible         :  253 978 FCFA

── LE PATRON REMPLIT ──────────────────────────────
Argent total caisse      : [ 260 000 ]
Fond de caisse (monnaie) : [ 10 000 ]  ← prérempli
= Vente réalisée         :  250 000 FCFA (calculé auto)

── DÉPENSES DU JOUR (optionnel) ────────────────��──
Transport                : [ 5 000 ]
Ration                   : [ 3 000 ]
Autres frais             : [ 0 ]
Description              : [ _________ ]
= Total dépenses         :  8 000 FCFA (calculé auto)

── RÉSULTAT ───────────────────────────────────────
Vente réalisée           :  250 000
- Achat du jour          : -221 000
- Dépenses               :   -8 000
= BÉNÉFICE NET DU JOUR   :  21 000 FCFA ✅

[ 💾 Clôturer la journée ]
```

### 2.3 — Fichiers à créer

```
domain/cloture/ClotureJournaliere.java
domain/cloture/ClotureJournaliereRepository.java
application/cloture/ClotureJournaliereService.java
adapter/web/controller/ClotureJournaliereController.java
adapter/web/dto/request/CloturerJourneeRequest.java
adapter/web/dto/response/PreparationClotureResponse.java
adapter/web/dto/response/ClotureJournaliereResponse.java
adapter/web/mapper/ClotureMapper.java
```

### 2.4 — Endpoints

| Méthode | URL | Rôle | Description |
|---------|-----|------|-------------|
| GET | `/api/v1/clotures/preparer?poissonnerieId=1&date=2026-03-15` | PATRON | Voir calculs auto avant clôture |
| POST | `/api/v1/clotures` | PATRON | Saisir vente réalisée + dépenses + valider |
| GET | `/api/v1/clotures?poissonnerieId=1&date=2026-03-15` | PATRON | Voir la clôture du jour |

### 2.5 — Tests

1. ✅ Préparer clôture → totalAchat et ventePrevisible calculés depuis factures
2. ✅ Clôturer → venteRealisee = argentCaisse - fondDeCaisse
3. ✅ Bénéfice net correct
4. ❌ Clôturer une journée déjà clôturée → BusinessException
5. ❌ Clôturer sans factures → avertissement

```bash
git checkout develop
git checkout -b feature/cloture-journaliere
git commit -m "feat: add ClotureJournaliere entity with daily expenses"
git commit -m "feat: add ClotureJournaliereService with auto-calculation"
git commit -m "feat: add ClotureController with prepare and close endpoints"
git commit -m "test: add ClotureJournaliere unit tests"
git checkout develop
git merge feature/cloture-journaliere
git push origin develop
```

---

## ═══════════════════════════════════════════════════════
## PHASE 3 : RÉCAPITULATIF & BILAN (1 semaine)
## Reproduire le document FISH-CAM + bilan amélioré
## ═══════════════════════════════════════════════════════

### 3.1 — Récapitulatif journalier (= Document FISH-CAM)

**PAS une entité en base.** C'est un CALCUL dans le service à partir
des ClotureJournaliere.

Le patron choisit un intervalle libre :

```
📊 RÉCAPITULATIF — Boutique LELE
Du [ 01/12/2025 ] au [ 31/12/2025 ]    [🔍 Afficher]

┌──────┬───────────┬───────────┬───────────┬──────────┬──────────┐
│ Jour │ Achat     │ Prévu     │ Réalisé   │ Dépenses │ Bénéfice │
├──────┼───────────┼───────────┼───────────┼──────────┼──────────┤
│  1   │ 178 000   │ 193 900   │ 193 900   │  6 000   │  9 900   │
│  2   │ 232 500   │ 256 300   │ 183 500   │  8 000   │ -57 000  │
│ ...  │           │           │           │          │          │
├──────┼───────────┼───────────┼───────────┼──────────┼──────────┤
│TOTAL │4 500 000  │5 100 000  │4 800 000  │ 150 000  │ 150 000  │
└──────┴───────────┴───────────┴───────────┴──────────┴──────────┘
```
Quand tu coderas BilanService, dans le DTO response tu ajoutes :

BilanMensuelResponse.java :
... (tout ce qu'on a déjà prévu)
+ dettesDebutMois             → BigDecimal
+ nouvellesDettes             → BigDecimal
+ remboursementsMois          → BigDecimal
+ dettesFinMois               → BigDecimal
+ topDebiteurs                → List<DebiteurResponse>
### 3.2 — Bilan mensuel (avec charges fixes)

**PAS une entité en base non plus.** Le patron tape seulement
l'électricité. Le reste est prérempli ou calculé.

```
📊 BILAN — Boutique LELE — DÉCEMBRE 2025

═══ PARTIE 1 — Détail jour par jour ═══
(le récapitulatif ci-dessus)

═══ PARTIE 2 — Résultat du mois ═══

REVENUS
  Total ventes réalisées        :  4 800 000 FCFA   ← calculé auto

COÛTS MARCHANDISE
  Total achats                  : -4 500 000 FCFA   ← calculé auto

DÉPENSES QUOTIDIENNES
  Transport                     :    -45 000 FCFA   ← calculé auto
  Ration                        :    -90 000 FCFA   ← calculé auto
  Autres frais                  :    -15 000 FCFA   ← calculé auto
  Sous-total                    :   -150 000 FCFA

= MARGE OPÉRATIONNELLE          :    150 000 FCFA

CHARGES FIXES
  Loyer                         :    -50 000 FCFA   ← prérempli depuis Poissonnerie
  Électricité                   : [  -25 000 ] FCFA ← SAISI par patron (variable)
  Salaires (3 employés)         :   -120 000 FCFA   ← calculé auto depuis Employe
    Marie (Caissière)    40 000
    Paul (Vendeur)       35 000
    Jean (Vendeur)       45 000
  Sous-total                    :   -195 000 FCFA

══════════════════════════════════════════
RÉSULTAT NET DU MOIS             :    -45 000 🔴
══════════════════════════════════════════
```

### 3.3 — Comparaison des 3 boutiques

```
📊 COMPARAISON — DÉCEMBRE 2025

┌───────────────┬────────────┬────────────┬────────────┐
│               │ Centrale   │ LELE       │ BARE       │
├───────────────┼────────────┼────────────┼────────────┤
│ Ventes        │ 5 200 000  │ 4 800 000  │ 3 100 000  │
│ Achats        │-4 800 000  │-4 500 000  │-2 900 000  │
│ Dépenses      │  -180 000  │  -150 000  │  -120 000  │
│ Charges fixes │  -195 000  │  -195 000  │  -160 000  │
├───────────────┼────────────┼────────────┼────────────┤
│ RÉSULTAT NET  │   25 000 🟢│  -45 000 🔴│  -80 000 🔴│
└───────────────┴────────────┴────────────┴────────────┘
```

### 3.4 — Fichiers à créer

```
application/rapport/RecapitulatifService.java
application/rapport/BilanService.java
adapter/web/controller/RapportController.java
adapter/web/dto/request/BilanMensuelRequest.java
adapter/web/dto/response/RecapitulatifResponse.java
adapter/web/dto/response/RecapitulatifLigneResponse.java
adapter/web/dto/response/BilanMensuelResponse.java
adapter/web/dto/response/ComparaisonBoutiquesResponse.java
adapter/web/mapper/RapportMapper.java
```

### 3.5 — Endpoints

| Méthode | URL | Rôle | Description |
|---------|-----|------|-------------|
| GET | `/api/v1/rapports/recapitulatif?poissonnerieId=1&du=2025-12-01&au=2025-12-31` | PATRON | Tableau jour par jour |
| POST | `/api/v1/rapports/bilan-mensuel` | PATRON | Bilan avec électricité saisie |
| GET | `/api/v1/rapports/comparaison?mois=12&annee=2025` | PATRON | Vue 3 boutiques |

**Body du POST bilan-mensuel :**
```json
{
  "poissonnerieId": 1,
  "mois": 12,
  "annee": 2025,
  "electricite": 25000,
  "loyerOverride": null,
  "autresCharges": 0,
  "descriptionAutresCharges": null
}
```

Le loyer est prérempli mais `loyerOverride` permet de changer si besoin ce mois-là.

### 3.6 — Tests

1. ✅ Récapitulatif → sommes correctes
2. ✅ Bilan → bénéfice net correct
3. ✅ Salaires calculés auto depuis Employe
4. ✅ Loyer prérempli depuis Poissonnerie
5. ✅ Comparaison 3 boutiques → totaux corrects

```bash
git checkout develop
git checkout -b feature/recapitulatif-bilan
git commit -m "feat: add RecapitulatifService with date range filter"
git commit -m "feat: add BilanService with auto salary and rent calculation"
git commit -m "feat: add shop comparison endpoint"
git commit -m "feat: add RapportController with all report endpoints"
git commit -m "test: add rapport and bilan unit tests"
git checkout develop
git merge feature/recapitulatif-bilan
git push origin develop
```

---

## ═══════════════════════════════════════════════════════
## PHASE 4 : FOURNISSEUR & LIVREURS (3-4 jours)
## Évaluer les livreurs CONGELCAM
## ═══════════════════════════════════════════════════════

### 4.1 — Entité Livreur

| Champ | Type | Exemple |
|-------|------|---------|
| id | Long (auto) | 1 |
| nom | String | "Jean" |
| prenom | String | "Mballa" |
| telephone | String | "677445566" |
| fournisseur | ManyToOne → Fournisseur | CONGELCAM |
| actif | Boolean | true |
| createdAt | LocalDateTime | auto |

### 4.2 — Entité EvaluationLivreur

| Champ | Type | Exemple |
|-------|------|---------|
| id | Long (auto) | 1 |
| livreur | ManyToOne → Livreur | Jean Mballa |
| achatJournalier | ManyToOne → AchatJournalier | Facture du 15/03 |
| dateEvaluation | LocalDate | 2026-03-15 |
| qualiteProduit | Integer (1-5) | 4 ⭐ |
| respectQuantite | Integer (1-5) | 3 ⭐ |
| ponctualite | Integer (1-5) | 5 ⭐ |
| respectPoids | Integer (1-5) | 4 ⭐ |
| commentaire | String (1000) | "Le JAX était pas frais" |
| problemeSignale | Boolean | false |
| evaluePar | ManyToOne → User | Patron |
| createdAt | LocalDateTime | auto |

### 4.3 — Verdict automatique

```
Note moyenne = (qualité + quantité + ponctualité + poids) / 4

>= 4.0  → 🟢 EXCELLENT — À garder
>= 3.0  → 🟡 CORRECT — À surveiller
>= 2.0  → 🟠 MÉDIOCRE — Avertissement
<  2.0  → 🔴 MAUVAIS — À remplacer
```

### 4.4 — Endpoints

| Méthode | URL | Rôle | Description |
|---------|-----|------|-------------|
| POST | `/api/v1/livreurs` | PATRON | Créer un livreur |
| GET | `/api/v1/livreurs` | PATRON, ENREGISTREUR | Liste des livreurs |
| POST | `/api/v1/livreurs/{id}/evaluations` | PATRON, ENREGISTREUR | Évaluer après livraison |
| GET | `/api/v1/livreurs/{id}/evaluations` | PATRON | Historique évaluations |
| GET | `/api/v1/livreurs/{id}/bilan?mois=&annee=` | PATRON | Bilan mensuel + verdict |

```bash
git checkout develop
git checkout -b feature/livreurs-evaluation
git commit -m "feat: add Livreur entity and repository"
git commit -m "feat: add EvaluationLivreur with rating criteria"
git commit -m "feat: add LivreurService with evaluation and verdict"
git commit -m "feat: add LivreurController with CRUD and evaluation"
git commit -m "test: add Livreur and evaluation unit tests"
git checkout develop
git merge feature/livreurs-evaluation
git push origin develop
```

---

## ═══════════════════════════════════════════════════════
## PHASE 5 : PRÊTS MULTI-BOUTIQUES (3-4 jours)
## Adapter les dettes pour les 3 boutiques
## ═══════════════════════════════════════════════════════

### 5.1 — Ajouter `pretActif` sur Poissonnerie

| Boutique | pretActif | Raison |
|----------|-----------|--------|
| Boutique Centrale | `true` | Le patron gère les prêts ici |
| Boutique LELE | `false` | Pas de PC |
| Boutique BARE | `false` | Pas de PC |

### 5.2 — Matrice complète des permissions

| Action | PATRON | CAISSIERE | ENREGISTREUR |
|--------|--------|-----------|--------------|
| Créer emprunt | ✅ | ✅ | ❌ |
| Remboursement | ✅ | ✅ | ❌ |
| Épargne (dépôt/retrait) | ✅ | ✅ | ❌ |
| Voir les comptes | ✅ | ✅ | ✅ (lecture seule) |
| Créer produit | ✅ | ❌ | ❌ |
| Créer/modifier facture | ✅ | ❌ | ✅ |
| Ajouter dépense | ✅ | ✅ (via clôture) | ❌ |
| Clôturer journée | ✅ | ❌ | ❌ |
| Voir rapports/bilans | ✅ | ❌ | ❌ |
| Gérer employés | ✅ | ❌ | ❌ |
| Gérer utilisateurs | ✅ | ❌ | ❌ |
| Évaluer livreur | ✅ | ❌ | ✅ |

```bash
git checkout develop
git checkout -b feature/prets-multi-boutiques
git commit -m "feat: add pretActif flag to Poissonnerie"
git commit -m "feat: add loan verification in CompteCourantService"
git commit -m "feat: add @PreAuthorize role-based access for all operations"
git commit -m "test: add multi-boutique permission tests"
git checkout develop
git merge feature/prets-multi-boutiques
git push origin develop
```

---

## ═══════════════════════════════════════════════════════
## PHASE 6 : RAPPORTS, EXPORT PDF & ADMIN (1 semaine)
## ═══════════════════════════════════════════════════════

### 6.1 — Export PDF

| Endpoint | Description |
|----------|-------------|
| `GET /api/v1/exports/facture/{id}/pdf` | Facture du jour en PDF |
| `GET /api/v1/exports/recapitulatif/pdf?...` | Récapitulatif (= document FISH-CAM) en PDF |
| `GET /api/v1/exports/bilan/pdf?...` | Bilan mensuel en PDF |

**Technologie** : iTextPDF ou JasperReports

### 6.2 — Export base de données (SUPER_ADMIN uniquement)

```
GET /api/v1/admin/export-database

→ Exporte toute la base en JSON
→ Seul SUPER_ADMIN peut faire ça
→ Pour récupérer les données à distance
→ Pour alimenter le futur modèle Python
```

### 6.3 — Statistiques (si temps)

| Endpoint | Description |
|----------|-------------|
| `GET /api/v1/stats/top-produits?...` | Top 10 produits les plus achetés |
| `GET /api/v1/stats/evolution-prix?produitId=1&...` | Évolution prix d'un produit |

### 6.4 — Impression depuis navigateur (Phase 1 simple)

Avant le PDF, le frontend peut utiliser `window.print()` pour imprimer
directement depuis l'écran. Gratuit, pas de librairie.

```bash
git checkout develop
git checkout -b feature/rapports-export
git commit -m "feat: add PDF export for invoices and reports"
git commit -m "feat: add database export endpoint for SUPER_ADMIN"
git commit -m "feat: add basic product statistics"
git commit -m "test: add export and statistics tests"
git checkout develop
git merge feature/rapports-export
git push origin develop
```

---

## ═══════════════════════════════════════════════════════
## PHASE 7 : TESTS FINAUX + SWAGGER + RELEASE (3-4 jours)
## ═══════════════════════════════════════════════════════

### 7.1 — Tests manquants

| Fichier test | Ce qu'il teste |
|-------------|----------------|
| ProduitServiceTest | CRUD produit, recherche |
| EmployeServiceTest | CRUD employé, salaires |
| AchatJournalierServiceTest | Facture, lignes, préremplissage |
| ClotureJournaliereServiceTest | Clôture, calculs auto |
| BilanServiceTest | Bilan mensuel, comparaison |
| LivreurServiceTest | Évaluations, verdict |

### 7.2 — Swagger complet

Ajouter sur TOUS les controllers :
- `@Operation(summary = "...", description = "...")`
- `@ApiResponse(responseCode = "200/400/404")`
- `@Parameter(description = "...")`

### 7.3 — Release

```bash
git checkout main
git merge develop
git tag -a v1.0.0 -m "Version 1.0.0 - Fish-Cam ERP complet"
git push origin main --tags
```

---

## 👥 LES EMPLOYÉS ET LEURS RÔLES

| Rôle entreprise | Ce qu'elle fait | Rôle app | Accès |
|----------------|-----------------|----------|-------|
| Patron | Vérifie tout, clôture jour/mois, gère users | PATRON | Total |
| Vendeuse principale | Ventes, dettes, épargnes | CAISSIERE | Dettes + épargnes |
| Secrétaire d'achat | Saisit les factures à 15h, évalue livreurs | ENREGISTREUR | Factures + livreurs |
| Vendeurs boutique | Vendent au comptoir | (pas d'accès) | Via entité Employe |
| Super Admin (toi) | Maintenance, export données | SUPER_ADMIN | Tout + export DB |

---

## 🏗️ ARCHITECTURE FINALE

```
╔═══════════════════════════════════════════════╗
║           FISH-CAM ERP v1.0                   ║
╠═══════════════════════════════════════════════╣
║                                               ║
║  ✅ Auth & Utilisateurs                       ║
║  ✅ Clients & Comptes Courants (Dettes)       ║
║  ✅ Épargnes                                  ║
║  ✅ Notifications & Rapports auto             ║
║  ✅ Multi-poissonneries & Avatars             ║
║                                               ║
║  📦 Phase 1 : Produits, Employés, Factures   ║
║  📦 Phase 2 : Clôture Journalière            ║
║  📦 Phase 3 : Récapitulatif & Bilan          ║
║  📦 Phase 4 : Fournisseur & Livreurs         ║
║  📦 Phase 5 : Prêts Multi-Boutiques          ║
║  📦 Phase 6 : Export PDF & Admin             ║
║  📦 Phase 7 : Tests + Swagger + Release      ║
║                                               ║
╚═══════════════════════════════════════════════╝
```

## ⚙️ TECHNOLOGIES

- Java 17 + Spring Boot 3.x
- Spring Security + JWT
- PostgreSQL
- MapStruct (mapping DTO ↔ entité)
- Swagger/OpenAPI 3
- iTextPDF (exports PDF — Phase 6)
- JUnit 5 + Mockito (tests)
- Lombok

## 📂 STRUCTURE DES PACKAGES

```
com.fishcam/
├── domain/
│   ├── user/
│   ├── client/
│   ├── poissonnerie/
│   ├── comptecourant/
│   ├── epargne/
│   ├── notification/
│   ├── produit/           ← Phase 1
│   ├── employe/           ← Phase 1
│   ├── achat/             ← Phase 1
│   ├── cloture/           ← Phase 2
│   ├── fournisseur/       ← Phase 0 (réorganisé)
│   └── livreur/           ← Phase 4
│
├── application/
│   ├── auth/
│   ├── user/
│   ├── client/
│   ├── comptecourant/
│   ├── epargne/
│   ├── notification/
│   ├── produit/           ← Phase 1
│   ├── employe/           ← Phase 1
│   ├── achat/             ← Phase 1
│   ├── cloture/           ← Phase 2
│   ├── rapport/           ← Phase 3
│   ├── livreur/           ← Phase 4
│   └── export/            ← Phase 6
│
├── adapter/web/
│   ├── controller/
│   ├── dto/request/
│   ├── dto/response/
│   └── mapper/
│
└── infrastructure/
    ├── config/
    ├── security/
    └── exception/
```

---

## 📋 TABLES SUPPRIMÉES (par rapport à la ROADMAP V1)

| Table | Raison de la suppression |
|-------|------------------------|
| HistoriquePrix | LigneAchat EST l'historique des prix |
| PrixBoutique | Le dernier prix vient de la dernière LigneAchat |
| BilanMensuel (entité) | C'est un calcul dans le service, pas une entité |
| DepenseQuotidienne | Intégrée directement dans ClotureJournaliere |
| ClotureMensuelle (entité) | Remplacée par BilanService (calcul à la volée) |

## 📋 TABLES AJOUTÉES (par rapport à la ROADMAP V1)

| Table | Raison |
|-------|--------|
| Employe | Employés sans accès système + calcul auto des salaires |

## 📋 CHAMPS AJOUTÉS SUR TABLES EXISTANTES

| Table | Champ ajouté | Raison |
|-------|-------------|--------|
| Poissonnerie | loyer | Prérempli dans bilan mensuel |
| Poissonnerie | fondDeCaisseDefaut | Prérempli chaque soir |