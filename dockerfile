FROM openjdk:21-ea-1-jdk-slim
VOLUME /tmp
COPY target/planet-task-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]