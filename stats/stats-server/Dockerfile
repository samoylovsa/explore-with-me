# Стадия сборки
FROM maven:3.8.6-openjdk-21 AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Стадия запуска
FROM eclipse-temurin:21-jre-jammy
VOLUME /tmp
COPY --from=builder /app/target/*.jar app.jar
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar /app.jar"]