FROM eclipse-temurin:23-jre-alpine AS runtime

WORKDIR /app

COPY ./build/libs/*.jar geohod.jar

EXPOSE 8080

RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "geohod.jar"]
