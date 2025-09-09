# Étape 1 : utiliser une image Maven pour builder ton projet
FROM maven:eclipse-temurin AS build
# WORKDIR /app
# COPY pom.xml .
# COPY src ./src
# RUN mvn package -DskipTests

# Étape 2 : utiliser une image JDK légère pour exécuter l'app
# FROM eclipse-temurin:24-jdk
# WORKDIR /app
# COPY --from=build /app/target/myapp-1.0-SNAPSHOT.jar app.jar
# EXPOSE 4567
# ENTRYPOINT ["java", "-jar", "app.jar"]