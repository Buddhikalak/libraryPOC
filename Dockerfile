# Build stage - Define the build environment using Maven and Java 17
FROM maven:3.9-eclipse-temurin-17 AS build
# Set the working directory inside the container
WORKDIR /app

# Copy the Maven project configuration file
COPY pom.xml .
# Copy the source code directory
COPY src ./src

# Build the application, skipping tests for faster build
RUN mvn clean package -DskipTests

# Run stage - Define the runtime environment using minimal JRE image
FROM eclipse-temurin:17-jre-alpine
# Set the working directory for the runtime container
WORKDIR /app
# Copy the built JAR file from the build stage
COPY --from=build /app/target/*.jar app.jar
# Expose the port the application will run on
EXPOSE 8080
# Define the command to run the application
ENTRYPOINT ["java", "-jar", "app.jar"]