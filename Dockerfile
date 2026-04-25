# ETAPA 1: Compilación (Build)
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
# Copiamos el pom.xml y descargamos dependencias (aprovechando el caché de capas de Docker)
COPY pom.xml .

#Descarga las dependencias antes de compilar
RUN mvn dependency:go-offline
# Copiamos el código fuente y compilamos
COPY src ./src
RUN mvn clean package -DskipTests

# ETAPA 2: Ejecución (Runtime)
FROM eclipse-temurin:21-jre
WORKDIR /app
# Copiamos solo el archivo .jar generado en la etapa anterior
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
