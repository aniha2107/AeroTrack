# Stage 1: build the app (Maven runs here, on the build server - not on your computer)
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -B -DskipTests package

# Stage 2: run it
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/lost-baggage-tracker-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-Xmx350m", "-jar", "app.jar"]
