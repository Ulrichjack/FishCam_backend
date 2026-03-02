# 🚀 GUIDE PRATIQUE - AMÉLIORATIONS FISHCAM
# Pour déploiement ON-PREMISE (machine locale entreprise)

---

## 📋 CONTEXTE DE TON PROJET

### Situation actuelle
✅ Application Spring Boot fonctionnelle
✅ Base de données (probablement PostgreSQL ou MySQL)
✅ API REST complète (Clients, Dettes, Épargnes, Notifications)
✅ Validations métier en place

### Objectif final
🎯 Système tournant sur **une seule machine** dans l'entreprise
🎯 Démarrage automatique au boot (Docker Compose)
🎯 Accès via navigateur sur le réseau local
🎯 **Pas de cloud** - Tout reste dans l'entreprise

---

## 🔥 PRIORITÉ 1 : SÉCURITÉ JWT (ESSENTIEL)
**Durée estimée : 1-2 jours**

### Pourquoi c'est critique ?
Actuellement, n'importe qui peut mettre `User-Id: 5` dans le header et se faire passer pour quelqu'un d'autre.

### Ce que ça fait
- Le patron se connecte avec son login/mot de passe
- Le système lui donne un **token JWT** (comme un badge électronique)
- Chaque requête envoie ce token
- Le système vérifie automatiquement qui est connecté

### Étapes d'implémentation

#### ÉTAPE 1 : Ajouter Spring Security
**Fichier : `pom.xml`**
```xml
<!-- Ajouter ces dépendances -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.11.5</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.11.5</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.11.5</version>
</dependency>
```

#### ÉTAPE 2 : Créer le service JWT
**Nouveau package : `infrastructure/security`**
**Nouveau fichier : `JwtService.java`**

Ce service va :
- Générer un token quand quelqu'un se connecte
- Vérifier que le token est valide
- Extraire l'utilisateur du token

**Méthodes principales :**
- `generateToken(User user)` → Crée le token
- `validateToken(String token)` → Vérifie si valide
- `getUserFromToken(String token)` → Récupère l'utilisateur

#### ÉTAPE 3 : Créer l'endpoint de login
**Nouveau controller : `AuthController.java`**

**Endpoint POST `/api/v1/auth/login`**
```
Request:
{
  "username": "patron@fishcam.com",
  "password": "motdepasse123"
}

Response:
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": 1,
    "firstName": "Jean",
    "role": "PATRON"
  }
}
```

#### ÉTAPE 4 : Modifier les controllers existants
**Avant :**
```java
@PostMapping
public ApiResponse<DetteResponse> createDette(
    @RequestHeader("User-Id") Long userId  // ❌ Pas sécurisé
)
```

**Après :**
```java
@PostMapping
public ApiResponse<DetteResponse> createDette(
    Authentication authentication  // ✅ Automatique
) {
    User currentUser = (User) authentication.getPrincipal();
    // currentUser contient l'utilisateur connecté
}
```

#### ÉTAPE 5 : Configuration Security
**Nouveau fichier : `SecurityConfig.java`**

Ce fichier configure :
- Quelles URL sont publiques (login)
- Quelles URL nécessitent une authentification
- Comment vérifier les tokens

**URLs publiques :**
- `/api/v1/auth/login` → Tout le monde
- `/swagger-ui/**` → Documentation (optionnel)

**URLs protégées :**
- Tout le reste nécessite un token valide

### Test
1. Lance l'application
2. Essaie d'accéder à `/api/v1/clients` → **401 Unauthorized**
3. Appelle `/api/v1/auth/login` avec login/mot de passe → Tu reçois un token
4. Ajoute le token dans le header : `Authorization: Bearer <ton-token>`
5. Accède à `/api/v1/clients` → **200 OK** ✅

---

## 📊 PRIORITÉ 2 : LOGS (DÉBOGAGE)
**Durée estimée : 1 jour**

### Pourquoi c'est important ?
Quand il y a un problème :
- "Le patron ne peut pas créer de dette"
- "L'épargne a disparu"
- "La dette n'a pas été remboursée"

