### Сборка Docker-образа
docker build -t dreader-gateway .

### для запуска в application.yml поменять для configserver - localhost на config-server
### Запуск контейнера, проброс порта и монтирование basedir
docker run --rm \
--name dreader-gateway \
--network dreader-net \
-e SPRING_CLOUD_CONFIG_URI=http://config-service:8888 \
-p 8765:8765 \
dreader-gateway

### Проверка работоспособности и health
curl http://localhost:8765/actuator/health

#### получить конфиги для приложения prop-test, профиль default, label main
curl http://localhost:8765/prop-dreader/default/main

#### или для generic application
curl http://localhost:8765/application/default