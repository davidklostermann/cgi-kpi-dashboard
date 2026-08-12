# Multi-stage build for Spring Boot backend (Java 21)
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw mvnw
COPY src ./src
# Strip CRLF in case the wrapper was checked out on Windows without .gitattributes.
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw && ./mvnw -q -DskipTests package

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
RUN useradd --system --create-home --uid 10001 appuser
COPY --from=build /app/target/*.jar /app/app.jar
USER appuser
EXPOSE 8080
HEALTHCHECK --interval=10s --timeout=5s --start-period=90s --retries=18 \
  CMD curl -fsS http://localhost:8080/actuator/health || exit 1
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
