# Multi-stage Dockerfile: build with Maven (OpenJDK 25) and run on small JRE image
FROM maven:3.9.12-eclipse-temurin-25 AS builder
WORKDIR /app

# Cache dependencies first
COPY pom.xml ./
RUN mvn -B -DskipTests dependency:go-offline

# Copy source and build
COPY src ./src
RUN mvn -B -DskipTests package

FROM eclipse-temurin:25-jre
WORKDIR /app

# Copy assembled fat jar from builder stage
COPY --from=builder /app/target/GiveawayBot-4.0-jar-with-dependencies.jar /app/giveawaybot.jar

# Create non-root user, data directories for bind-mounts, and set permissions
RUN useradd -ms /bin/bash appuser \
 && mkdir -p /data/config /data/db \
 && chown -R appuser:appuser /app/giveawaybot.jar /data

# Allow mounting a directory at /data for config and database files
VOLUME ["/data"]
USER appuser

# Hardcode Typesafe Config path to /data/config/application.conf
ENTRYPOINT ["java", "-Dconfig.file=/data/config/application.conf", "-jar", "/app/giveawaybot.jar"]

