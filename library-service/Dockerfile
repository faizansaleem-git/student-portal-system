# Library Service Dockerfile — Java multi-stage build
#
# Stage 1 (build): Maven compiles the source and packages the JAR.
# Stage 2 (runtime): only the JRE is included — image is ~200MB instead of ~500MB.
# This mirrors best practice for Java microservices in production.

# ── Stage 1: Build ────────────────────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

# Copy pom.xml first so Maven dependency layer is cached independently of source changes
COPY pom.xml .
RUN mvn dependency:go-offline -q

# Copy source and build, skipping tests (tests run in CI, not in Docker build)
COPY src ./src
RUN mvn package -DskipTests -q

# ── Stage 2: Runtime ──────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# Copy only the packaged JAR from the build stage
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8083

# Connection strings and URLs supplied via environment variables (never hardcoded)
ENV LIBRARY_DB_URL=jdbc:mysql://library-mysql:3306/librarydb?createDatabaseIfNotExist=true&serverTimezone=UTC
ENV LIBRARY_DB_USER=root
ENV LIBRARY_DB_PASSWORD=root
ENV FINANCE_SERVICE_URL=http://finance-service:8082

ENTRYPOINT ["java", "-jar", "app.jar"]
