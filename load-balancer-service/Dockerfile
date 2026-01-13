# Etapa 1: Build
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Copiem DOAR pom.xml mai întâi (cheia pentru caching!)
COPY pom.xml .
# Descărcăm dependențele. Acest pas va fi refăcut DOAR dacă modifici pom.xml, nu și codul sursă.
RUN mvn dependency:go-offline

# Abia acum copiem sursele
COPY src ./src
# Compilăm (mult mai rapid acum)
RUN mvn clean package -DskipTests

# Etapa 2: Runtime (imaginea finală mică)
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]