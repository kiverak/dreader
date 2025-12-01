# Stage 1: build
FROM gradle:9.2.1-jdk25-corretto AS builder
WORKDIR /workspace/app

# Копируем только необходимые файлы для ускорения сборки
COPY build.gradle settings.gradle ./
COPY gradle ./gradle
COPY src ./src

# Собираем jar (bootJar)
RUN gradle clean bootJar --no-daemon

# Stage 2: runtime
FROM eclipse-temurin:25.0.1_8-jre-ubi10-minimal
WORKDIR /app

# Копируем собранный jar из билд-стадии
COPY --from=builder /workspace/app/build/libs/*.jar app.jar

EXPOSE 8765

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
