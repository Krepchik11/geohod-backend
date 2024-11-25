FROM eclipse-temurin:23-jre-alpine AS runtime

WORKDIR /app

COPY ./build/libs/*.jar geohod.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "geohod.jar"]