FROM gradle:4.10-jdk8 as build
COPY --chown=gradle:gradle . /appsrc
WORKDIR /appsrc
ENV GRADLE_OPTS -Dorg.gradle.daemon=false
RUN gradle clean build -x test

FROM openjdk:8u191-jdk-alpine3.9
COPY --from=build /appsrc/build/libs/*.jar /app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
