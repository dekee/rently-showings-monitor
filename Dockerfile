#
# Container image for running the Rently Showings Monitor in Kubernetes.
#
# We use the official Playwright Java base image so Chromium + OS deps are already present.
# (Otherwise Playwright would try to download browsers at runtime, which often fails in clusters.)
#

FROM gradle:8.8-jdk17 AS build
WORKDIR /workspace

# Copy only build files first for better layer caching
COPY build.gradle.kts settings.gradle.kts gradlew gradlew.bat /workspace/
COPY gradle /workspace/gradle

# Copy sources
COPY src /workspace/src

RUN ./gradlew --no-daemon clean bootJar

# Runtime image with Playwright browsers
FROM mcr.microsoft.com/playwright/java:v1.48.0-jammy

WORKDIR /app
COPY --from=build /workspace/build/libs/*.jar /app/app.jar

EXPOSE 8080
ENV JAVA_OPTS=""

ENTRYPOINT ["sh","-lc","java $JAVA_OPTS -jar /app/app.jar"]


