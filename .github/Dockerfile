FROM eclipse-temurin:23-jdk-alpine

WORKDIR /app

COPY gradlew ./
COPY gradle/wrapper/ gradle/wrapper/
RUN chmod +x gradlew

COPY build.gradle settings.gradle ./
COPY src/ src/

RUN ./gradlew build --no-daemon -x test

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "build/libs/geohod-backend-0.0.1-SNAPSHOT.jar"]