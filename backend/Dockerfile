# syntax=docker/dockerfile:1
# Railway: set Root Directory = backend, Builder = Dockerfile

FROM maven:3.9.9-eclipse-temurin-21-alpine AS build
WORKDIR /app

COPY pom.xml .
RUN mvn -B -q dependency:go-offline -DskipTests

COPY src ./src
RUN mvn -B -q -DskipTests package \
    && cp target/vimean-baby-*.jar /app/app.jar

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN apk add --no-cache curl \
    && addgroup -S app \
    && adduser -S app -G app

COPY --from=build /app/app.jar ./app.jar

USER app

# Railway injects PORT; Spring maps it via SERVER_PORT / PORT in application.yml
ENV SPRING_PROFILES_ACTIVE=prod
EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
  CMD curl -fsS "http://127.0.0.1:${PORT:-8080}/actuator/health" || exit 1

ENTRYPOINT ["sh", "-c", "java -XX:MaxRAMPercentage=75.0 -Dserver.port=${PORT:-${SERVER_PORT:-8080}} -jar app.jar"]
