FROM openjdk:24-ea-jdk-slim-bullseye

RUN apt-get update && \
    apt-get install -y wget unzip && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /opt
RUN wget https://services.gradle.org/distributions/gradle-9.6.1-bin.zip && \
    unzip gradle-9.6.1-bin.zip && \
    rm gradle-9.6.1-bin.zip

ENV GRADLE_HOME=/opt/gradle-9.6.1
ENV PATH=$GRADLE_HOME/bin:$PATH

WORKDIR /app

COPY . .

# Tests are executed on the host before `docker compose build` (see Makefile).
# Inside the build container there is no PostgreSQL, so skip the test task.
RUN gradle build -x test

ENV JAR_NAME=api-1.0.0.jar
ENV APP_HOME=/app
EXPOSE 8080 5005

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar $APP_HOME/build/libs/$JAR_NAME"]
