FROM maven:3.9.6-eclipse-temurin-21-jammy AS builder
WORKDIR /app
COPY pom.xml .
RUN mvn -B dependency:go-offline
COPY src ./src
RUN mvn -B package -DskipTests

FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S 666 && adduser -S 666 -G 666

WORKDIR /app

COPY --from=builder /app/target/*.jar ./example-client.jar

RUN chown -R 666:666 /app
USER 666

EXPOSE 8080
ENTRYPOINT ["java", "-Xmx100m", "-jar", "example-client.jar"]
