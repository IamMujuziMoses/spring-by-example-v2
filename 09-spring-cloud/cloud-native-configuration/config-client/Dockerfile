FROM eclipse-temurin:21-jre

WORKDIR /app

COPY target/config-client-1.0-SNAPSHOT-exec.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
