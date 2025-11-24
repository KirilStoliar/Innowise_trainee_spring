# Build stage
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app

# Копируем всё необходимое для сборки
COPY . .

# Даем права на выполнение gradlew
RUN chmod +x gradlew

# Собираем приложение
RUN ./gradlew clean bootJar --no-daemon

# Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Создаем пользователя для безопасности
RUN addgroup -S spring && adduser -S spring -G spring
USER spring

# Копируем JAR из build stage
COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]