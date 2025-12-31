# Use a base image with Java and Gradle
FROM gradle:8.5-jdk21-jammy AS build

# Set the working directory inside the container
WORKDIR /app

# Copy the Gradle wrapper and build filesCOPY gradlew .
COPY gradle ./gradle
COPY build.gradle.kts .
COPY settings.gradle.kts .

# Copy the source code
COPY src ./src

# Build the application and create the "fat" JAR
RUN ./gradlew build -x test

# --- Second Stage: Create the final, smaller image ---
# THIS IS THE CORRECTED LINE:
FROM eclipse-temurin:21-jre-alpine

# Set the working directory
WORKDIR /app

# Copy only the built JAR file from the previous stage
COPY --from=build /app/build/libs/*-all.jar ./app.jar

# Expose the port the server will run on
EXPOSE 8080

# The command to run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
