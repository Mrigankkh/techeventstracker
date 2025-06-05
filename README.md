# Tech Events Tracker 

Track upcoming tech conferences & meet‑ups and get timely e‑mail reminders.

* **Back‑end:** Spring Boot 3 • Kafka • RabbitMQ • PostgreSQL
* **Front‑end:** React 18 • Vite • Tailwind CSS
* **Infra:** Docker Compose (local) • GitHub Actions (CI)

---

## Table of Contents

1. [High‑level Overview](#high-level-overview)
2. [Architecture](#architecture)
3. [Quick Start](#quick-start)
4. [Configuration](#configuration)
5. [Project Structure](#project-structure)
6. [API Reference](#api-reference)
7. [Running Tests](#running-tests)
8. [Docker Compose](#docker-compose)
9. [CI / CD](#cicd)
10. [Contributing](#contributing)
11. [License](#license)

---

## High‑level Overview

The service ingests raw **tech‑event JSON** payloads from **Kafka**, stores them in **PostgreSQL** via Spring Data JPA, then emits **RabbitMQ** messages that downstream workers (e‑mail, search‑index, etc.) can consume.
Every evening at **20:00 server time** a scheduled job publishes tomorrow’s event summary to the *daily‑summary* queue.
A lightweight React SPA surfaces the events list and lets users subscribe with one click.

---

## Architecture

```text
                               ┌─────────────────────────────┐
                               │        Kafka Topic          │
                               │     `events‑ingest` (JSON)  │
                               └────────────┬───────────────┘
                                            │
                                            ▼
 ┌──────────────────────────────────────────────────────────────────────────┐
 │                         Spring Boot Service                             │
 │ ──────────────────────────────────────────────────────────────────────── │
 │ • @KafkaListener → validate & persist (JPA)                              │
 │ • REST  →  GET /events,  POST /subscribe                                 │
 │ • @Scheduled 20:00 → build next‑day summary → RabbitMQ                   │
 └────────────┬─────────────────────────────────────────────────────────────┘
              │ JDBC
              ▼
          PostgreSQL 15
              │
              ▼
        RabbitMQ exchange
              │
              ▼
    Worker(s): e‑mail dispatcher, search indexer, etc.

                                   ▲
                                   │ HTTPS
                       React + Vite front‑end SPA
```

---

## Quick Start

### 1. Clone & bootstrap

```bash
git clone https://github.com/Mrigankkh/techeventstracker.git
cd techeventstracker
```

### 2. Start local infrastructure

(creates Postgres, Kafka & RabbitMQ)

```bash
docker compose -f docker/docker-compose.yml up -d
```

### 3. Run the back‑end

```bash
./mvnw spring-boot:run            # http://localhost:8080
```

### 4. Run the front‑end

```bash
cd frontend
npm install
npm run dev                       # http://localhost:5173
```

---

## Configuration

| Name                       | Purpose                   | Default                                       | Where to set                |
| -------------------------- | ------------------------- | --------------------------------------------- | --------------------------- |
| `SPRING_DATASOURCE_URL`    | Postgres JDBC URL         | `jdbc:postgresql://localhost:5432/techevents` | shell / `.env`              |
| `SPRING_RABBITMQ_HOST`     | RabbitMQ host             | `localhost`                                   | `.env`                      |
| `KAFKA_BOOTSTRAP_SERVERS`  | Kafka brokers             | `localhost:9092`                              | `.env`                      |
| `VITE_API_BASE` (frontend) | Base URL for API requests | `http://localhost:8080`                       | `frontend/.env.development` |

> **Tip:** the repo ignores `*.env*` files—feel free to commit sample templates like `.env.example`.

---

## Project Structure

```text
.
├── docker/                  # infra YAMLs
├── src/main/java/com/techevents
│   ├── config/              # Kafka & Rabbit configs
│   ├── controller/          # REST endpoints
│   ├── consumer/            # Kafka listener
│   ├── jobs/                # Daily scheduler
│   ├── model/               # JPA entities
│   ├── repository/          # Spring Data interfaces
│   └── service/             # business logic
├── src/test/                # unit + integration tests
├── frontend/                # Vite React app
└── README.md
```

---

## API Reference

### `GET /events`

Returns array of upcoming events.

```jsonc
[
  {
    "id": 42,
    "title": "ReactConf NYC",
    "eventDate": "2025-07-14",
    "city": "New York",
    "tags": ["react", "frontend"]
  }
]
```

### `POST /subscribe`

Create / update a subscriber.

```http
POST /subscribe
Content-Type: application/json

{ "email": "alice@example.com" }
```

*Responses*

| Code | Meaning     |
| ---- | ----------- |
| 201  | Created     |
| 400  | Bad Request |

---

## Running Tests

Back‑end: runs with Embedded Kafka, Testcontainers Rabbit & H2.

```bash
./mvnw verify
```

Front‑end: uses Vitest + React Testing Library.

```bash
cd frontend
npm test
```

Coverage reports land in `target/site/jacoco` (back‑end) and `frontend/coverage/` (front‑end).

---

## Docker Compose

```bash
docker compose -f docker/docker-compose.yml up -d   # infra only
docker compose -f docker/compose.full.yml up -d     # infra + built images (optional)
```

*`compose.full.yml`* can build a production‑grade stack: Spring Boot `:8080`, Vite static assets via nginx `:80`, all services on an internal network.

---

## CI / CD <a name="cicd"></a>

* **GitHub Actions**

  * `mvn verify` on Java 17
  * `npm ci && npm run build` on Node 20
  * Build & push Docker images to GHCR
  * Deploy step: add Render, AWS ECS, Kubernetes, etc.

---


## Contributing

1. Fork & create a feature branch.
2. Run **all** tests (`./mvnw verify && (cd frontend && npm test)`).
3. Open a pull request describing *why* the change is valuable.
4. Ensure no advertisements or tracking scripts are introduced.

---

## License

[MIT](LICENSE)

Built by [Mrigank](https://www.mrigank.me)
