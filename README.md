# Банковская Платформа (Monolith)

Spring Boot-приложение для управления банковскими картами, пользователями и переводами между счетами.

---

## 📚 Содержание

* [🚀 Возможности](#-возможности)
* [🛠 Технологический стек](#-технологический-стек)
* [📋 Предварительные требования](#-предварительные-требования)
* [⚡ Быстрый запуск](#-быстрый-запуск)
* [🔧 Конфигурация](#-конфигурация)
* [📖 API-документация](#-api-документация)
* [🧪 Тестирование API (гайд)](#-тестирование-api-полное-руководство)
* [🧪 Запуск тестов](#-запуск-тестов)
* [📦 Dockerization (опционально)](#-dockerization-опционально)
* [🆘 Получение помощи](#-получение-помощи)
* [🐛 Известные проблемы](#-известные-проблемы-и-решения)

---

## 🚀 Возможности

* **JWT-аутентификация и авторизация**

    * Регистрация и вход для пользователей.
    * Ролевая модель: `USER` и `ADMIN`.
    * Обновление access-токена по refresh-токену.
* **Управление картами**

    * `ADMIN`: полный CRUD (создание, блокировка/активация, «удаление» через статус), пополнение баланса.
    * `USER`: просмотр своих карт, запрос на блокировку, пополнение своих карт.
    * PAN шифруется в БД и маскируется в ответах API.
    * Фильтрация и пагинация списков.
* **Переводы между картами**

    * `USER`: переводы между собственными картами.
    * Идемпотентность через заголовок `Idempotency-Key`.
* **Просмотр транзакций**

    * История операций по карте.
* **Администрирование**

    * `ADMIN`: просмотр и управление статусом пользователей.

---

## 🛠 Технологический стек

* Java 17
* Spring Boot 3.x
* Spring Security (JWT)
* Spring Data JPA
* PostgreSQL
* Liquibase (миграции)
* Docker & Docker Compose (БД)
* Testcontainers (интеграционные тесты)

---

## 📋 Предварительные требования

1. **Java 17+** (JDK)
2. **Maven 3.6+**
3. **Docker & Docker Compose**
4. **Postman** или `curl` для тестирования API

---

## ⚡ Быстрый запуск

1. **Клонирование и сборка**

```bash
git clone <your-repo-url>
cd bankingplatfrommonolit
mvn clean package
```

2. **Запуск БД**

```bash
docker-compose up -d
```

> PostgreSQL поднимется в контейнере. По умолчанию доступен на **localhost:15432** (`bankdb`).

3. **Запуск приложения**

```bash
java -jar target/bankingplatfrommonolit-0.0.1-SNAPSHOT.jar
```

> Приложение подключается к БД по `localhost:15432` (см. `application.yml`).

4. **Проверка здоровья**

```bash
curl http://localhost:8080/actuator/health
```

Ожидается:

```json
{"status":"UP"}
```

---

## 🔧 Конфигурация

Основной файл: `src/main/resources/application.yml`.

### Важные переменные окружения (для запуска на хосте, БД в Docker)

| Переменная                   | Значение по умолчанию                      | Описание                                   |
| ---------------------------- | ------------------------------------------ | ------------------------------------------ |
| `SPRING_DATASOURCE_URL`      | `jdbc:postgresql://localhost:15432/bankdb` | JDBC-URL БД                                |
| `SPRING_DATASOURCE_USERNAME` | `postgres`                                 | Пользователь БД (см. `docker-compose.yml`) |
| `SPRING_DATASOURCE_PASSWORD` | `password`                                 | Пароль БД (см. `docker-compose.yml`)       |

### JWT-ключи

Приложение ищет ключи по путям:

* Приватный: `src/main/resources/keys/jwt-private.pem`
* Публичный: `src/main/resources/keys/jwt-public.pem`

> Для `application-test.yml` используются те же ключи, что и для разработки (если не указано иначе).

---

## 📖 API-документация

После старта доступно:

* Swagger UI: `http://localhost:8080/swagger-ui.html`
  (в некоторых версиях — `http://localhost:8080/swagger-ui/index.html`)

---

## 🧪 Тестирование API (полное руководство)

Убедитесь, что запущены приложение и БД.

### 1) Окружение (Postman)

Создайте Environment:

* `baseUrl = http://localhost:8080`
* `adminToken` — пусто (заполнится)
* `userToken` — пусто (заполнится)
* `userId` — пусто (заполнится)
* `cardId1` / `cardId2` — пусто (заполнится)

Во всех запросах используйте `{{baseUrl}}`.

### 2) Создание администратора

При первом запуске администратор создаётся автоматически (через `AdminSeeder`).
Учетные данные:

* Логин: `admin`
* Пароль: `admin`
* Email: `admin@example.com`

### 3) Сценарии

#### 3.1. Токен администратора

**POST** `{{baseUrl}}/auth/login`

```json
{
  "login": "admin",
  "password": "admin"
}
```

Сохраните `accessToken` как `adminToken`.

#### 3.2. Регистрация и вход пользователя

**POST** `{{baseUrl}}/auth/register`

```json
{
  "username": "testuser",
  "email": "testuser@example.com",
  "password": "securepassword123"
}
```

Сохраните `accessToken` в `userToken`, `userId` — из ответа или JWT.

Альтернатива — вход:
**POST** `{{baseUrl}}/auth/login`

```json
{
  "login": "testuser",
  "password": "securepassword123"
}
```

#### 3.3. Работа с картами (ADMIN)

**Создание карты пользователю**
**POST** `{{baseUrl}}/admin/cards`
Header: `Authorization: Bearer {{adminToken}}`

```json
{
  "ownerId": "{{userId}}",
  "pan": "4111111111111111",
  "expiry": "2026-12-31"
}
```

Сохраните `id` → `cardId1`.

**Вторая карта** — повторите запрос с другим PAN (например, `4012888888881881`), сохраните `cardId2`.

**Пополнение баланса**
**POST** `{{baseUrl}}/admin/cards/{{cardId1}}/topup?amount=100.00`
Header: `Authorization: Bearer {{adminToken}}`

**Просмотр всех карт**
**GET** `{{baseUrl}}/admin/cards?size=100`
Header: `Authorization: Bearer {{adminToken}}`

#### 3.4. Работа с картами (USER)

**Пополнение своей карты**
**POST** `{{baseUrl}}/cards/{{cardId2}}/topup?amount=50.00`
Header: `Authorization: Bearer {{userToken}}`

**Список своих карт**
**GET** `{{baseUrl}}/cards?page=0&size=10`
Header: `Authorization: Bearer {{userToken}}`

**Фильтрация карт**
**GET** `{{baseUrl}}/cards/filter?minBalance=300&size=100`
Header: `Authorization: Bearer {{userToken}}`

**Запрос на блокировку**
**POST** `{{baseUrl}}/cards/{{cardId1}}/block-request`
Header: `Authorization: Bearer {{userToken}}`

#### 3.5. Переводы между картами

**Инициировать перевод**
**POST** `{{baseUrl}}/transactions/transfer`
Headers:

* `Authorization: Bearer {{userToken}}`
* `Idempotency-Key: {{$timestamp}}`
* `Content-Type: application/json`

```json
{
  "fromCardId": "{{cardId1}}",
  "toCardId": "{{cardId2}}",
  "amount": 15.50
}
```

**Проверка идемпотентности**
Повторите тот же запрос с тем же `Idempotency-Key`.
Ожидается тот же ответ, балансы не меняются повторно.

#### 3.6. Транзакции

**GET** `{{baseUrl}}/transactions/card/{{cardId1}}?page=0&size=10`
Header: `Authorization: Bearer {{userToken}}`

#### 3.7. Управление токенами

**Обновление access-токена**
**POST** `{{baseUrl}}/auth/refresh`

```json
{
  "refreshToken": "<your_refresh_token>"
}
```

**Выход со всех устройств**
**POST** `{{baseUrl}}/auth/logout-all`
Header: `Authorization: Bearer {{userToken}}`

### 4) Тестирование ошибок

* **Недостаточно средств** — выполните перевод с суммой больше баланса.
* **Идемпотентность (409)** — два разных запроса с одним и тем же `Idempotency-Key: test-key`.
* **Доступ к чужой карте** — попробуйте операции с картой, не принадлежащей пользователю.

---

## 🧪 Запуск тестов

Интеграционные тесты (через Testcontainers PostgreSQL):

```bash
mvn test -Dspring.profiles.active=test
```

---

## 📦 Dockerization (опционально)

Можно добавить `Dockerfile` для сборки приложения в образ и расширить `docker-compose.yml`, чтобы запускать **и** БД, **и** приложение одной командой.

---

## 🆘 Получение помощи

1. Проверьте переменные окружения и пути к ключам.
2. Убедитесь, что PostgreSQL запущен и доступен.
3. Просмотрите логи (`stdout`/`logs/application.log`).
4. Убедитесь, что используете корректный токен (admin vs user).

При необходимости обратитесь к Swagger UI либо создайте Issue в репозитории.

---

## 🐛 Известные проблемы и решения

1. **Данные не сохраняются между перезапусками**
   Проверьте, что в `docker-compose.yml` отсутствует сервис `db-reset`, который очищает БД при старте.

2. **Ошибки валидации PAN**
   Используйте валидные номера, проходящие алгоритм Луна (напр., `4111111111111111`, `4012888888881881`).
   Также корректные примеры имеются в тестовом классе src/test/java/com/example/bankingplatfrommonolit/Integration/TransferFlowIT.java
3. **Ошибки доступа**
   Админские операции требуют `adminToken`; пользовательские — `userToken`.

---
