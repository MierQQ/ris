FROM gradle:8-jdk17 AS builder

WORKDIR /
COPY settings.gradle /manager/
COPY build.gradle /manager/
COPY src /manager/src

WORKDIR /manager

RUN gradle build --no-daemon 

FROM openjdk:17-jdk-alpine

ARG NUM
ENV COUNT_OF_WORKERS=$NUM

WORKDIR /manager

COPY --from=builder /manager/build/libs/Manager.jar /manager/app.jar
COPY src/main/resources/application.properties /manager/application.properties

EXPOSE 8080

CMD ["java", "-jar", "app.jar", "--spring.config.location=classpath:/application.properties"]