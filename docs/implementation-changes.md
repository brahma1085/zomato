# AWS Implementation Changes

## 1. Purpose

This document defines the application-level and infrastructure-facing changes required to make the existing application deployable on AWS using:

- Vercel for the Angular frontend
- Amazon ECS for containerized backend services
- Amazon ECR for Docker images
- Amazon RDS PostgreSQL for relational persistence
- Amazon OpenSearch Service for search/indexing
- Amazon ECS for Keycloak
- Application Load Balancer (ALB) for public API routing
- Amazon VPC, private/public subnets and Security Groups
- AWS Secrets Manager for credentials/secrets
- Amazon CloudWatch for logs and monitoring
- Amazon S3 where object/file storage is required
- IAM for service permissions
- Route 53 / ACM when a custom AWS-managed domain and certificate are required

The design intentionally keeps the application portable: Docker remains the packaging boundary and Spring Boot services remain independent of AWS-specific business logic.

> Important: AWS Free Tier has changed for accounts created after July 15, 2025. New accounts receive $100 credits at signup and may earn up to another $100; the Free account plan can be used for eligible services for up to six months. Treat AWS as a learning environment and monitor billing continuously. See the companion deployment plan for current-cost controls.

---

## 2. Target Application Architecture

```text
Vercel
  |
  | HTTPS
  v
AWS Application Load Balancer
  |
  +--> ECS Service: API Gateway / Edge service
  |
  +--> ECS Service: Keycloak
  |
  +--> ECS Service: Microservice 1
  +--> ECS Service: Microservice 2
  +--> ...
  +--> ECS Service: Microservice 9
             |
             +--> RDS PostgreSQL
             |
             +--> OpenSearch
             |
             +--> S3 (when required)
             |
             +--> Secrets Manager
             |
             +--> CloudWatch
```

The initial deployment can expose only the ALB. RDS and OpenSearch must remain private.

---

## 3. Application Changes

### 3.1 Externalize configuration

Do not hard-code:

- database URLs
- database usernames/passwords
- Keycloak URLs
- Keycloak client secrets
- OpenSearch endpoints
- JWT secrets
- third-party API keys
- environment-specific URLs

Use Spring Boot configuration properties and environment variables.

Recommended structure:

```text
application.yml
application-local.yml
application-aws.yml
```

Prefer environment-variable overrides in AWS.

Example:

```yaml
spring:
  datasource:
    url: ${DB_URL}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}

search:
  endpoint: ${SEARCH_ENDPOINT}

keycloak:
  issuer-uri: ${KEYCLOAK_ISSUER_URI}
```

Do not commit production secrets to Git.

---

## 4. Spring Boot Container Changes

Every microservice must have:

- Dockerfile
- ARM64-compatible or multi-architecture image support
- health endpoint
- graceful shutdown
- externalized configuration
- predictable application port
- CloudWatch-compatible stdout/stderr logging

Recommended Spring Boot dependencies/features:

```text
spring-boot-starter-actuator
spring-boot-starter-validation
```

Expose:

```text
/actuator/health
```

Do not expose sensitive actuator endpoints publicly.

---

## 5. JVM Memory Configuration

The application must not assume unlimited memory.

For ECS, use container-aware JVM sizing.

Recommended starting point:

```text
JAVA_TOOL_OPTIONS=
-XX:MaxRAMPercentage=60
-XX:InitialRAMPercentage=20
-XX:+UseContainerSupport
```

Tune per service after measuring actual memory use.

Do not blindly assign 1 GB heap to every microservice.

---

## 6. Docker Image Standard

Use a small Java runtime image and multi-stage builds where appropriate.

Conceptual pattern:

```text
Build stage
  Maven
    |
    v
Spring Boot JAR
    |
    v
Runtime stage
  Java 17/21 runtime
```

Use immutable image tags.

Recommended:

```text
restaurant-service:git-<commit-sha>
restaurant-service:<version>
```

