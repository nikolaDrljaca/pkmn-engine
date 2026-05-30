# ---- Build Stage ----
FROM gradle:9.3-jdk21-jammy AS build
WORKDIR /app

# Copy everything and build the fat jar
COPY . .
# Build the server module
RUN gradle :server:buildFatJar --no-daemon

# ---- Run Stage ----
FROM eclipse-temurin:21-jre-jammy AS run
WORKDIR /app

# Copy only the fat jar from the build stage
# Run the jar from the server module
COPY --from=build /app/server/build/libs/*.jar app.jar

# Expose Ktor's default port
EXPOSE 5001

# Run the application
CMD ["java", "-jar", "app.jar"]
