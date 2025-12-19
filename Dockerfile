FROM gradle:8.8-jdk17 AS build
WORKDIR /workspace

# Copy only build files first for better layer caching
COPY build.gradle.kts settings.gradle.kts gradlew gradlew.bat /workspace/
COPY gradle /workspace/gradle

# Copy sources
COPY src /workspace/src

RUN ./gradlew --no-daemon clean bootJar

#
# Runtime image
# - Use a slim JRE image (much smaller than the Playwright "all browsers" base image)
# - Install system dependencies required for Chromium
# - Let Playwright download the Chromium browser at runtime (into the mounted /data volume)
#
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app
COPY --from=build /workspace/build/libs/*.jar /app/app.jar

EXPOSE 8080
ENV JAVA_OPTS=""
ENV PLAYWRIGHT_BROWSERS_PATH="/data/ms-playwright"

# Chromium runtime deps for Playwright on Ubuntu Jammy
RUN apt-get update && \
  apt-get install -y --no-install-recommends \
    ca-certificates \
    libnss3 \
    libatk-bridge2.0-0 \
    libatk1.0-0 \
    libatspi2.0-0 \
    libxkbcommon0 \
    libxcomposite1 \
    libxdamage1 \
    libxfixes3 \
    libxrandr2 \
    libxshmfence1 \
    libdrm2 \
    libgbm1 \
    libasound2 \
    libpangocairo-1.0-0 \
    libpango-1.0-0 \
    libcairo2 \
    libglib2.0-0 \
    libgtk-3-0 \
    fonts-liberation \
  && rm -rf /var/lib/apt/lists/*

ENTRYPOINT ["sh","-lc","java $JAVA_OPTS -jar /app/app.jar"]


