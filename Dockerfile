# Etapa 1: Build (Construção)
FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Etapa 2: Run (Execução)
# Alterado de openjdk:17-jdk-slim para eclipse-temurin:17-jdk-alpine
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY --from=build /app/target/fidelidade-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]