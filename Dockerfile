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

ENV JAVA_TOOL_OPTIONS="-Dserver.port=8080" \
    SPRING_PROFILES_ACTIVE=prod \
    spring.mail.host=smtp.postmarkapp.com \
    spring.mail.port=587 \
    spring.mail.properties.mail.smtp.auth=true \
    spring.mail.properties.mail.smtp.starttls.enable=true \
    spring.mail.default-encoding=UTF-8

ARG POSTMARK_SERVER_TOKEN
ENV spring.mail.username=${POSTMARK_SERVER_TOKEN} \
    spring.mail.password=${POSTMARK_SERVER_TOKEN}

ARG IMGBB_API_KEY
ENV IMGBB_API_KEY=${IMGBB_API_KEY} \
    IMGBB_API_BASE_URL=https://api.imgbb.com \
    IMGBB_API_UPLOAD_PATH=/1/upload \
    IMGBB_API_TIMEOUT_SECONDS=30 \
    IMGBB_API_CONNECT_TIMEOUT_MILLIS=5000

ENTRYPOINT ["java","-jar","/app/app.jar"]
