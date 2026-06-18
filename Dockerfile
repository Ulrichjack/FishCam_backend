# ─── SKELETON: Dockerfile (Backend Spring Boot) ───────────────────────────

# DIRECTIVE: Utilise l'image de base 'eclipse-temurin:17-jre-alpine'
# YOUR CODE HERE
FROM eclipse-temurin:17-jre-alpine

# DIRECTIVE: Définis le répertoire de travail sur '/app'
# YOUR CODE HERE
WORKDIR /app

# DIRECTIVE: Copie le fichier JAR généré par Maven depuis ton PC vers le container.
# Le fichier source est 'target/*.jar' (l'étoile permet de prendre le jar peu importe sa version exacte).
# Le fichier de destination dans le container sera 'app.jar'.
# YOUR CODE HERE
COPY target/*.jar app.jar

# DIRECTIVE: Expose le port 8080 (le port par défaut de Spring Boot)
# YOUR CODE HERE
EXPOSE 8080

# DIRECTIVE: Définis la commande de démarrage (ENTRYPOINT).
# Elle doit exécuter un tableau JSON : ["java", "-jar", "app.jar"]
# YOUR CODE HERE
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar app.jar"]
