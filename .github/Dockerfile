FROM eclipse-temurin:23-jre-alpine

RUN addgroup -S appgroup && adduser -S appuser -G appgroup

COPY --chown=appuser:appgroup build/libs/*.jar /app/app.jar 

USER appuser

WORKDIR /app

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]