→ Les logs te disent **exactement ce qui s'est passé**

### Ce que ça fait
Écrit dans un fichier tout ce qui se passe :
```
2025-12-14 10:30:15 INFO  Dette créée pour client 5 par user 1
2025-12-14 10:30:20 INFO  Remboursement 2000 FCFA sur dette 12
2025-12-14 10:30:25 ERROR Erreur création dette - Client 999 introuvable
```

### Étapes d'implémentation

#### ÉTAPE 1 : Ajouter Lombok pour @Slf4j
Déjà fait si tu utilises Lombok ✅

#### ÉTAPE 2 : Ajouter @Slf4j sur tous les services
**Dans chaque service :**
```java
@Slf4j  // ← Ajouter cette annotation
@Service
@RequiredArgsConstructor
public class DetteService {
    // ...
}
```

#### ÉTAPE 3 : Ajouter des logs aux endroits clés
**Début de chaque méthode importante :**
```java
log.info("Création dette pour client {} par user {}", clientId, userId);
```

**Après succès :**
```java
log.info("Dette {} créée avec succès - Montant: {}", dette.getId(), dette.getInitialAmount());
```

**En cas d'erreur :**
```java
log.error("Erreur création dette pour client {}", clientId, e);
```

#### ÉTAPE 4 : Configuration des logs
**Fichier : `application.yml` ou `application.properties`**

**application.yml :**
```yaml
logging:
  file:
    name: /var/log/fishcam/application.log  # Emplacement du fichier
  level:
    com.fishcam: INFO          # Ton application en INFO
    org.springframework: WARN  # Spring en WARN seulement
  pattern:
    file: "%d{yyyy-MM-dd HH:mm:ss} - %msg%n"
```

**application.properties :**
```properties
logging.file.name=/var/log/fishcam/application.log
logging.level.com.fishcam=INFO
logging.level.org.springframework=WARN
```

### Rotation des logs (pour ne pas remplir le disque)
**Fichier : `logback-spring.xml`**
```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>/var/log/fishcam/application.log</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <!-- Nouveau fichier chaque jour -->
            <fileNamePattern>/var/log/fishcam/application-%d{yyyy-MM-dd}.log</fileNamePattern>
            <!-- Garder 30 jours d'historique -->
            <maxHistory>30</maxHistory>
        </rollingPolicy>
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>
    <root level="INFO">
        <appender-ref ref="FILE" />
    </root>
</configuration>
```

### Où ajouter des logs ?

**Services :**
- ✅ Début de chaque méthode (info)
- ✅ Après succès (info)
- ✅ Erreurs (error)

**Controllers :**
- ✅ Pas besoin (Spring Boot les logs automatiquement)

**Repositories :**
- ✅ Pas besoin (Hibernate les logs)

### Consulter les logs
```bash
# Voir les dernières lignes
tail -f /var/log/fishcam/application.log

# Chercher une erreur
grep "ERROR" /var/log/fishcam/application.log

# Chercher un client spécifique
grep "client 5" /var/log/fishcam/application.log
```

---

## 🧪 PRIORITÉ 3 : TESTS UNITAIRES (CONFIANCE)
**Durée estimée : 2-3 jours**

### Pourquoi c'est important ?
- Tu modifies le code → Les tests te disent si tu as cassé quelque chose
- Nouveau développeur → Les tests documentent comment ça marche
- Avant mise en prod → Tu lances les tests pour vérifier

### Ce que ça fait
Lance automatiquement des scénarios :
- ✅ "Créer une dette de 5000 FCFA" → Doit réussir
- ❌ "Rembourser 99999 FCFA sur une dette de 1000" → Doit échouer
- ✅ "Payer dette avec épargne" → Les 2 doivent être mis à jour

### Structure des tests

#### ÉTAPE 1 : Créer le package de tests
```
src/test/java/com/fishcam/
├── application/
│   ├── dette/
│   │   └── DetteServiceTest.java
│   ├── epargne/
│   │   └── EpargneServiceTest.java
│   └── client/
│       └── ClientServiceTest.java
```

