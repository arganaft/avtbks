# Указываем базовый образ
FROM openjdk:17-jdk-alpine

# Указываем рабочую директорию
WORKDIR /app

# Копируем файл jar из target папки в контейнер
COPY target/AvtoBuks-1.0-SNAPSHOT.jar app.jar

# Указываем команду для запуска приложения
ENTRYPOINT ["java", "-jar", "/app.jar"]
