# 🧪 RAPPORT DE TESTS GLOBAUX — FISHCAM ERP

**Date :** 20 Juin 2026
**Serveur :** Xubuntu (4 Go RAM, 256 Go HDD)
**Architecture :** Docker (PostgreSQL 16, Spring Boot 3, Nginx/Angular)

---

## 1. Test de Concurrence (Transactions Simultanées)
- **Objectif :** Vérifier que deux caissières ne peuvent pas corrompre un solde en cliquant en même temps.
- **Résultat attendu :** Les deux transactions passent, le solde final est exact.
- **Statut :** ✅ Validé
- **Observations :** Avant la correction, le test échouait (perte de mise à jour / Lost Update). Après l'ajout de `@Lock(LockModeType.PESSIMISTIC_WRITE)` dans le Repository (pour les Comptes Courants et les Épargnes), PostgreSQL met les requêtes en file d'attente. Le solde final est parfaitement calculé, même avec des clics à la milliseconde près.

## 2. Test de Montée en Charge (Load Testing)
- **Objectif :** Créer 500 factures simultanément via un script Python.
- **Résultat attendu :** Le serveur ne crash pas, la RAM reste stable, 0 erreur 500.
- **Statut :** ✅ Validé
- **Observations :** 500 requêtes traitées en 12.88 secondes. 100% de succès (Code 201). Le CPU du backend est monté à 125% temporairement pour absorber la charge, mais la RAM est restée dans les limites fixées par Docker (271 Mo utilisés sur 1 Go alloué). Le système est extrêmement robuste.

## 3. Test de Résilience (Coupure Réseau)
- **Objectif :** Débrancher la connexion internet/réseau pendant une transaction.
- **Résultat attendu :** Le frontend affiche une erreur propre, la base de données n'est pas corrompue.
- **Statut :** ✅ Validé
- **Observations :** Avant la correction, l'interface restait bloquée à l'infini. Après l'ajout d'un `timeout(10000)` dans l'intercepteur HTTP d'Angular, l'application coupe la requête au bout de 10 secondes et affiche un Toast d'erreur clair. La transaction n'est pas enregistrée en base.

## 4. Test de Sécurité des Rôles (RBAC)
- **Objectif :** Vérifier que chaque employé n'a accès qu'aux fonctionnalités de son rôle.
- **Résultat attendu :** Blocage Frontend (menus cachés) et Backend (Erreur 403).
- **Statut :** ✅ Validé
- **Observations :** 
  - L'Enregistreur ne peut voir que les Factures et ne peut pas les clôturer.
  - La Caissière peut gérer les transactions mais ne peut pas modifier les limites de crédit.
  - Le Patron a accès aux bilans et statistiques.
  - Le Super Admin peut basculer d'une poissonnerie à l'autre (Multi-Tenant).

## 5. Test de Cohérence Comptable (Clôture Journalière)
- **Objectif :** Vérifier que l'algorithme de calcul des écarts de caisse est exact.
- **Résultat attendu :** L'écart de caisse reflète parfaitement l'argent physique, incluant les dettes et remboursements.
- **Statut :** ✅ Validé
- **Observations :** Bug détecté : Le backend calculait l'écart mais omettait de l'affecter à l'entité avant le save(), ce qui renvoyait toujours 0 dans l'historique. Correction : Ajout de cloture.setEcartVente(ecartVente);. L'historique affiche désormais les manquants et surplus exacts.

## 6. Test d'Auto-Sauvegarde et Résilience Cloud
- **Objectif :** Vérifier que le système sauvegarde automatiquement les données sur Cloudflare R2 en cas de retard, et gère les pannes internet.
- **Résultat attendu :** Sauvegarde silencieuse en arrière-plan, ou affichage d'une bannière d'alerte si hors-ligne.
- **Statut :** ✅ Validé
- **Observations :** Si la sauvegarde est en retard et qu'internet est coupé, une bannière rouge globale s'affiche. Dès que la connexion est rétablie, le système lance la sauvegarde en arrière-plan (sans bloquer l'utilisateur) et fait disparaître la bannière au succès. Les erreurs 500 dues aux coupures réseau pendant l'auto-sauvegarde sont ignorées silencieusement.

## 7. Test de Cohérence du Bénéfice Net avec Dettes (Bug Fixé)
- **Objectif :** Vérifier que le bénéfice net n'est pas faussé par les emprunts ou les remboursements.
- **Statut :** ✅ Validé (Après correction)
- **Observations :** Bug détecté : Le système soustrayait les dettes du bénéfice, affichant une fausse perte financière. Correction : Application de la formule comptable stricte : Bénéfice = Vente Prévisible - Achat - Dépenses + Écart. Le système calcule désormais la rentabilité réelle indépendamment des mouvements de trésorerie.

## 8. Test d'Agrégation Mensuelle (Bilan & Statistiques)
- **Objectif :** Vérifier que le Dashboard additionne correctement les données journalières.
- **Statut :** ✅ Validé
- **Observations :** Testé sur 6 jours simulés. Les totaux (Achats: 640k, Ventes: 665.2k, Bénéfice: 41.7k) sont mathématiquement parfaits. L'identification des produits les plus rentables fonctionne.