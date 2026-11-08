FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
COPY src src
RUN mvn -B -q package -DskipTests

FROM eclipse-temurin:21-jre
RUN useradd -r -u 1001 app && mkdir -p /data/media && chown app /data/media
COPY --from=build /app/target/backend-0.1.0.jar /app.jar
USER app
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app.jar"]
