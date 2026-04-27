# 🐟 FishCam ERP — Système de Gestion des Poissonneries

> Système de gestion complet des dettes, épargnes et opérations quotidiennes pour les poissonneries du Cameroun.

---

## 📋 Table des matières

- [Description](#description)
- [Modules](#modules)
- [Technologies](#technologies)
- [Architecture](#architecture)
- [Structure du projet](#structure-du-projet)
- [Lancer le projet](#lancer-le-projet)
- [Configuration](#configuration)
- [API Documentation](#api-documentation)
- [Sécurité](#sécurité)

---

## Description

FishCam ERP est une application Spring Boot conçue pour les poissonneries. Elle permet de gérer les dettes clients, les épargnes, les utilisateurs par rôle, et génère des rapports et notifications automatiques. Le système est **multi-poissonneries** : un SUPER_ADMIN peut gérer plusieurs établissements depuis une seule interface.

---

## Modules

### ✅ Modules implémentés

| # | Module | Description |
|---|--------|-------------|
| 0.1 | **Auth** | Login par numéro de téléphone + JWT |
| 0.2 | **Gestion des utilisateurs** | CRUD utilisateurs, rôles, changement de mot de passe |
| 0.3 | **Gestion des poissonneries** | CRUD multi-établissements |
| 0.4 | **Gestion des clients** | CRUD clients rattachés à une poissonnerie |
| 0.5 | **Comptes courants (dettes)** | Emprunts, remboursements, limite de crédit |
| 0.6 | **Épargnes** | Dépôts, retraits, transfert épargne → compte courant |
| 0.7 | **Notifications** | Alertes dettes, rapports journaliers, sécurité IDOR |
| 0.8 | **Avatars utilisateurs** | Upload/download photo de profil (stockage local) |
| 0.9 | **Rapports résilients** | Rapport journalier même si PC éteint à 19h |

### 🔜 Modules à venir

| # | Module | Description |
|---|--------|-------------|
| 1.0 | **Rapports PDF** | Export PDF des rapports journaliers et historiques |
| 1.1 | **Dashboard statistiques** | Graphiques dettes, remboursements, épargnes |
| 1.2 | **Export Excel** | Export des transactions par période |
| 1.3 | **Rappels automatiques** | Notification chaque dimanche pour dettes en cours |

---

## Technologies

| Technologie | Version | Usage |
|-------------|---------|-------|
| Java | 17 | Langage principal |
| Spring Boot | 3.5.8 | Framework applicatif |
| Spring Security | 6.x | JWT, autorisation par rôle |
| Spring Data JPA | 3.x | Accès base de données |
| PostgreSQL | 15+ | Base de données |
| Liquibase | - | Migrations de schéma |
| MapStruct | - | Mapping entité ↔ DTO |
| Lombok | - | Réduction boilerplate |
| iTextPDF | - | Génération de rapports PDF |
| Springdoc OpenAPI | - | Documentation Swagger UI |
| JUnit 5 + Mockito | - | Tests unitaires et intégration |

---

## Architecture

Le projet suit une **architecture hexagonale (ports & adapters)** :

```
┌─────────────────────────────────────────────────────┐
│                    ADAPTER (Web)                     │
│  Controllers → DTOs (Request/Response) → Mappers    │
└──────────────────────┬──────────────────────────────┘
                       │
┌──────────────────────▼──────────────────────────────┐
│                  APPLICATION                         │
│         Services → UseCases → Business Logic        │
└──────────────────────┬──────────────────────────────┘
                       │
┌──────────────────────▼──────────────────────────────┐
│                    DOMAIN                            │
│         Entités JPA → Repositories (interfaces)     │
└──────────────────────┬──────────────────────────────┘
                       │
┌──────────────────────▼──────────────────────────────┐
│               INFRASTRUCTURE                         │
│   Security (JWT) · Exceptions · Scheduler · Config  │
└─────────────────────────────────────────────────────┘
```

---

## Structure du projet

```
src/main/java/com/fishcam/
│
├── adapter/
│   └── web/
│       ├── controller/
│       │   ├── AuthController.java
│       │   ├── AvatarController.java
│       │   ├── CompteCourantController.java
│       │   ├── NotificationController.java
│       │   ├── PoissonnerieController.java
│       │   └── UserController.java
│       ├── dto/
│       │   ├── request/          # LoginRequest, CreateUserRequest, EmpruntRequest...
│       │   └── response/         # ApiResponse, AuthResponse, UserResponse...
│       └── mapper/               # UserMapper, PoissonnerieMapper... (MapStruct)
│
├── application/
│   ├── auth/                     # AuthService
│   ├── comptecourant/            # CompteCourantService, TransfertEpargneVersCCUseCase
│   ├── notification/             # NotificationService
│   ├── poissonnerie/             # PoissonnerieService
│   └── user/                     # UserService, AvatarService
│
├── domain/
│   ├── comptecourant/            # CompteCourant, TransactionCC, repositories
│   ├── notification/             # Notification, RapportJournalierRecord, repositories
│   ├── poissonnerie/             # Poissonnerie, repository
│   └── user/                     # User, Role, UserScope, UserRepository
│
└── infrastructure/
    ├── exception/                # GlobalExceptionHandler, BusinessException...
    ├── scheduler/                # NotificationScheduler (@Scheduled + @EventListener)
    └── security/                 # JwtService, JwtAuthenticationFilter,
                                  # CustomUserDetailsService, SecurityConfig

src/main/resources/
├── application.properties        # Config commune
├── application-dev.properties    # Config développement (BDD locale)
└── application-prod.properties   # Config production

fishcam-data/                     # Données locales (hors JAR)
└── avatars/                      # Photos de profil utilisateurs
    ├── 550e8400-...jpg
    └── f47ac10b-...png
```

---

## Lancer le projet

### Prérequis

- Java 17+
- PostgreSQL 15+
- Maven 3.8+

### 1. Cloner le projet

```bash
git clone https://github.com/ton-repo/fishcam-backend.git
cd fishcam-backend
```

### 2. Créer la base de données

```sql
CREATE DATABASE fishcam_db;
CREATE USER fishcam_user WITH PASSWORD 'tonmotdepasse';
GRANT ALL PRIVILEGES ON DATABASE fishcam_db TO fishcam_user;
```

### 3. Configurer l'environnement

```bash
cp src/main/resources/application-dev.properties.example \
   src/main/resources/application-dev.properties
```

Puis remplir :

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/fishcam_db
spring.datasource.username=fishcam_user
spring.datasource.password=tonmotdepasse
jwt.secret=une-cle-secrete-longue-et-aleatoire
fishcam.upload.avatar-dir=fishcam-data/avatars
```

### 4. Lancer en développement

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### 5. Lancer en production

```bash
mvn clean package -DskipTests
java -jar target/fishcam-backend.jar --spring.profiles.active=prod
```

---

## Configuration

### `application.properties` (commun)

```properties
spring.application.name=fishcam
spring.profiles.active=dev

# Multipart (upload avatars)
spring.servlet.multipart.max-file-size=5MB
spring.servlet.multipart.max-request-size=5MB

# Upload local
fishcam.upload.avatar-dir=fishcam-data/avatars

# Scheduler activé
spring.task.scheduling.enabled=true
```

### Variables sensibles — ne jamais commiter

```properties
# JWT
jwt.secret=...
jwt.expiration=86400000

# BDD
spring.datasource.password=...
```

> ⚠️ Ajouter `application-prod.properties` et `application-dev.properties` dans `.gitignore`

---

## API Documentation

Swagger UI disponible après démarrage :

```
http://localhost:8080/swagger-ui.html
```

### Endpoints principaux

| Méthode | Endpoint | Description | Rôles |
|---------|----------|-------------|-------|
| POST | `/api/v1/auth/login` | Connexion | Public |
| GET | `/api/v1/auth/me` | Utilisateur connecté | Tous |
| PUT | `/api/v1/auth/change-password` | Changer son mot de passe | Tous |
| PUT | `/api/v1/auth/reset-password` | Réinitialiser MDP d'un employé | PATRON, SUPER_ADMIN |
| POST | `/api/v1/users` | Créer un utilisateur | SUPER_ADMIN, PATRON |
| POST | `/api/v1/users/{id}/avatar` | Upload photo de profil | Tous (le sien) |
| GET | `/api/v1/users/{id}/avatar` | Voir photo de profil | Tous |
| POST | `/api/v1/comptes-courants/emprunts` | Enregistrer un emprunt | CAISSIERE+ |
| POST | `/api/v1/comptes-courants/remboursements` | Enregistrer un remboursement | CAISSIERE+ |
| POST | `/api/v1/comptes-courants/transfert-epargne` | Épargne → Compte courant | CAISSIERE+ |
| GET | `/api/v1/notifications/user/{userId}` | Mes notifications | Tous (le sien) |
| PUT | `/api/v1/notifications/{id}/mark-as-read` | Marquer comme lue | Tous (la sienne) |
| POST | `/api/v1/poissonneries/{id}/cloturer-journee` | Clôture manuelle journée | PATRON, SUPER_ADMIN |

---

## Sécurité

### Rôles

| Rôle | Permissions |
|------|-------------|
| `SUPER_ADMIN` | Accès total, toutes les poissonneries |
| `PATRON` | Gestion de sa poissonnerie, modification limites crédit |
| `CAISSIERE` | Emprunts, remboursements, transferts, comptes courants |
| `ENREGISTREUR` | Lecture seule + enregistrement emprunts |

### JWT

- Token transmis dans le header : `Authorization: Bearer <token>`
- Identifiant = **numéro de téléphone** (pas d'email)
- Tokens expirés ou malformés → `401 Unauthorized` propre

### Protection IDOR

Les endpoints sensibles vérifient que l'utilisateur connecté accède uniquement à ses propres données. Seul le `SUPER_ADMIN` peut accéder aux données d'un autre utilisateur. La logique de vérification est dans la **couche service**, pas dans le controller.

---

## Branches Git

```
main              ← production stable
develop           ← intégration continue
feature/*         ← nouvelles fonctionnalités
fix/*             ← corrections de bugs
```

---

*FishCam ERP — Java 17 · Spring Boot 3.5.8 · PostgreSQL · Architecture Hexagonale*