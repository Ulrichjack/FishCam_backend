# 🚀 ROADMAP V2 — FISHCAM ERP (Améliorations Futures)

> Ce document liste les fonctionnalités, optimisations techniques et améliorations d'expérience utilisateur (UX) qui ont été identifiées pendant le développement de la V1, mais volontairement reportées pour garantir une livraison rapide et stable du MVP (Minimum Viable Product).

---

## 📊 1. Fonctionnalités Métier & UX

### 🐟 Grille des Prix (Catalogue dynamique)
- **Constat V1 :** Pour connaître le prix de vente actuel d'un poisson, la caissière doit simuler une facture ou chercher dans l'historique.
- **Action V2 :** Créer une page "Grille des Prix" (lecture seule) qui affiche le dernier prix d'achat et de vente de chaque produit pour la poissonnerie active.

### 🗑️ Gestion des Avaries (Pertes)
- **Constat V1 :** Le système gère les achats et les ventes, mais pas les pertes physiques (poisson gâté, jeté).
- **Action V2 :** Ajouter un module "Avaries" pour déduire ces pertes du stock théorique et les imputer dans le bilan financier mensuel.

### 💸 Catégorisation avancée des Dépenses
- **Constat V1 :** Les dépenses journalières sont limitées à "Transport", "Ration" et "Autres".
- **Action V2 :** Permettre au Patron de créer des catégories de dépenses personnalisées (Électricité, Salaire journalier, Entretien, etc.) pour des statistiques plus fines.

### ✏️ Édition des lignes de facture
- **Constat V1 :** Si l'Enregistreur se trompe sur une ligne de facture, il doit la supprimer et la recréer.
- **Action V2 :** Ajouter un bouton "Modifier" (Crayon) directement dans le tableau des lignes de facture pour éditer la quantité ou le prix sans tout retaper.

### 📈 Export Excel des Transactions
- **Constat V1 :** Les transactions (Emprunts/Remboursements) sont visibles dans l'application, mais seul le récapitulatif global est exportable en PDF.
- **Action V2 :** Ajouter un bouton "Exporter en Excel (CSV)" sur la page des transactions pour faciliter le travail du comptable externe.

### ⭐ Filtrage des Évaluations Livreurs
- **Constat V1 :** Le Slide-Over affiche l'historique complet des évaluations d'un livreur.
- **Action V2 :** Ajouter une pagination ou un filtre "Mois en cours" pour éviter un affichage trop long après 1 an d'utilisation.

---

## 🛠️ 2. Architecture & DevOps (Tech Debt)

### 🗄️ Migrations de Base de Données (Flyway / Liquibase)
- **Constat V1 :** Le projet utilise `spring.jpa.hibernate.ddl-auto=update` en production.
- **Action V2 :** Désactiver la génération automatique d'Hibernate et intégrer **Flyway**. Créer des scripts SQL versionnés (`V1__init.sql`, `V2__add_column.sql`) pour sécuriser et tracer les modifications de la base de données en production.

### 📦 Vraie Archive Mensuelle (Cloud)
- **Constat V1 :** Le bouton "Archive Mensuelle" déclenche la même action que la sauvegarde hebdomadaire (Dump SQL + CSV).
- **Action V2 :** Créer un endpoint dédié `/api/v1/admin/backup/monthly` qui génère un fichier `.zip` contenant le SQL, le CSV, **et tous les PDF des bilans du mois**, puis l'envoie sur Cloudflare R2.

### 🔄 Refactoring `ResponseEntity`
- **Constat V1 :** Les contrôleurs Spring Boot renvoient directement l'objet `ApiResponse` en s'appuyant sur le `GlobalExceptionHandler` et `@ResponseStatus`.
- **Action V2 :** Envelopper tous les retours des contrôleurs dans des `ResponseEntity.ok()` ou `ResponseEntity.status(HttpStatus.CREATED)` pour respecter à 100% les standards d'entreprise RESTful.

---

## 🖨️ 3. Intégrations Matérielles

### 🧾 Impression de Tickets de Caisse
- **Constat V1 :** L'application est 100% digitale (PDF).
- **Action V2 :** Intégrer l'API Web Bluetooth ou une bibliothèque d'impression pour permettre à la caissière d'imprimer un reçu physique (ticket thermique 80mm) lors d'un remboursement ou d'un dépôt d'épargne.