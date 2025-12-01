### Сборка Docker-образа
docker build -t dreader-discovery-server .

### для запуска в application.yml поменять для configserver - localhost на config-server
### Запуск контейнера, проброс порта и монтирование basedir
docker run --rm \
--name discovery-service \
--network dreader-net \
-e SPRING_CLOUD_CONFIG_URI=http://config-service:8888 \
-p 8761:8761 \
dreader-discovery-server

### Проверка работоспособности и health
curl http://localhost:8761/actuator/health

#### получить конфиги для приложения prop-test, профиль default, label main
curl http://localhost:8761/prop-dreader/default/main

#### или для generic application
curl http://localhost:8761/application/default
