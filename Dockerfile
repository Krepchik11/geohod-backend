# Stage 1: Build
FROM eclipse-temurin:23-jdk-alpine AS builder
WORKDIR /build
COPY gradlew ./
COPY gradle/wrapper/ gradle/wrapper/
RUN chmod +x gradlew
COPY build.gradle settings.gradle ./
RUN ./gradlew dependencies --no-daemon
COPY src/ src/
RUN ./gradlew bootJar --no-daemon -x test

# Stage 2: Runtime
FROM eclipse-temurin:23-jre-alpine
# wget is not present by default in the Alpine JRE image — required by HEALTHCHECK
RUN apk add --no-cache wget && \
    addgroup -S appgroup && adduser -S appuser -G appgroup
WORKDIR /app
COPY --from=builder --chown=appuser:appgroup /build/build/libs/geohod-backend-*.jar app.jar
USER appuser
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
  CMD wget -qO- http://localhost:8080/actuator/health || exit 1
ENTRYPOINT ["java", "-jar", "app.jar"]
