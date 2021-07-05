# A docker image for building ReLF client

FROM androidsdk/android-28:latest

LABEL org.opencontainers.image.source https://github.com/nexus-lab/relf-client

WORKDIR /app

ENV GRADLE_USER_HOME=/app/.gradle

COPY gradle/wrapper /app/gradle/wrapper
COPY gradlew /app/

VOLUME /app

CMD ./gradlew