#### ÉTAPE 2 : Tests essentiels par service

**DetteServiceTest.java** - 10 tests minimum
1. ✅ Créer une dette normale
2. ✅ Créer une dette > 5000 → Vérifier alerte envoyée
3. ❌ Créer dette avec client inexistant → Erreur
4. ❌ Créer dette client d'une autre poissonnerie → Erreur
5. ✅ Rembourser partiellement
6. ✅ Solder une dette → Vérifier notification
7. ❌ Rembourser montant > dette → Erreur
8. ❌ Rembourser dette déjà soldée → Erreur
9. ✅ Voir détail dette avec historique
10. ✅ Top débiteurs

**EpargneServiceTest.java** - 8 tests minimum
1. ✅ Créer un compte épargne
2. ❌ Créer 2 comptes pour même client → Erreur
3. ✅ Faire un dépôt
4. ✅ Faire un retrait
5. ❌ Retirer plus que le solde → Erreur
6. ✅ Voir historique transactions
7. ✅ Calculer total dépôts
8. ✅ Calculer total retraits

**PayerDetteAvecEpargneUseCaseTest.java** - 6 tests
1. ✅ Payer dette avec épargne
2. ✅ Solder dette complètement avec épargne
3. ❌ Épargne insuffisante → Erreur
4. ❌ Dette et épargne de clients différents → Erreur
5. ❌ Dette déjà soldée → Erreur
6. ✅ Vérifier les 2 transactions créées

#### ÉTAPE 3 : Configuration des tests
**Fichier : `application-test.yml`**
```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb  # Base en mémoire pour tests
    driver-class-name: org.h2.Driver
  jpa:
    hibernate:
      ddl-auto: create-drop  # Recrée la DB à chaque test
```

#### ÉTAPE 4 : Lancer les tests
```bash
# Tous les tests
./mvnw test

# Un test spécifique
./mvnw test -Dtest=DetteServiceTest

# Avec rapport de couverture
./mvnw test jacoco:report
```

### Règle d'or
**Avant chaque commit → Lance les tests !**
Si un test échoue → Ne commit pas.

---

## 📈 PRIORITÉ 4 : DASHBOARD STATISTIQUES
**Durée estimée : 2-3 jours**

### Pourquoi c'est important pour le Patron ?
Quand le patron ouvre l'application, il veut voir **d'un coup d'œil** :
- 💰 Combien d'argent est dû (dettes actives)
- 💵 Combien d'épargne il y a
- 👥 Nombre de clients
- 📊 Tendances du mois

### Nouveau endpoint
**`GET /api/v1/poissonneries/{id}/dashboard`**

**Réponse :**
```json
{
  "poissonnerieId": 1,
  "poissonnerieName": "AKWA CENTRE",
  "dateGeneration": "2025-12-14T10:30:00",
  
  "clients": {
    "total": 45,
    "actifs": 42,
    "inactifs": 3
  },
  
  "dettes": {
    "nombreActives": 12,
    "nombreSoldees": 156,
    "totalActif": 234500.00,
    "moyenneParClient": 19541.67,
    "plusGrosseDette": 45000.00,
    "topDebiteurs": [
      {
        "client": {"id": 5, "nom": "Alice Kamga"},
        "montant": 45000.00
      },
      // ...top 5
    ]
  },
  
  "epargnes": {
    "nombreComptes": 28,
    "totalEpargne": 890000.00,
    "moyenneParCompte": 31785.71,
    "plusGrosseEpargne": 120000.00
  },
  
  "tendancesMoisCourant": {
    "nouveauxClients": 3,
    "nouvellesdettes": 8,
    "dettesCreees": 125000.00,
    "dettesRemboursees": 89000.00,
    "depotsEpargne": 145000.00,
    "retraitsEpargne": 23000.00
  },
  
  "alertes": {
    "dettesElevees": 4,  // > 5000 FCFA
    "clientsInactifsSansRemboursement": 2
  }
}
```

