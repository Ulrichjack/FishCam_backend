# ─── Build multi-stage : Render build l'image depuis un clone git frais,     ───
# ─── donc target/*.jar (gitignore) n'existe pas encore. Il faut le construire ───
# ─── ici avant de le copier dans l'image finale.                            ───

# Stage 1 : build du jar avec Maven (wrapper du projet)
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app
COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -B dependency:go-offline
COPY src ./src
RUN ./mvnw -B clean package -DskipTests

# Stage 2 : image d'exécution légère (JRE seul, pas de JDK/Maven)
FROM eclipse-temurin:17-jre-alpine
RUN apk add --no-cache postgresql-client
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar app.jar"]
