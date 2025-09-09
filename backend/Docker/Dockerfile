# Étape 1 : utiliser une image Maven pour builder ton projet
FROM maven:eclipse-temurin AS build
WORKDIR /app

# Copier le pom.xml depuis le bon répertoire
COPY ./pom.xml ./pom.xml
# Copier le répertoire src depuis le bon répertoire
COPY ./src ./src

# Compiler le projet
ENTRYPOINT [ "./Docker/init.sh" ]