---
name: context-governance
description: "OBLIGATOIRE AVANT de modifier: CLAUDE.md, AGENTS.md, AGENT.md, .claude/skills/*, .claude/hooks/*, docs/, README.md racine, ou créer un nouveau skill projet. Déclencheurs: « ajouter une règle », « documenter », « où mettre », « nouveau skill », modification de CLAUDE.md ou de la doc racine. NE PAS utiliser pour: écrire du code métier Java ou des migrations SQL."
---

# Gouvernance du contexte — FishCam ERP

## PRIMARY RESPONSIBILITY

Protéger l'architecture de contexte optimisée : décider OÙ va chaque information
documentaire, faire respecter le budget de CLAUDE.md et le format des skills projet.

## USE THIS SKILL WHEN

- On s'apprête à modifier CLAUDE.md, AGENTS.md, un skill projet, un hook ou docs/.
- On modifie la doc racine : README.MD, Amelioration_futur.md.
- On hésite sur l'emplacement d'une nouvelle règle, doc ou information.
- On crée un nouveau skill projet (template obligatoire ci-dessous).

## DO NOT USE THIS SKILL WHEN

- On écrit du code Java, des entités JPA, des migrations SQL, ou du contenu de test
  (→ CLAUDE.md suffit).

## TRIGGERS

`CLAUDE.md`, `AGENTS.md`, `skill`, `documentation projet`, `règle`, `convention`,
`où mettre`, `gouvernance`, `docs/`.

## OWNED DIRECTORIES

- `CLAUDE.md`, `AGENTS.md`
- `.claude/skills/`, `.claude/hooks/`, `.claude/settings.json`, `.claude/memory/`
- `docs/`
- `README.MD`, `Amelioration_futur.md`, `Test_Report.md`

## REQUIRED DEPENDENCIES

- Hook `.claude/hooks/check-context-budget.sh` (déclaré dans `.claude/settings.json`) — applique le budget automatiquement.
- Hook `.claude/hooks/sync-memory.sh` — sauvegarde la mémoire projet dans `.claude/memory/` (versionnée git) ; `restore` sur une nouvelle machine.

## RELATED DOCUMENTATION

- Mécanisme d'origine : repo trafric-website, `.claude/skills/context-governance/SKILL.md`.

---

## Budget

| Fichier | Budget | Application |
|---------|--------|-------------|
| `CLAUDE.md` | **120 lignes max** | Hook automatique — bloque tout dépassement |
| `MEMORY.md` (mémoire) | index d'une ligne par mémoire | Détail dans des fichiers thématiques |
| Un skill | ~120 lignes | Au-delà, scinder ou renvoyer vers docs/ |

## Règles de placement — où va quoi

| Contenu | Emplacement |
|---------|-------------|
| Règle nécessaire à CHAQUE session (stack, secrets, commandes, déploiement) | `CLAUDE.md` — compact |
| Workflow déclenché par un TYPE de tâche (déploiement Render, migration Flyway future) | Skill dans `.claude/skills/<nom>/` |
| Référence consultable (procédures, incidents, guides existants) | `docs/*.md` (déjà présent : `EVALUATION_ET_SUGGESTIONS.md`, `GUIDE_IMPLEMENTATION_PRATIQUE.md`, `ROADMAP_FISHCAM.md`) |
| Roadmap / dette technique connue | `Amelioration_futur.md` (ne pas dupliquer dans CLAUDE.md) |
| Résultats de tests | `Test_Report.md` (ne pas dupliquer dans CLAUDE.md) |
| Point d'entrée agents tiers (Codex, renvois courts) | `AGENTS.md` |
| Fait de session non dérivable du repo | Mémoire projet (`~/.claude/projects/<slug>/memory/`) — jamais CLAUDE.md |

## Procédure OBLIGATOIRE avant de modifier CLAUDE.md

1. **Skill d'abord** : règle liée à un type de tâche ? → créer/enrichir le skill ;
   CLAUDE.md reçoit au plus 1 ligne dans « Workflows → Skills ».
2. **Référence ensuite** : donnée consultable ? → `docs/`.
3. **Lien enfin** : la ressource existe ? → pointer, ne pas dupliquer.
4. **CLAUDE.md en dernier recours** : seulement si nécessaire à chaque session,
   en 1-3 lignes, en condensant l'existant si le budget approche.

Toute modification de CLAUDE.md doit le laisser plus petit ou égal, sauf justification explicite.

## Template OBLIGATOIRE pour tout skill projet

Frontmatter : `name` + `description` au format machine :
`"OBLIGATOIRE pour: <périmètre>. Déclencheurs: <mots-clés>. NE PAS utiliser pour: <exclusions>."`

Sections, dans cet ordre, avant le contenu : `PRIMARY RESPONSIBILITY`, `USE THIS SKILL WHEN`,
`DO NOT USE THIS SKILL WHEN`, `TRIGGERS`, `OWNED DIRECTORIES`, `REQUIRED DEPENDENCIES`,
`OPTIONAL DEPENDENCIES`, `RELATED DOCUMENTATION`.

## Checklist avant commit documentaire

- [ ] `wc -l CLAUDE.md` ≤ 120 ; skills ≤ ~120 lignes.
- [ ] Une information = un propriétaire ; les autres fichiers pointent.
- [ ] Aucun secret (mot de passe DB, JWT, clés R2/S3, tokens) dans un fichier versionné.
- [ ] Nouveaux skills conformes au template.

## Anti-patterns (interdits)

- Coller dans CLAUDE.md le détail d'une feature livrée (→ CHANGELOG/commit).
- Recopier une table/un flow déjà documenté (→ lien).
- Documenter deux fois « pour être sûr » (→ une source, des pointeurs).
- Stocker un état temporaire dans CLAUDE.md (→ mémoire projet).
