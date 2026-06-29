# Student Service Dockerfile — Java multi-stage build
#
# Stage 1 (build): Maven compiles the source and packages the JAR.
# Stage 2 (runtime): only the JRE is included — image is ~200MB instead of ~500MB.

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

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENV STUDENT_DB_URL=jdbc:mysql://student-mysql:3306/studentdb?createDatabaseIfNotExist=true&serverTimezone=UTC
ENV STUDENT_DB_USER=root
ENV STUDENT_DB_PASSWORD=root
ENV FINANCE_SERVICE_URL=http://host.docker.internal:8082
ENV LIBRARY_SERVICE_URL=http://host.docker.internal:8083
ENV FINANCE_PORTAL_URL=http://localhost:8082
ENV LIBRARY_PORTAL_URL=http://localhost:8083

ENTRYPOINT ["java", "-jar", "app.jar"]