Avoid relying only on:

```text
latest
```

---

## 7. Multi-Architecture Images

OCI A1 is ARM64, while AWS can use either x86_64 or ARM64 depending on the selected compute.

Build images as multi-platform when practical:

```text
linux/amd64
linux/arm64
```

Example:

```bash
docker buildx build   --platform linux/amd64,linux/arm64   -t <ecr-repository>:<version>   --push .
```

This keeps the same application portable between OCI and AWS.

---

## 8. Service-to-Service Communication

Do not hard-code container IP addresses.

Use logical service DNS names in local Docker Compose and ECS service discovery / internal routing in AWS.

Example:

```text
USER_SERVICE_URL
RESTAURANT_SERVICE_URL
SEARCH_SERVICE_URL
RECOMMENDATION_SERVICE_URL
```

For production-like AWS deployment, prefer stable internal DNS/service discovery or internal ALB routing where appropriate.

---

## 9. Keycloak Changes

Keycloak should be treated as an independently deployable container.

Externalize:

```text
KC_DB
KC_DB_URL
KC_DB_USERNAME
KC_DB_PASSWORD
KC_HOSTNAME
KC_PROXY_HEADERS
```

The Keycloak database should be PostgreSQL.

Recommended architecture:

```text
Keycloak
   |
   v
RDS PostgreSQL
```

Do not expose the Keycloak database publicly.

The public frontend should use the stable Keycloak hostname, for example:

```text
https://auth.example.com
```

The Spring Boot resource servers should validate JWTs against the Keycloak issuer.

---

## 10. PostgreSQL Changes

For AWS, move PostgreSQL out of Docker and into Amazon RDS.

Application change:

```text
Before:
service -> postgres Docker container

After:
service -> RDS PostgreSQL endpoint
```

No code should depend on a Docker container name such as:

```text
postgres
```

Instead use:

```text
DB_HOST
DB_PORT
DB_NAME
DB_USERNAME
DB_PASSWORD
```

The RDS endpoint is supplied through AWS configuration.

---

## 11. Elasticsearch/OpenSearch Changes

For AWS, replace the self-managed Elasticsearch container with Amazon OpenSearch Service where application compatibility permits.

Abstract the search client behind an application service/interface.

Recommended:

```text
SearchService
   |
   +--> Local Elasticsearch implementation
   |
   +--> AWS OpenSearch implementation
```

Avoid scattering Elasticsearch-specific connection code throughout business services.

Before migration, verify:

- Elasticsearch/OpenSearch version compatibility
- Java client compatibility
- query DSL used
- aggregations
- geo queries
- analyzers
- index mappings
- authentication method

---

## 12. Database Migration

Create repeatable SQL/Flyway/Liquibase migrations.

Recommended:

```text
src/main/resources/db/migration/
    V1__initial_schema.sql
    V2__restaurant_tables.sql
    V3__search_metadata.sql
```

The deployment should never depend on manually modifying the RDS database.

If Flyway or Liquibase is already available, retain it.

---

## 13. CORS

Because the frontend is hosted on Vercel and the backend is on AWS, configure CORS explicitly.

Example concept:

```text
Allowed Origin:
https://<your-vercel-domain>
```

Do not use:

```text
*
```

for authenticated production APIs.

Keep the allowed frontend origin configurable:

```text
FRONTEND_ORIGIN
```

---

## 14. Authentication Flow

Target flow:

```text
Angular/Vercel
      |
      v
   Keycloak
      |
      | JWT
      v
AWS ALB
      |
      v
Spring Boot services
      |
      v
JWT validation
```

The backend must validate:

- issuer
- audience where applicable
- signature
- expiration
- roles/scopes

Do not rely only on frontend route guards for security.

---

## 15. Logging

All containers should log to stdout/stderr.

Avoid writing application logs only to local files.

AWS path:

