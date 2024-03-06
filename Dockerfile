FROM gradle:8.5.0-jdk21-jammy AS build

COPY --chown=gradle:gradle . /home/gradle/src

WORKDIR /home/gradle/src

RUN gradle build --no-daemon --stacktrace --build-cache --gradle-user-home cache/.gradle

FROM openjdk:21-slim

ARG VERSION='0.1'

EXPOSE 8080

RUN mkdir /app

COPY --from=build /home/gradle/src/api/build/libs/api-${VERSION}.jar /app/solver-engine.jar

ENTRYPOINT ["java", "-jar", "/app/solver-engine.jar"]