### Implémentation

#### Nouveau service : `StatistiquesService.java`

**Méthodes à créer :**
1. `getStatistiquesClients(Poissonnerie)` → Stats clients
2. `getStatistiquesDettes(Poissonnerie)` → Stats dettes
3. `getStatistiquesEpargnes(Poissonnerie)` → Stats épargnes
4. `getTendancesMoisCourant(Poissonnerie)` → Tendances
5. `getDashboardComplet(Poissonnerie)` → Tout assemblé

### Optimisation
**Utiliser des requêtes SQL directes pour performance :**
```java
@Query("SELECT COUNT(d), SUM(d.remainingAmount) FROM Dette d WHERE d.poissonnerie = :p AND d.statut = 'ACTIVE'")
Object[] getStatsDetteRapide(@Param("p") Poissonnerie poissonnerie);
```

---

## 🔔 PRIORITÉ 5 : NOTIFICATIONS INTELLIGENTES
**Durée estimée : 1 jour**

### Ce que le Patron veut voir en se connectant

#### 1. Badge rouge "Notifications non lues"
**Endpoint déjà existant :**
`GET /api/v1/notifications/user/{userId}/unread-count`

**Le frontend affiche :**
```
🔔 (5)  ← Badge rouge avec nombre
```

#### 2. Liste des notifications triées
**Endpoint déjà existant :**
`GET /api/v1/notifications/user/{userId}`

**Affichage :**
```
🔴 Attention : Client Alice Kamga a une dette de 12000 FCFA (il y a 2h)
✅ Bravo ! Dette de Moussa Ndongo soldée (il y a 1 jour)
⚠️ Rappel : 12 clients ont des dettes impayées pour 234500 FCFA (Dimanche)
```

#### 3. Amélioration : Filtres de notifications

**Nouveau endpoint :**
`GET /api/v1/notifications/user/{userId}/filter?type=DETTE_ELEVEE&read=false`

**Types de filtres :**
- `type` : DETTE_ELEVEE, DETTE_SOLDEE, ALERTE_DIMANCHE
- `read` : true/false
- `date` : après telle date

#### 4. Amélioration : Marquer toutes comme lues

**Nouveau endpoint :**
`PUT /api/v1/notifications/user/{userId}/mark-all-read`

### Notifications à ajouter

#### Alerte : Client inactif depuis 30 jours avec dette
**Quand ?** Tâche planifiée hebdomadaire

**Message :**
```
⚠️ Client Jean Doe n'a pas remboursé depuis 30 jours (Dette: 8000 FCFA)
```

#### Info : Objectif épargne atteint
**Quand ?** Dépôt qui atteint l'objectif

**Message :**
```
🎉 Client Alice a atteint son objectif d'épargne de 50000 FCFA !
```

---

## 🐳 PRIORITÉ 6 : DOCKERISATION (DÉPLOIEMENT)
**Durée estimée : 1 jour**

### Pourquoi Docker ?
**Problème actuel :**
- Il faut installer Java 17
- Il faut installer PostgreSQL
- Il faut configurer la base de données
- Il faut lancer Spring Boot manuellement

**Avec Docker :**
- Un seul fichier `docker-compose.yml`
- Lance tout d'un coup : `docker-compose up`
- Redémarre automatiquement au boot de la machine

### Architecture finale
```
Machine entreprise
├── Backend Spring Boot (Docker container)
├── Base de données PostgreSQL (Docker container)
└── Frontend Angular (Docker container - futur)

Tous communiquent sur un réseau Docker interne
Accès via http://192.168.1.50:8080 (IP locale)
```

### Fichiers à créer

#### 1. Dockerfile (Backend)
**Fichier : `Dockerfile`** dans la racine du projet

Ce fichier dit comment construire l'image Docker de ton backend.

**Étapes du Dockerfile :**
1. Utilise une image Java 17
2. Copie ton `.jar` compilé
3. Expose le port 8080
4. Lance l'application

#### 2. docker-compose.yml (Tout le système)
**Fichier : `docker-compose.yml`** dans la racine

