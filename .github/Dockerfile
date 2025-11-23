FROM eclipse-temurin:23-jdk-alpine AS builder

WORKDIR /app

COPY gradlew ./
COPY gradle/wrapper/ gradle/wrapper/
RUN chmod +x gradlew

COPY build.gradle settings.gradle ./
COPY src/ src/

RUN ./gradlew build --no-daemon

FROM eclipse-temurin:23-jre-alpine

WORKDIR /app

COPY --from=builder /app/build/libs/ build/libs/
COPY --from=builder /app/build/classes/java/main/ build/classes/java/main/
COPY --from=builder /app/build/resources/main/ build/resources/main/

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "build/libs/geohod-backend-0.0.1-SNAPSHOT.jar"]