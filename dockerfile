FROM bellsoft/liberica-runtime-container:jdk-21-crac-cds-musl AS builder

WORKDIR /home/app
COPY target/planet-task-0.0.1-SNAPSHOT.jar app.jar

FROM bellsoft/liberica-runtime-container:jdk-21-cds-slim-musl AS optimizer

WORKDIR /app
COPY --from=builder /home/app/app.jar planet-task.jar
RUN java -Djarmode=tools -jar planet-task.jar extract --layers --launcher

FROM bellsoft/liberica-runtime-container:jdk-21-cds-slim-musl

WORKDIR /app
EXPOSE 8080

# Only non-sensitive configuration for build time
ENV SPRING_PROFILES_ACTIVE=docker

# Copy application layers
COPY --from=optimizer /app/planet-task/dependencies/ ./
COPY --from=optimizer /app/planet-task/spring-boot-loader/ ./
COPY --from=optimizer /app/planet-task/snapshot-dependencies/ ./
COPY --from=optimizer /app/planet-task/application/ ./

# Generate CDS archive using mock configuration for the build process
# This avoids storing sensitive info in image layers while still generating the archive
RUN { \
      java -Xshare:dump -Dspring.aot.enabled=true -Dspring.datasource.url=${JDBC_DATABASE_URL} \
           -Dspring.datasource.username=${JDBC_DATABASE_USERNAME} \
           -Dspring.datasource.password=${JDBC_DATABASE_PASSWORD} \
           -Dspring.cache.host=${SPRING_REDIS_HOST} \
           -Dspring.cache.port=${SPRING_REDIS_PORT} \
           -Dspring.profiles.active=${SPRING_PROFILES_ACTIVE} \
           -Dspring.config.location=optional:classpath:/application.yml \
           -XX:ArchiveClassesAtExit=./application.jsa \
           -Dspring.context.exit=onRefresh \
           -Dspring.main.cloud-platform=none \
           org.springframework.boot.loader.launch.JarLauncher || touch ./application.jsa.failed; \
    }

# Create startup script that allows for passing credentials at runtime instead of build time
COPY --chmod=755 <<'EOT' /app/start.sh
#!/bin/sh
# Check if CDS archive exists and is valid
if [ -f ./application.jsa ] && [ ! -f ./application.jsa.failed ]; then
  exec java -Xshare:on -XX:SharedArchiveFile=./application.jsa \
    -Dspring.profiles.active=${SPRING_PROFILES_ACTIVE:-docker} \
    "$@" \
    org.springframework.boot.loader.launch.JarLauncher
else
  exec java \
    -Dspring.profiles.active=${SPRING_PROFILES_ACTIVE:-docker} \
    "$@" \
    org.springframework.boot.loader.launch.JarLauncher
fi
EOT

# Use entrypoint script - credentials are passed at runtime, not stored in image
ENTRYPOINT ["/app/start.sh"]

# Default command if no additional arguments provided
CMD []