# Build stage
FROM gradle:8.5-jdk21-alpine AS builder
WORKDIR /workspace/app

# Копируем ВЕСЬ проект
COPY .. .

# Даем права на выполнение gradlew
RUN chmod +x /workspace/app/gradlew

# Собираем приложение
RUN /workspace/app/gradlew :api-gateway:bootJar --no-daemon --stacktrace

# Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring
USER spring

COPY --from=builder /workspace/app/api-gateway/build/libs/*.jar app.jar

EXPOSE 8083

ENTRYPOINT ["java", "-jar", "app.jar"]