**Services à définir :**

**Service 1 : Base de données**
- Image : postgres:15
- Port : 5432
- Volume : données persistantes
- Variables : POSTGRES_DB, POSTGRES_USER, POSTGRES_PASSWORD

**Service 2 : Backend**
- Build depuis Dockerfile
- Port : 8080
- Dépend de : database
- Variables : URL BDD, user, password

**Service 3 : Frontend (plus tard)**
- Image : nginx
- Port : 80
- Sert les fichiers Angular

#### 3. Script de démarrage
**Fichier : `start.sh`**

```bash
#!/bin/bash
# Construit les images
docker-compose build

# Lance tout
docker-compose up -d

# Affiche les logs
docker-compose logs -f
```

### Configuration production

**application-prod.yml :**
```yaml
spring:
  datasource:
    url: jdbc:postgresql://database:5432/fishcam
    username: fishcam_user
    password: ${DB_PASSWORD}  # Variable d'environnement
  jpa:
    hibernate:
      ddl-auto: validate  # NE PAS recréer la DB en prod !
    
server:
  port: 8080
  
logging:
  file:
    name: /var/log/fishcam/application.log
```

### Démarrage automatique au boot

**Ubuntu/Debian :**
```bash
# Créer un service systemd
sudo nano /etc/systemd/system/fishcam.service
```

**Contenu du fichier :**
```ini
[Unit]
Description=FishCam Application
Requires=docker.service
After=docker.service

[Service]
Type=oneshot
RemainAfterExit=yes
WorkingDirectory=/opt/fishcam
ExecStart=/usr/bin/docker-compose up -d
ExecStop=/usr/bin/docker-compose down
TimeoutStartSec=0

[Install]
WantedBy=multi-user.target
```

**Activer :**
```bash
sudo systemctl enable fishcam
sudo systemctl start fishcam
```

### Accès réseau local

**IP de la machine :** Ex: `192.168.1.50`

**Accès depuis autres postes :**
- Backend API : `http://192.168.1.50:8080`
- Frontend (futur) : `http://192.168.1.50`

### Maintenance

**Voir les logs :**
```bash
docker-compose logs -f backend
```

**Redémarrer :**
```bash
docker-compose restart
```

**Mettre à jour :**
```bash
# Nouvelle version du code
git pull
docker-compose build
docker-compose up -d
```

**Backup base de données :**
```bash
docker-compose exec database pg_dump -U fishcam_user fishcam > backup_$(date +%Y%m%d).sql
```

---

## 📊 PRIORITÉ 7 : RAPPORTS & EXPORTS
**Durée estimée : 2-3 jours**

### Ce que le Patron veut imprimer

#### 1. Liste des dettes actives (PDF)
**`GET /api/v1/dettes/poissonnerie/{id}/export/pdf`**

**Contenu du PDF :**
```
POISSONNERIE AKWA CENTRE
Liste des Dettes Actives
Date: 14/12/2025

Client          | Téléphone   | Dette Initiale | Restant    | Depuis
----------------|-------------|----------------|------------|----------
Alice Kamga     | 677123456   | 12000 FCFA     | 8000 FCFA  | 10 jours
Jean Doe        | 699887766   | 5000 FCFA      | 5000 FCFA  | 3 jours
...

TOTAL: 234500 FCFA
```

#### 2. Historique client (PDF)
**`GET /api/v1/clients/{id}/historique/pdf`**

**Contenu :**
- Infos client
- Toutes ses dettes (actives + soldées)
- Son épargne actuelle
- Historique transactions épargne
- Graphique des remboursements

#### 3. Rapport mensuel (Excel)
**`GET /api/v1/poissonneries/{id}/rapport-mensuel/excel?mois=12&annee=2025`**

**Feuilles Excel :**
1. **Résumé** : Chiffres clés du mois
2. **Dettes** : Toutes les dettes créées
3. **Remboursements** : Tous les remboursements
4. **Épargnes** : Mouvements épargne
5. **Nouveaux clients** : Liste

