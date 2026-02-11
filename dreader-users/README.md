# Dreader-Users
### Keycloak
```
docker run -p 127.0.0.1:8180:8080 -e KC_BOOTSTRAP_ADMIN_USERNAME=admin -e KC_BOOTSTRAP_ADMIN_PASSWORD=admin quay.io/keycloak/keycloak:26.4.7 start-dev
```
Для доступа к административной консоли перейдите по ссылке: http://localhost:8180/

---

Browser Login

http://localhost:8765/users/oauth2/authorization/dreader-users

Форма авторизации

http://localhost:8180/realms/dreaderapp-realm/protocol/openid-connect/auth?response_type=code&client_id=dreader-users&scope=openid%20profile%20email&state=HkfZGP7SBPaGb3NGEgN2AoTPd4f0RQAbR2iYfcxN7wc%3D&redirect_uri=http://localhost:8765/login/oauth2/code/dreader-users&nonce=Sn5SL7JWRFAFJTi4UQvpePB57lMRqr24kpntEPVS09A

Авторизация

http://localhost:8765/login/oauth2/code/dreader-users?state=oWCrN62_2T9D3QaQShZucR0yEuLLYNKhq25tPE54u3U%3D&session_state=ace44a18-ed48-25f8-a8ce-a1811f753d41&iss=http%3A%2F%2Flocalhost%3A8180%2Frealms%2Fdreaderapp-realm&code=8c009b17-1114-e33c-cd1f-e80caf3a9cfa.ace44a18-ed48-25f8-a8ce-a1811f753d41.246c3cb1-3a04-47b9-9e8b-2c22eab26218

Logout

http://localhost:8765/users/logout

редирект после логаута

http://localhost:8765/oauth2/authorization/dreader-users

---