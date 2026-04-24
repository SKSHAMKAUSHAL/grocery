FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY target/user-service-0.0.1-SNAPSHOT.jar app.jar
COPY src/main/resources/firebase-service-account.json /app/firebase-service-account.json
ENTRYPOINT ["java", "-jar", "app.jar"]