### Bibliothèques à utiliser

**Pour PDF :**
- iText (payant en prod mais gratuit pour tester)
- Apache PDFBox (gratuit)

**Pour Excel :**
- Apache POI (gratuit)

---

## 🎯 PLAN D'IMPLÉMENTATION RECOMMANDÉ

### Semaine 1 : Sécurité & Stabilité
- ✅ Jour 1-2 : JWT + Security
- ✅ Jour 3 : Logs
- ✅ Jour 4-5 : Tests unitaires (minimum)

### Semaine 2 : Features Patron
- ✅ Jour 1-2 : Dashboard statistiques
- ✅ Jour 3 : Notifications améliorées
- ✅ Jour 4-5 : Tests + corrections bugs

### Semaine 3 : Déploiement
- ✅ Jour 1 : Dockerisation
- ✅ Jour 2 : Tests déploiement local
- ✅ Jour 3 : Script démarrage auto
- ✅ Jour 4-5 : Documentation + formation

### Semaine 4 : Nice to have
- ✅ Exports PDF/Excel
- ✅ Recherche avancée
- ✅ Peaufinage interface

---

## ✅ CHECKLIST AVANT MISE EN PRODUCTION

### Sécurité
- [ ] JWT implémenté et testé
- [ ] Tous les endpoints protégés
- [ ] Mots de passe hashés (BCrypt)
- [ ] HTTPS configuré (certificat SSL)

### Stabilité
- [ ] Logs configurés et testés
- [ ] Tests unitaires passent (minimum 50% couverture)
- [ ] Gestion d'erreurs complète
- [ ] Pas de données en dur dans le code

### Performance
- [ ] Requêtes SQL optimisées
- [ ] Index sur colonnes fréquentes
- [ ] Pagination partout
- [ ] Cache si nécessaire

### Déploiement
- [ ] Docker fonctionne
- [ ] Backup automatique configuré
- [ ] Démarrage auto au boot
- [ ] Accessible réseau local

### Documentation
- [ ] README avec instructions installation
- [ ] Guide utilisateur pour le Patron
- [ ] Guide maintenance (logs, backup)

---

## 🆘 TROUBLESHOOTING COMMUN

### "L'application ne démarre pas"
```bash
# Voir les logs
docker-compose logs backend

# Vérifier la base de données
docker-compose ps
```

### "Impossible de se connecter"
- Vérifier le firewall
- Vérifier l'IP de la machine
- Tester : `curl http://localhost:8080/api/v1/health`

### "La base de données est pleine"
```bash
# Backup puis suppression vieilles données
docker-compose exec database psql -U fishcam_user
DELETE FROM notification WHERE created_at < NOW() - INTERVAL '90 days';
```

### "Tout est lent"
- Ajouter des index SQL
- Augmenter la mémoire Docker
- Vérifier les requêtes N+1 (lazy loading)

---

## 📚 RESSOURCES UTILES

### Documentation officielle
- Spring Security : https://spring.io/guides/gs/securing-web/
- Docker Compose : https://docs.docker.com/compose/
- JWT : https://jwt.io/

### Tutoriels recommandés
- "Spring Boot JWT Tutorial" sur YouTube
- "Docker for Java Developers"
- "Writing Clean Tests in Spring Boot"

---

## 🎓 CONCLUSION

### Ce que tu dois faire EN PRIORITÉ
1. **JWT** (1-2 jours) - CRITIQUE pour sécurité
2. **Logs** (1 jour) - Essentiel pour debug
3. **Tests** (2-3 jours) - Confiance dans le code
4. **Docker** (1 jour) - Facilite tout le déploiement

Avec ces 4 choses, ton application est **production-ready** pour l'entreprise.

### Le reste peut attendre
- Dashboard → Fais-le en itératif (ajoute stats au fur et à mesure)
- Exports PDF → Nice to have, pas critique
- Notifications SMS → Pas nécessaire (notifications web suffisent)

**Bon courage ! Tu as déjà fait 80% du travail ! 🚀**
