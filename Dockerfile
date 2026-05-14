
FROM openjdk:24-ea-jdk-slim-bullseye


RUN apt-get update && \
    apt-get install -y wget unzip


WORKDIR /opt
RUN wget https://services.gradle.org/distributions/gradle-8.14.3-bin.zip && \
    unzip gradle-8.14.3-bin.zip


ENV GRADLE_HOME=/opt/gradle-8.14.3
ENV PATH=$GRADLE_HOME/bin:$PATH


WORKDIR /app


COPY . .


RUN gradle build -x compileTestJava


ENV JAR_NAME=api-1.0.0.jar
ENV APP_HOME=/app
ENV SPRING_PROFILES_ACTIVE=prod

EXPOSE 8080 5005


ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dspring.profiles.active=$SPRING_PROFILES_ACTIVE -jar $APP_HOME/build/libs/$JAR_NAME"]