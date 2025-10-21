FROM docker.io/library/eclipse-temurin:23-jdk-alpine AS build

WORKDIR /app

RUN apk add --no-cache curl && \
    curl -L https://services.gradle.org/distributions/gradle-8.14.2-bin.zip -o gradle.zip && \
    unzip gradle.zip -d /opt/ && \
    rm gradle.zip && \
    ln -s /opt/gradle-8.14.2/bin/gradle /usr/local/bin/gradle

COPY gradlew gradlew.bat build.gradle settings.gradle ./
COPY gradle/ gradle/

COPY src/ src/

RUN ./gradlew build -x test --no-daemon

FROM docker.io/library/eclipse-temurin:23-jre-alpine AS runtime

WORKDIR /app

COPY --from=build /app/build/libs/*.jar geohod.jar

EXPOSE 8080

RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=90.0", "-jar", "geohod.jar"]
