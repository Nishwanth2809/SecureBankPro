# ── Stage 1: BUILD ──────────────────────────────────────────────────────────
# Use Maven + JDK to compile and package the application into a fat JAR.
# This stage is discarded after build — it never ends up in the final image.
FROM eclipse-temurin:17-jdk-alpine AS build

WORKDIR /app

# Copy Maven wrapper and pom.xml first (Docker layer cache: dependencies won't
# re-download unless pom.xml changes)
COPY .maven/ .maven/
COPY pom.xml .
RUN .maven/apache-maven-3.9.6/bin/mvn dependency:go-offline -B

# Copy source code and build, skipping tests (tests run in CI)
COPY src/ src/
RUN .maven/apache-maven-3.9.6/bin/mvn clean package -DskipTests -B

# ── Stage 2: RUNTIME ─────────────────────────────────────────────────────────
# Lean JRE-only image — much smaller than the JDK build image.
FROM eclipse-temurin:17-jre-alpine AS runtime

# Add a non-root user for security (never run as root in production)
RUN addgroup -S securebank && adduser -S securebank -G securebank

WORKDIR /app

# Copy only the packaged JAR from the build stage
COPY --from=build /app/target/*.jar app.jar

# Set ownership
RUN chown securebank:securebank app.jar

USER securebank

# Expose the port Spring Boot listens on
EXPOSE 8080

# Health check — Docker/Railway will restart the container if it fails
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD wget -qO- http://localhost:8080/v3/api-docs || exit 1

# JVM tuning for containers:
# -XX:+UseContainerSupport — respects cgroup memory limits
# -XX:MaxRAMPercentage=75  — use 75% of available container RAM for heap
# -Djava.security.egd     — faster SecureRandom (important for JWT generation)
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
