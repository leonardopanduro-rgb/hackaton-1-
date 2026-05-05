# TropelCare Signal Engine

Backend Spring Boot para la hackathon DBP. No incluye frontend: todo el flujo se valida con Postman o Bruno.

## Integrantes

- Nombre completo - Codigo UTEC
- Nombre completo - Codigo UTEC
- Nombre completo - Codigo UTEC

## Stack

- Java 21+
- Spring Boot 3.x
- Spring Web
- Spring Data JPA
- PostgreSQL
- Spring Boot Mail
- `@Async` + `@TransactionalEventListener(phase = AFTER_COMMIT)`
- GitHub Models API
- JUnit 5 + Mockito

## Levantar PostgreSQL

```bash
docker run --name tropelcare-db \
  -e POSTGRES_DB=tropelcare \
  -e POSTGRES_USER=tropeluser \
  -e POSTGRES_PASSWORD=tropelpass \
  -p 5432:5432 \
  -d postgres:16
```

Si el contenedor ya existe:

```bash
docker start tropelcare-db
```

## Variables de entorno

Copia `.env.example` como `.env` en la raiz del proyecto y reemplaza los valores reales:

```properties
DB_HOST=localhost
DB_PORT=5432
DB_NAME=tropelcare
DB_USERNAME=tropeluser
DB_PASSWORD=tropelpass

GITHUB_TOKEN=<personal_access_token_con_permiso_models_read>
GITHUB_MODELS_URL=https://models.inference.ai.azure.com
MODEL_ID=gpt-4o-mini

MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=<email_del_equipo@gmail.com>
MAIL_PASSWORD=<app_password_de_16_caracteres>

ADMIN_NAME=Cameron Walker
ADMIN_EMAIL=cameron@tuckersoft.com
ADMIN_NOTIFICATION_EMAIL=<email_real_del_equipo@gmail.com>
```

El token de GitHub debe ser un fine-grained PAT con `Account permissions -> Models: Read-only`. Si el token no tiene ese permiso, GitHub Models devuelve `401` y la app activa el fallback obligatorio (`SENAL_CORRUPTA`, `status=ERROR`, sin evento asíncrono).

El archivo `.env` esta en `.gitignore`; no lo subas al repositorio.

## Ejecutar

Con Maven Wrapper:

```bash
./mvnw spring-boot:run
./mvnw test
```

En Windows tambien puedes usar:

```bash
mvnw.cmd spring-boot:run
mvnw.cmd test
```

## Arquitectura por capas

- `models`: entidades JPA y enums.
- `repositories`: interfaces Spring Data JPA.
- `dtos`: requests, responses y wrapper paginado.
- `controllers`: endpoints REST.
- `services`: reglas de negocio, filtros, fallback de IA y actualizacion de stats.
- `clients`: cliente HTTP para GitHub Models y parser de clasificacion.
- `events`: evento y listener asincrono para email.
- `exceptions`: excepciones de negocio y `GlobalExceptionHandler`.
- `config`: async executor y `DataInitializer`.

## Flujo asincrono de senales

`POST /api/v1/signals` entra a `SignalService`, valida Tropel/Guardian, llama a GitHub Models, actualiza stats, guarda `TropelSignal`, crea `CareResponse` y publica `TropelSignalCreatedEvent`.

El listener `TropelSignalNotificationListener` esta en otra clase, usa:

- `@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)`
- `@Async("tropelTaskExecutor")`
- `@Transactional`

El email se envia despues del commit y en un hilo `tropel-worker-X`. Si SMTP falla, la senal queda en `ERROR` y se crea `NotificationLog` con `FAILED`.

## Endpoints principales

```text
GET  /api/v1/guardians
GET  /api/v1/guardians/{id}

POST /api/v1/sectors
GET  /api/v1/sectors
GET  /api/v1/sectors/{id}

POST /api/v1/tropels
GET  /api/v1/tropels
GET  /api/v1/tropels/{id}

POST /api/v1/signals
GET  /api/v1/signals
GET  /api/v1/signals/{id}
GET  /api/v1/signals/{id}/care-response
GET  /api/v1/signals/{id}/notifications
```

## Datos rapidos de prueba

Crear sector:

```json
{
  "sectorCode": "SECTOR-7",
  "climate": "RETRO_ARCADE",
  "capacity": 1
}
```

Crear Tropel:

```json
{
  "name": "BipBop",
  "species": "GLITCHY",
  "sectorId": 1,
  "guardianId": 1
}
```

Crear senal:

```json
{
  "tropelId": 1,
  "guardianId": 1,
  "senderTag": "sensor-norte-7",
  "rawContent": "BipBop lleva 3 ciclos sin recibir nutrientes y ha empezado a morder los bordes del sector digital."
}
```

## Tests incluidos

Hay pruebas unitarias para:

- Clasificacion IA exitosa.
- Fallback cuando la IA falla.
- Limites de stats en severidad `CRITICO`.
- Mapeo `signalType` a `responseCode`.
- Reduccion de estabilidad por `FUGA`.
- Validacion de `guardianId`.
- Sector lleno al crear Tropel.
- Parser de JSON con texto extra alrededor.
