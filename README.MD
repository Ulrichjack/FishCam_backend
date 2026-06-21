# 🐟 FishCam ERP — Système de Gestion des Poissonneries

> Système de gestion complet des dettes, épargnes et opérations quotidiennes pour les poissonneries du Cameroun.

> API REST robuste et sécurisée pour le système de gestion des poissonneries FishCam ERP au Cameroun.

![Java](https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.x-6DB33F?style=for-the-badge&logo=spring)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-4169E1?style=for-the-badge&logo=postgresql)
![Docker](https://img.shields.io/badge/Docker-Ready-2496ED?style=for-the-badge&logo=docker)

---

## 📋 Table des matières

- [Description](#-description)
- [Modules Implémentés](#-modules-implémentés)
- [Technologies](#-technologies)
- [Architecture](#-architecture)
- [Structure du projet](#-structure-du-projet)
- [Installation & Lancement](#-installation--lancement)
- [Sauvegardes Cloud (R2)](#-sauvegardes-cloud)
- [Sécurité & Rôles](#-sécurité--rôles)

---

## 💡 Description

FishCam ERP Backend est une application Spring Boot conçue spécifiquement pour les besoins des poissonneries. Elle gère la logique métier complexe liée aux dettes clients (comptes courants), aux épargnes, à la facturation des fournisseurs, et à la clôture journalière des caisses. 

Le système est **multi-poissonneries** : un `SUPER_ADMIN` ou un `PATRON` peut gérer plusieurs établissements depuis une seule interface centralisée.

---

## ✨ Modules Implémentés

| Module | Description |
|--------|-------------|
| 🔐 **Auth & Sécurité** | Login par numéro de téléphone + JWT. Protection IDOR stricte. |
| 👥 **Équipe & Rôles** | CRUD utilisateurs, gestion des rôles (Patron, Caissière, etc.). |
| 🏪 **Poissonneries** | Gestion multi-établissements (loyer, fond de caisse, statut). |
| 🤝 **Clients & Comptes** | CRUD clients, gestion des Comptes Courants (dettes) et Épargnes. |
| 💸 **Transactions** | Emprunts, remboursements, dépôts, retraits, transferts Épargne → CC. |
| 📦 **Achats & Factures** | Saisie des achats, calcul des marges, historique des prix fournisseurs. |
| 🚚 **Livreurs** | Gestion des livreurs et système d'évaluation (qualité, poids, problèmes). |
| 📊 **Clôture & Bilans** | Clôture de caisse journalière, calcul des écarts, bilans mensuels. |
| 🔔 **Notifications** | Alertes de dépassement de dette, rapports journaliers automatiques (Cron). |
| 📄 **Export PDF** | Génération de fiches d'épargne et de récapitulatifs avec iTextPDF. |
| ☁️ **Sauvegardes** | Export automatique SQL et CSV vers Cloudflare R2 (S3-compatible). |
| 📜 **Audit Logs** | Traçabilité complète des actions sensibles via AOP (Aspect-Oriented Programming). |

---

## 🛠️ Technologies

| Catégorie | Technologie |
|-----------|-------------|
| **Cœur** | Java 17, Spring Boot 3.x |
| **Sécurité** | Spring Security 6, JSON Web Tokens (JWT) |
| **Base de données** | PostgreSQL 16, Spring Data JPA, Hibernate |
| **Utilitaires** | Lombok, MapStruct (Mapping DTO) |
| **Génération PDF** | OpenPDF / iText |
| **Cloud Storage** | AWS SDK S3 (pour Cloudflare R2 / MinIO) |
| **Documentation** | Springdoc OpenAPI (Swagger UI) |

---

## 🏗️ Architecture

Le projet suit les principes de l'**Architecture Hexagonale (Ports & Adapters)** et du Clean Code :

```text
┌─────────────────────────────────────────────────────┐
│                    ADAPTER (Web)                    │
│  Controllers → DTOs (Request/Response) → Mappers    │
└──────────────────────┬──────────────────────────────┘
                       │
┌──────────────────────▼──────────────────────────────┐
│                  APPLICATION                        │
│         Services → UseCases → Business Logic        │
└──────────────────────┬──────────────────────────────┘
                       │
┌──────────────────────▼──────────────────────────────┐
│                    DOMAIN                           │
│         Entités JPA → Repositories (interfaces)     │
└──────────────────────┬──────────────────────────────┘
                       │
┌──────────────────────▼──────────────────────────────┐
│               INFRASTRUCTURE                        │
│   Security (JWT) · Exceptions · Scheduler · Cloud   │
└─────────────────────────────────────────────────────┘

```
# 📁 Structure du projet

``` text
src/main/java/com/fishcam/
├── adapter/web/          # Controllers REST, DTOs, Mappers (MapStruct)
├── application/          # Services métiers (Auth, Factures, Clients, etc.)
├── domain/               # Entités JPA et interfaces Repositories
└── infrastructure/       # Configuration Spring, Sécurité JWT, Schedulers, AOP
```

# 🚀 Installation & Lancement
* Prérequis
* Java 17+
* PostgreSQL 15+
* Maven 3.8+

1. Base de données
 ```sql
CREATE DATABASE fishcam_db;
CREATE USER fishcam_user WITH PASSWORD 'tonmotdepasse';
GRANT ALL PRIVILEGES ON DATABASE fishcam_db TO fishcam_user;
```
2. Configuration

Créez un fichier application-dev.properties dans src/main/resources/ :

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/fishcam_db
pring.datasource.username=fishcam_user
spring.datasource.password=tonmotdepasse
jwt.secret=une-cle-secrete-tres-longue-de-plus-de-32-caracteres
```
3. Lancement (Développement)
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
L'API sera accessible sur http://localhost:8080/api/v1.
Swagger UI : http://localhost:8080/swagger-ui.html
```
4. Déploiement (Production avec Docker)
L'application est packagée dans une image Alpine légère :
```bash
mvn clean package -DskipTests
docker build -t fishcam-backend .
```
# ☁️ Sauvegardes Cloud
Le système intègre une sauvegarde automatique de la base de données (Dump SQL) et des données d'apprentissage machine (CSV) vers Cloudflare R2 (compatible S3).
Variables d'environnement requises en production :
```env
CF_ENDPOINT=https://<ACCOUNT_ID>.r2.cloudflarestorage.com
CF_ACCESS_KEY=votre_access_key
CF_SECRET_KEY=votre_secret_key
CF_BUCKET=fishcam-backups
```
# 🛡️ Sécurité & Rôles
* Authentification : Basée sur le numéro de téléphone (pas d'email).
* SUPER_ADMIN : Accès total, gestion des poissonneries, accès aux logs d'audit.
* PATRON : Gestion de son équipe, modification des limites de crédit, accès aux * bilans.
* CAISSIERE : Gestion des transactions (Emprunts, Remboursements, Dépôts, Retraits).
* ENREGISTREUR : Saisie des factures d'achat fournisseurs.