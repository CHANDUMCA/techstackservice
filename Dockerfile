# Use a lightweight JRE 21 image
FROM eclipse-temurin:21-jre-alpine

# Set the working directory inside the container
WORKDIR /app

# Copy the built WAR file from the target directory into the container
COPY target/techstackservice-0.0.1.war app.war

# Expose the application port
EXPOSE 8080

# Run the Spring Boot application
ENTRYPOINT ["java", "-jar", "app.war"]
