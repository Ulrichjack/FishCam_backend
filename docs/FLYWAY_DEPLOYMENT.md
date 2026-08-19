# Premier déploiement Flyway

Cette procédure concerne une base FishCam existante sans table
`flyway_schema_history`. Elle ne doit être exécutée qu'après validation de la sauvegarde.

## Garanties préparées

- `V1__baseline.sql` contient uniquement les 20 tables métier du schéma `public`, leurs
  identités, contraintes et index.
- La migration ne contient aucune donnée, aucun propriétaire, aucun rôle Supabase et aucun
  secret.
- `spring.jpa.hibernate.ddl-auto=validate` interdit désormais à Hibernate de modifier le
  schéma.
- `FLYWAY_BASELINE_ON_MIGRATE` vaut `false` par défaut : une base existante non baselinée
  provoque un échec sûr.

## Avant le déploiement

1. Déclencher une sauvegarde complète et vérifier sa présence dans Cloudflare R2.
2. Télécharger une copie du dump et vérifier qu'il se termine par
   `PostgreSQL database dump complete`.
3. Ne jamais committer le dump : il contient les données et mots de passe hachés des comptes.
4. Compiler en Java 17 avec `./mvnw -o compile`.

## Premier déploiement sur Render

1. Ajouter temporairement la variable Render `FLYWAY_BASELINE_ON_MIGRATE=true`.
2. Déployer l'image contenant Flyway et `V1__baseline.sql`.
3. Vérifier dans les logs :
   - `Successfully baselined schema with version: 1` ;
   - `Schema public is up to date. No migration necessary` ;
   - `Initialized JPA EntityManagerFactory` ;
   - `Started FishcamApplication`.
4. Tester la connexion, une liste de factures, les produits et une clôture existante.
5. Retirer `FLYWAY_BASELINE_ON_MIGRATE` de Render, ou la mettre à `false`.
6. Redéployer et vérifier que Flyway annonce que le schéma est à jour.

Sur la base existante, Flyway crée seulement `flyway_schema_history` et inscrit une entrée
`BASELINE` en version 1. Il n'exécute pas le contenu de `V1__baseline.sql` et ne modifie pas
les lignes métier.

## Migrations suivantes

- Chaque changement de schéma reçoit un nouveau fichier immuable :
  `V2__description.sql`, puis `V3__description.sql`, etc.
- Ne jamais modifier une migration déjà appliquée.
- Tester chaque migration sur une base vide et sur une copie de la structure de production.
- Les suppressions de table ou colonne exigent une sauvegarde et une coordination explicites.

## Retour arrière

Si le premier déploiement échoue, revenir à l'image précédente. La présence de
`flyway_schema_history` est sans effet pour l'ancienne version utilisant Hibernate. Ne
restaurer le dump complet qu'en cas de perte de données constatée ; le baselining seul ne
supprime aucune donnée.
