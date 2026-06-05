# Stage 1: Cache dependencies and build the application
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Step 1: Copy ONLY the pom.xml to download and cache dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Step 2: Copy the actual source code and compile
# (Since dependencies are cached from Step 1, this step is lightning fast)
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Run the application
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]