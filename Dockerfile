FROM openjdk:latest
COPY . /app
WORKDIR /app
RUN ./gradlew build
CMD ["java", "-jar", "build/libs/com.example.ktor-sample-all.jar"]
EXPOSE 8080