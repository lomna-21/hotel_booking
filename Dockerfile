# Stage 1: Build the application
FROM maven:3.8.6-openjdk-8-slim AS build
WORKDIR /app

# Copy the pom.xml and download dependencies (cached layer)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy the source code and build the application
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Create the runtime image
FROM eclipse-temurin:8-jre
WORKDIR /app

# Copy the built jar file from the build stage
COPY --from=build /app/target/hotelbooking-0.0.1-SNAPSHOT.jar app.jar

# Expose the port (Render uses dynamic PORT mapping, but we define 8080 as default)
EXPOSE 8080

# Run the jar with active prod profile and dynamic port matching
ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-Dserver.port=${PORT:8080}", "-jar", "app.jar"]
