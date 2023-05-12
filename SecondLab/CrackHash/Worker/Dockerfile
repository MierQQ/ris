FROM gradle:8-jdk17 AS builder

WORKDIR /
COPY settings.gradle /worker/
COPY build.gradle /worker/
COPY src /worker/src

WORKDIR /worker

RUN gradle build --no-daemon 

FROM openjdk:17-jdk-alpine

WORKDIR /worker

COPY --from=builder /worker/build/libs/Worker.jar /worker/app.jar
COPY src/main/resources/application.properties /worker/application.properties

EXPOSE 8081

CMD ["java", "-jar", "app.jar", "--spring.config.location=classpath:/application.properties"]