# MindLex — образ для стенда проверки доставки журналов.
#
# Отличие от backend/Dockerfile: сборка выполняется на хосте (./mvnw -o -DskipTests package),
# а в образ копируется уже готовый jar. Так сделано потому, что стенд проверки запускается
# там, где нет доступа к Maven Central: стадия сборки внутри Docker не смогла бы скачать
# зависимости. Для обычной работы используйте backend/Dockerfile.
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app
RUN addgroup -S mindlex && adduser -S mindlex -G mindlex

COPY mindlex-backend.jar /app/app.jar
RUN chown -R mindlex:mindlex /app

# Каталог для файла журнала, который читает Fluent Bit.
# Он создаётся в образе, потому что при первом монтировании пустого тома Docker
# копирует в него права одноимённого каталога образа, а не создаёт том от root.
# Права 1777 разрешают запись непривилегированному пользователю приложения.
RUN mkdir -p /var/log/mindlex && chmod 1777 /var/log/mindlex

USER mindlex

EXPOSE 8080

# docker run ... sh -c "java ... | tee /var/log/mindlex/api.log" позволяет получить файл
# логов контейнера так же, как его видит kubelet на узле (/var/log/pods/...).
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "/app/app.jar"]
