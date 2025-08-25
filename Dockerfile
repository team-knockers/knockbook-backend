# ---- build stage ----
FROM openjdk:21-jdk-bookworm AS build
WORKDIR /workspace

# Gradle cache optimization
COPY gradlew ./
COPY gradle ./gradle
RUN chmod +x gradlew
COPY build.gradle settings.gradle ./
RUN ./gradlew --no-daemon dependencies || true

# Build
COPY src ./src
RUN ./gradlew clean bootJar -x test --no-daemon

# ---- runtime stage (distroless: nonroot) ----
FROM gcr.io/distroless/java21-debian12:nonroot
WORKDIR /app
COPY --from=build /workspace/build/libs/*.jar /app/app.jar
EXPOSE 8080
ENV JAVA_TOOL_OPTIONS="-Dserver.port=8080"
ENTRYPOINT ["java","-jar","/app/app.jar"]
