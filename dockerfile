# ----------- Stage 1: Build with Maven -----------
  FROM maven:3.9.6-eclipse-temurin-21 AS builder
  WORKDIR /build
  
  # Copy only the pom.xml and download dependencies (cached layer)
  COPY pom.xml .
  RUN mvn dependency:go-offline -B
  
  # Copy the rest of the source files
  COPY src ./src
  
  # Package the application and clean up Maven cache
  RUN mvn clean package -DskipTests && rm -rf ~/.m2/repository
  
  # ----------- Stage 2: Generate CDS Archive -----------
  FROM eclipse-temurin:21-jre AS cds-builder
  WORKDIR /app
  
  # Copy the built JAR from the previous stage
  COPY --from=builder /build/target/planet-task-0.0.1-SNAPSHOT.jar app.jar
  
  # Generate the class list (ignore non-zero exit codes from Spring Boot shutdown)
  RUN java -Xshare:off -XX:DumpLoadedClassList=classes.lst -jar app.jar || true
  
  # Generate the CDS archive
  RUN java -Xshare:dump \
      -XX:SharedClassListFile=classes.lst \
      -XX:SharedArchiveFile=app-cds.jsa \
      -cp app.jar
  
  # ----------- Stage 3: Final runtime image -----------
  # Use Alpine for smaller footprint — switch to regular jre if needed
  FROM eclipse-temurin:21-jre-alpine
  # FROM eclipse-temurin:21-jre
  WORKDIR /app
  VOLUME /tmp
  
  # Copy only required files from CDS stage
  COPY --from=cds-builder /app/app.jar .
  COPY --from=cds-builder /app/app-cds.jsa .
  
  EXPOSE 8080
  
  ENTRYPOINT ["java", "-XX:SharedArchiveFile=app-cds.jsa", "-jar", "app.jar"]  