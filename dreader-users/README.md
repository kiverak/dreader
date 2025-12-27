# Dreader-Users
### Keycloak
```
docker run -p 127.0.0.1:8180:8080 -e KC_BOOTSTRAP_ADMIN_USERNAME=admin -e KC_BOOTSTRAP_ADMIN_PASSWORD=admin quay.io/keycloak/keycloak:26.4.7 start-dev
```
Для доступа к административной консоли перейдите по ссылке: http://localhost:8180/