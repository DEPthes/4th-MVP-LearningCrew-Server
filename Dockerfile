FROM eclipse-temurin:21-jdk-alpine
EXPOSE 8080

ENV PROFILE=remote

COPY app.jar learnit-back.jar

ENTRYPOINT ["java", "-jar", "/learnit-back.jar", "--spring.profiles.active=${PROFILE}"]