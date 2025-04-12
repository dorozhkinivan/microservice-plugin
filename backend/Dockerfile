FROM gradle:5.3.0-jdk-alpine AS build
WORKDIR /home/gradle/project
COPY . .
RUN gradle build --no-daemon

FROM openjdk:21-jdk
WORKDIR /app
COPY --from=build /home/gradle/project/backend/build/libs/*.jar app.jar
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]