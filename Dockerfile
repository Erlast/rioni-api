# The official `openjdk` Docker Hub library is deprecated and no longer
# publishes images for JDK 17+. Use Eclipse Temurin (Adoptium) instead.
#
# The application jar is built on the HOST (see the Makefile), not inside this
# image. Building inside the container previously required downloading every
# Gradle dependency from Maven Central over the Docker proxy/VPN tunnel, which
# intermittently failed with "Remote host terminated the handshake" (TLS).
# Building on the host reuses the local Gradle cache and is deterministic.
FROM eclipse-temurin:24-jdk-noble

WORKDIR /app

# The fat jar produced by `./gradlew build -x test` (or `./gradlew bootJar`).
COPY build/libs/api-1.0.0.jar /app/api-1.0.0.jar

# Directory mounted as a volume by docker-compose (./uploads:/app/uploads).
RUN mkdir -p /app/uploads

ENV JAR_NAME=api-1.0.0.jar
ENV APP_HOME=/app
EXPOSE 8080 5005

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar $APP_HOME/$JAR_NAME"]
