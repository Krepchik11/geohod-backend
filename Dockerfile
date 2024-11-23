FROM eclipse-temurin:23-jdk-alpine AS build

WORKDIR /app

COPY gradlew gradlew.bat settings.gradle build.gradle /app/
COPY gradle /app/gradle

RUN ./gradlew dependencies --no-daemon

COPY src /app/src

RUN ./gradlew clean build --no-daemon -x test

FROM eclipse-temurin:23-jre-alpine AS runtime

WORKDIR /app

COPY --from=build /app/build/libs/*.jar geohod.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "geohod.jar"]