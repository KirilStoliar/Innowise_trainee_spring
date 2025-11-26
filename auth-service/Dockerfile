# Build stage
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /workspace

# Копируем ВЕСЬ проект из корня (включая gradlew)
COPY . .

# Даем права на выполнение gradlew (он теперь в /workspace)
RUN chmod +x gradlew

# Собираем приложение (указываем конкретный модуль)
RUN ./gradlew :auth-service:bootJar --no-daemon

# Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Создаем пользователя для безопасности
RUN addgroup -S spring && adduser -S spring -G spring
USER spring

# Копируем JAR из build stage
COPY --from=builder /workspace/auth-service/build/libs/*.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]