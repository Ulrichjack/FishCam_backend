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

## 🖨️ 3. Intégrations Matérielles

### 🧾 Impression de Tickets de Caisse
- **Constat V1 :** L'application est 100% digitale (PDF).
- **Action V2 :** Intégrer l'API Web Bluetooth ou une bibliothèque d'impression pour permettre à la caissière d'imprimer un reçu physique (ticket thermique 80mm) lors d'un remboursement ou d'un dépôt d'épargne.
---

## ✅ Fait (sorti de la roadmap)

- **Bénéfice sur le récapitulatif PDF** — encadré en bas du récapitulatif affichant
  `TOTAL DEPENSES` et `BENEFICE DE LA PERIODE` (implémenté dans `PdfExportService.exportRecapitulatifToPdf`).
- **Vraie archive mensuelle (Cloud)** — `POST /api/v1/admin/backup/monthly` + cron le 1er du mois à 2h :
  zip (SQL + CSV + un récapitulatif PDF par poissonnerie) envoyé sur R2, type `CLOUD_MONTHLY`
  (`MonthlyArchiveService`). La sauvegarde quotidienne de 19h reste inchangée.
- **Migrations Flyway** — baseline `V1` vérifiée contre la structure de production du
  18/08/2026, Hibernate en `validate` et baselining sécurisé par variable temporaire. Voir
  `docs/FLYWAY_DEPLOYMENT.md`.

## ❌ Abandonné

- **Refactoring `ResponseEntity`** — le `GlobalExceptionHandler` gère déjà les codes HTTP ;
  envelopper les ~20 contrôleurs serait un diff massif pour zéro gain fonctionnel.


#IMPORTANCE
sur le frontend sur les facture dacte mette un selecteur de date un truc pour aller a  date suivate et autre precedent et sur la nav bar facture/achat en bas on met cloture journaliere en bas cote frontend et aussi avoir la possibilite de supprimer une facture temps quelle nest pas cloturer ou archiver elle saffiche pas ou suppression qui fait elle saffiche pas et le probleme sa met pas a jour les prix des produit rapidement a pres change je sais pas pouruqoiet aussi sur create facture les ronf lors de ajout produit tourne hord de leur derive genre au alentour et on peut pas modifier la date dune facture qui nest pas encore cree et aussi lors de insersion on voie pas directement le prix total de la vente previsible avant de clique sur ajout ligne et pourvoir ou si on fiat plein update sa cause des probleme? ou la update des produit se fait que sur la poissonerie principal
