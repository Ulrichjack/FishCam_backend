# AGENTS.md — Point d'entrée agents IA (Codex, etc.)

> Ce fichier est le point d'entrée générique lu par les outils IA non-Claude (Codex CLI, etc.).
> La référence complète et à jour vit dans **`CLAUDE.md`** — ce fichier ne fait que pointer dessus
> pour éviter toute divergence entre deux copies des mêmes règles.

## Lire en premier

1. `CLAUDE.md` — stack, architecture, règles critiques (secrets, config, déploiement), commandes.
2. `.claude/skills/context-governance/SKILL.md` — avant de modifier `CLAUDE.md`, `AGENTS.md` ou tout fichier de doc/config.

## Règles valables quel que soit l'agent

- **Ne jamais committer** `.env`, ni de secret en dur dans `application-*.properties` — toute valeur sensible passe par une variable d'environnement (`${VAR_NAME}`).
- Le profil `prod` (`application-prod.properties`) n'a **aucune valeur par défaut** pour les secrets : `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`, `CORS_ALLOWED_ORIGINS`. Elles sont injectées par Render.
- `mvn compile` (ou `./mvnw -o compile`) doit passer avant tout commit.
- Si deux agents (Claude Code et Codex) travaillent sur ce repo à des moments différents, cette procédure garantit qu'ils lisent la même source de vérité : ne dupliquez jamais une règle entre `AGENTS.md` et `CLAUDE.md` — `CLAUDE.md` est la seule source, `AGENTS.md` ne fait que renvoyer vers lui.
