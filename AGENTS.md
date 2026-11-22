# Dreader

Distributed news backend: Jsoup scraping → Kafka → articles/posts → Telegram/Threads. Java 25, Spring Boot 4.2.0, Spring Cloud 2026.0.0, Gradle multi-project, no frontend here. `README.md` and `timur.txt` are legacy Cyrillic-encoded notes describing an aspirational React/Go/RabbitMQ/ClickHouse stack — ignore them. Runtime config lives in the external `prop-dreader` repo, not in this checkout.

## Build & tests — read first

- Root `gradlew`/`gradlew.bat` are **broken**: `gradle/wrapper/gradle-wrapper.jar` is missing. Use installed `gradle` 9.x from the repo root. Java 25 needs Gradle 9+; `dreader-news`' checked-in wrapper is 8.14.3 and dies with `Unsupported class file major version 69`. The `dreader-gateway` and `dreader-llm-parser` wrappers are 9.2.1 and work.
- Root build includes everything except `dreader-llm-parser`. Only `dreader-news/settings.gradle` has `includeBuild '../common'`; other per-service `settings.gradle` files cannot resolve `project(":common:...")` — build those from the root.
- `gradle :dreader-news:test --tests "ru.dreader.dreadernews.service.RatingCalculatorTest"` runs one class. Same pattern for `:dreader-users`, `:dreader-news-parser`, `:dreader-gateway`, `:config-service`, `:dreader-discovery-server`; `gradle :<module>:bootRun` to run.
- LLM parser is standalone: from `dreader-llm-parser/`, `./gradlew test` / `./gradlew bootRun` (local `application.yml`, no config server).
- Stale incremental state after switching Gradle versions can emit ~100 bogus `cannot find symbol getX()` errors; `gradle :dreader-news:compileJava --rerun-tasks` clears it.
- JUnit 5; service-level tests are plain Mockito. `*ApplicationTests.contextLoads()` are full `@SpringBootTest` and currently fail in the Boot 4.2 state (other tests pass) — use `--tests` when verifying a change. No CI workflows.

## Modules

| Module | Role |
| --- | --- |
| `common:core` | Shared DTOs (`dto.ArticleDto`, `dto.SourceDetails`, `dto.UserInfo`). Package is `dto`, not `ru.dreader`. |
| `common:mvc` | `ru.dreader.mvc.entity.BaseEntity` / `AuditEntity`, `GlobalExceptionHandler`, `@DebugLog` aspect. |
| `common:utils` | Empty library. |
| `config-service` | Spring Cloud Config on 8888, git backend `kiverak/prop-dreader` (`main`); needs `GITHUB_TOKEN`. |
| `dreader-discovery-server` | Eureka. |
| `dreader-gateway` | Spring Cloud Gateway (WebFlux); routes live in the config repo (`dreader-gateway` profile). |
| `dreader-users` | Keycloak OIDC, registration, profile. DB `dreader_users_data` on 5434. |
| `dreader-news-parser` | Jsoup parsers, hourly job, Kafka producer. DB `dreader_news_parser` on 5435. Package `dreadernewsparser` (no `ru.dreader`). |
| `dreader-news` | Articles, posts, categories, channels, Kafka consumer, Telegram/Threads publishers. DB `dreader_news` on 5433. |

`dreader-llm-parser` is a separate Gradle build: Spring AI + Ollama (`llama3.1:8b`), HTTP on **18080**, `POST /api/parse`, `POST /api/categorize`.

## Configuration & infra

- Service `application.yml` files only set `spring.application.name`, `spring.config.import=optional:configserver:http://localhost:8888`, and profiles (`dreader-micro` + service name; news adds `redis,kafka`; gateway only `dreader-gateway`). Datasources, Kafka, Eureka, Redis, routes and `urls.*` are external: change them in `prop-dreader`, then restart or refresh.
- `docker-compose.yml` starts **infra only**: Kafka KRaft 3 controllers + 3 brokers (host 29092/39092/49092), Redis 6379 (no persistence), Postgres 18 (Keycloak 5432/`dreader_users`, users 5434/`dreader_users_data`, news 5433/`dreader_news`, parser 5435/`dreader_news_parser`), Keycloak 26 `start-dev` on 8180 (realm `dreaderapp-realm`; no realm export in repo), Ollama ROCm on 11434 (pull `llama3.1:8b` per its README).
- Boot order: infra → config-service → discovery → users/news/parser → gateway. LLM parser separately, with Ollama running.
- Flyway scripts in `src/main/resources/db/migration`: news is through **V10** (next V11), parser through **V1** (next V2). Never edit applied migrations.

## Pipeline

1. `ScheduledNewsParser` hourly (`fixedRate`, virtual thread per source) uses a `ParserStrategy` (`HabrParserStrategy`, `ThreeDNewsParserStrategy`) and publishes `ArticleDto` to `${kafka.topics.articlesTopic}`.
2. `ArticleConsumer` (group `dreader-news-group`, MANUAL ack, 3 retries at 2s then DLQ) dedups by URL and saves.
3. `LLMParserClient` calls `${urls.llm-parser}` + `/api/parse|/categorize`; `${urls.gateway}` + `/parser/api` is for parser source CRUD. LLM `/parse` is globally serialized by a 60s `ReentrantLock`.
4. `ScheduledPublisher` every 60s (`fixedDelay`) loops categories → `PublishingService` → `TelegramPublisher` / `ThreadsPublisher`. ShedLock is enabled (default `lockAtMostFor=10m`) but only `OldEntityCleaner` is `@SchedulerLock`-annotated — the publisher is **not**.
5. `ThreadsAuthRefresher` refreshes Threads tokens daily.

## Conventions

- Layers: `controller` → `service` → `repo`, plus `entity`, `dto`, `mapper`, `job`, `config`, `publisher`, `web`, `parser.strategy`. New site support = new `ParserStrategy`, not a scheduler change.
- Lombok (`@RequiredArgsConstructor`, `@Log4j2` / `@Slf4j`), constructor injection, no field `@Autowired`. JPA entities extend `BaseEntity` (`Long id`, `@Version`, `Instant createdAt`/`updatedAt` set by lifecycle callbacks); `AuditEntity` adds UUID `createdBy`/`updatedBy`.
- Security: `/api/public/**` + Swagger open, `/api/user/**` → `ROLE_USER`, `/api/admin/**` → `ROLE_ADMIN`, users `/api/internal/**` → `ROLE_INTERNAL_SERVICE`. Roles come from Keycloak `realm_access.roles`, filtered to values already prefixed `ROLE_`. Stateless JWT on `/api/**`; users also has browser `oauth2Login`.
- Service-to-service HTTP uses `WebClient` with blocking `.block()` at the edge.
- Keep shared parser/news contracts in `common:core` and update producer and consumer together. Do not duplicate those DTOs in a service.
- Secrets stay out of git: Threads ids are `{THREADS_APP_ID}` / `{THREADS_APP_SECRET}` placeholders in yml; the config-server token is env-only. Publishing and token code is rate-limit sensitive (`TelegramRateLimitedException`, `ThreadsRateLimitedException`) — preserve retry/backoff.
- Do not add a frontend, Go service, or RabbitMQ client unless asked.
