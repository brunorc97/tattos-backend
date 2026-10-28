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

# Extrai as camadas do jar (Boot 4: dependencies/lib + application/app.jar) para cache eficiente
RUN java -Djarmode=tools -jar build/libs/*.jar extract --layers --destination extracted \
    && mv extracted/application/*.jar extracted/application/app.jar

# ── Estágio 2: Runtime ───────────────────────────────────────
FROM eclipse-temurin:25-jre
WORKDIR /app

# curl para o HEALTHCHECK
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

# Cria usuário não-root, diretório de logs e define a propriedade antes de alternar o usuário
RUN groupadd -r tattos && useradd -r -g tattos tattos \
    && mkdir -p /app/logs && chown -R tattos:tattos /app

# Alterne para o usuário não-root com segurança
USER tattos

# Camadas do jar copiadas separadamente: dependências (lib/) mudam menos que o código
COPY --from=builder --chown=tattos:tattos /app/extracted/dependencies/ ./
COPY --from=builder --chown=tattos:tattos /app/extracted/snapshot-dependencies/ ./
COPY --from=builder --chown=tattos:tattos /app/extracted/application/ ./

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:8081/actuator/health || curl -f http://localhost:8080/actuator/health || exit 1

ENV JAVA_OPTS=""

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS \
  -XX:+UseContainerSupport \
  -XX:MaxRAMPercentage=75.0 \
  -Djava.security.egd=file:/dev/./urandom \
  -jar app.jar"]