```text
ECS container
    |
    v
CloudWatch Logs
```

Use structured JSON logging later if practical.

Include:

- timestamp
- service name
- correlation/request ID
- log level
- trace ID when tracing is introduced

Never log:

- passwords
- access tokens
- refresh tokens
- database credentials
- client secrets

---

## 16. Health Checks

Every service should expose:

```text
/actuator/health
```

Recommended health categories:

```text
Liveness
Readiness
```

ECS/ALB health checks should use a lightweight endpoint.

Do not make an ALB health check depend on Elasticsearch or a slow downstream service unless the service genuinely cannot operate without it.

---

## 17. Graceful Shutdown

Enable Spring graceful shutdown so ECS deployments do not abruptly terminate active requests.

Concept:

```yaml
server:
  shutdown: graceful

spring:
  lifecycle:
    timeout-per-shutdown-phase: 30s
```

Tune based on real request durations.

---

## 18. File/Object Storage

If the application currently stores uploaded files/images on the local filesystem, change it for AWS:

```text
Application
    |
    v
Amazon S3
```

Store only metadata/path references in PostgreSQL.

Do not depend on container-local filesystem persistence.

---

## 19. Time and Locale

Store timestamps in UTC.

Use:

```text
Instant
OffsetDateTime
```

where appropriate.

Convert to Indian/local presentation time at the UI layer.

Do not make server-local timezone assumptions.

---

## 20. Environment Separation

Create explicit environments:

```text
local
aws-dev
aws-test
aws-prod
```

At minimum:

```text
Local:
Docker Compose

AWS:
ECS + RDS + OpenSearch
```

Do not use production credentials in local development.

---

## 21. Recommended Repository Structure

```text
project-root/
|
+-- service-1/
+-- service-2/
+-- ...
+-- service-9/
|
+-- keycloak/
|
+-- deployment/
|    +-- docker/
|    +-- aws/
|         +-- ecs/
|         +-- task-definitions/
|         +-- networking/
|         +-- scripts/
|
+-- docs/
|    +-- implementation-changes.md
|    +-- deployment-plans.md
|
+-- docker-compose.yml
+-- docker-compose.local.yml
```

Keep AWS deployment definitions outside the business-service source directories.

---

## 22. CI/CD Preparation

The application should support this pipeline:

```text
Git Push
   |
   v
Build
   |
   v
Unit Tests
   |
   v
Docker Build
   |
   v
Security/quality checks
   |
   v
ECR Push
   |
   v
ECS Deployment
   |
   v
Health Check
   |
   v
Deployment Complete
```

Use GitHub Actions initially or Jenkins if the existing Jenkins setup is intentionally being retained.

---

## 23. AWS Portability Principles

Avoid AWS SDK usage in core business logic.

Good:

```text
Business service
    |
    v
StorageService interface
    |
    +--> S3 implementation
```

Avoid:

```text
BusinessService
    |
    +--> directly calls S3 SDK everywhere
```

This keeps the application portable to OCI and other environments.

---

## 24. Definition of Done

The AWS-compatible application is ready when:

- [ ] All 9 services build independently.
- [ ] All 9 services have Dockerfiles.
- [ ] Images support the selected AWS CPU architecture.
- [ ] Configuration is externalized.
- [ ] Secrets are not stored in Git.
- [ ] Actuator health endpoints work.
- [ ] CORS supports the Vercel domain.
- [ ] PostgreSQL works through RDS.
- [ ] Search works through OpenSearch.
- [ ] Keycloak works with RDS PostgreSQL.
- [ ] Services validate Keycloak JWTs.
- [ ] Logs go to stdout/stderr.
- [ ] ECS health checks are defined.
- [ ] No service depends on a fixed container IP.
- [ ] File storage uses S3 where persistence is required.
- [ ] Docker images can be pushed to ECR.
- [ ] ECS task definitions can consume the images.
- [ ] Application can be deployed without manual code modification.
