# Multi-stage Dockerfile
# Builder stage: build the Spring Boot fat jar using Maven
FROM maven:3.9.4-eclipse-temurin-17 AS builder
WORKDIR /workspace/app

# Copy only Maven files first to leverage Docker cache
COPY pom.xml ./
COPY mvnw ./
COPY .mvn/ .mvn/
RUN mvn -B -version

# Copy sources and build
COPY src src
RUN mvn -B -DskipTests package

# Runtime stage
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Copy jar from builder (assumes artifact ends with .jar)
COPY --from=builder /workspace/app/target/*.jar app.jar

ENV JAVA_OPTS="-Xms256m -Xmx512m"

EXPOSE 8080

ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/app.jar"]


