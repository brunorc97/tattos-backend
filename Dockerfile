# syntax=docker/dockerfile:1

# ── Estágio 1: Build ─────────────────────────────────────────
FROM --platform=$BUILDPLATFORM eclipse-temurin:25-jdk AS builder
WORKDIR /app

# Copia arquivos do Gradle para aproveitar cache de camadas
COPY gradlew settings.gradle build.gradle ./
COPY gradle ./gradle

# Garante permissão de execução para o gradlew
RUN chmod +x ./gradlew

# Baixa dependências (falha silenciosa se offline)
RUN ./gradlew dependencies --no-daemon || true

# Copia o código-fonte e compila o projeto
COPY src ./src
RUN ./gradlew bootJar --no-daemon -x test

# ── Estágio 2: Runtime ───────────────────────────────────────
FROM eclipse-temurin:25-jre
WORKDIR /app

# Cria usuário não-root por segurança
RUN groupadd -r tattos && useradd -r -g tattos tattos
USER tattos

# Copia o artefato JAR gerado no estágio de build
COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
