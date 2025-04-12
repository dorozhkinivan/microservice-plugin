FROM openjdk:21-jdk

WORKDIR /app

COPY backend/build/libs/fat.jar app.jar

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]