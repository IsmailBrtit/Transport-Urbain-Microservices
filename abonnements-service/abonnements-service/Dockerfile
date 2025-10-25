FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copy the pre-built JAR file
COPY build/libs/abonnements-service-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
