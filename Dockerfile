FROM eclipse-temurin:21-jre-alpine AS runtime

WORKDIR /app

RUN addgroup -S omni && adduser -S omni -G omni

COPY omni-admin/target/omni-admin-*.jar /app/app.jar

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Djdk.tracePinnedThreads=short"
ENV SPRING_PROFILES_ACTIVE=prod

USER omni
EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
