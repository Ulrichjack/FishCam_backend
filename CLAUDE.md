# CLAUDE.md — Instructions pour Claude Code

> Référence principale, volontairement concise. Le détail vit dans `docs/` (guides existants),
> `Amelioration_futur.md` (roadmap/dette technique), `Test_Report.md` (résultats de tests) et
> les skills `.claude/skills/` — les charger uniquement quand la tâche le requiert.
>
> **GOUVERNANCE** : budget 120 lignes max (hook automatique). Avant TOUT ajout ici, suivre
> `.claude/skills/context-governance/SKILL.md` : skill d'abord, docs/ ensuite, lien enfin —
> CLAUDE.md en dernier recours.
>
> Codex et autres agents tiers : voir `AGENTS.md` (pointe ici, ne pas dupliquer).

## Projet

**FishCam ERP** — gestion des dettes, épargnes et opérations quotidiennes des poissonneries
au Cameroun. Multi-poissonneries (un `SUPER_ADMIN`/`PATRON` gère plusieurs établissements).

| Clé | Valeur |
|-----|--------|
| Repo | `github.com/Ulrichjack/FishCam_backend` |
| Branche de travail actuelle | `develop` |
| Dev local | `mvn spring-boot:run -Dspring-boot.run.profiles=dev` → `http://localhost:8080/api/v1` |
| Cible prod | **Render** (Docker) + **Supabase** (PostgreSQL managé) |
| Swagger | `/swagger-ui.html` |

**Stack** : Java 17, Spring Boot 3.3.5, Spring Security 6 + JWT (login par téléphone),
PostgreSQL + JPA/Hibernate, MapStruct, iTextPDF/OpenPDF, AWS SDK S3 (Cloudflare R2 pour
les sauvegardes), Springdoc OpenAPI.

## Architecture (Hexagonale / Ports & Adapters)

```
src/main/java/com/fishcam/
├── adapter/web/          # Controllers REST, DTOs, Mappers (MapStruct)
├── application/          # Services métiers (Auth, Factures, Clients, Cloture, etc.)
├── domain/               # Entités JPA et interfaces Repositories
└── infrastructure/       # Config Spring, Sécurité JWT, Scheduler, AOP (audit)
```

Rôles : `SUPER_ADMIN` (multi-tenant), `PATRON`, `CAISSIERE`, `ENREGISTREUR`.

## Règles CRITIQUES

**Secrets — AUCUNE valeur sensible en dur dans un fichier versionné.** `.env` est gitignoré
mais a déjà été commité par le passé (secrets exposés côté historique git, rotation faite le
2026-08-08 : voir mémoire projet). `application-prod.properties` ne contient **aucun défaut**
pour `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`, `CORS_ALLOWED_ORIGINS` — ces
variables sont injectées par Render à l'exécution, jamais codées en dur.

**Base de données prod = Supabase** (pooler "Session", port 6543, pas la connexion directe
5432) pour éviter l'épuisement de connexions Hibernate sur un petit plan Render.

**Migrations** : `spring.jpa.hibernate.ddl-auto=update` en prod (Flyway prévu mais **reporté
volontairement** — ne pas l'introduire sans demande explicite). Toute modification de schéma
en prod doit rester rétro-compatible avec `update` (pas de suppression de colonne/table sans
migration manuelle coordonnée).

**CORS** : origines lues depuis `app.cors.allowed-origins` (`CORS_ALLOWED_ORIGINS` en prod) —
ne jamais remettre d'origine en dur dans `SecurityConfig.java`.

**Build** : `mvn compile` (ou `./mvnw -o compile`) doit passer avant tout commit.

**Hook pre-push** : `.githooks/pre-push` bloque `git push` si la compilation échoue. Sur une
nouvelle machine/clone : `git config core.hooksPath .githooks` (une fois).

## Déploiement (résumé)

Render déploie l'image Docker (`Dockerfile` à la racine, `eclipse-temurin:17-jre-alpine`).
Variables d'environnement à définir sur Render : `PORT` (auto), `DB_URL`, `DB_USERNAME`,
`DB_PASSWORD` (Supabase, pooler session 6543), `JWT_SECRET`, `CORS_ALLOWED_ORIGINS`,
`CF_ENDPOINT`, `CF_ACCESS_KEY`, `CF_SECRET_KEY`, `CF_BUCKET` (sauvegardes R2).

## Commandes

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev   # Dev local
./mvnw -o compile                                     # Vérifier la compilation avant commit
mvn clean package -DskipTests                          # Build du jar pour Docker
docker build -t fishcam-backend .                       # Image locale
```

## Workflows → Skills (charger selon la tâche)

| Tâche | Ressource |
|-------|-----------|
| Modifier CLAUDE.md / AGENTS.md / skills / docs | `.claude/skills/context-governance/SKILL.md` |
| Roadmap V2, dette technique connue | `Amelioration_futur.md` |
| Résultats de tests déjà validés | `Test_Report.md` |
| Guides d'implémentation existants | `docs/GUIDE_IMPLEMENTATION_PRATIQUE.md`, `docs/EVALUATION_ET_SUGGESTIONS.md`, `docs/ROADMAP_FISHCAM.md` |
