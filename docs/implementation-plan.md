# Zomato UC - Deployment Implementation Plan

This plan details the necessary coding changes to implement the production-level deployment strategy outlined in `deployment-plan.md`.

## User Review Required
> [!IMPORTANT]
> Please review the `.env.template` variables list and ensure it aligns with your Oracle deployment credentials.

## Proposed Changes

### 1. Environment Configuration
#### [NEW] `.env.template`
Add a template file in the root directory for all necessary environment variables:
- `EUREKA_URL`
- `KEYCLOAK_ISSUER_URI`
- `FRONTEND_URL`
- `USER_DB_URL`
- `REVIEW_DB_URL`
- `RESTAURANT_DB_URL`
- `RECOMMENDATION_DB_URL`
- `ELASTICSEARCH_URL`
- AI service keys (e.g., `GEMINI_API_KEY`)

---

### 2. Spring Boot Configuration (`backend/*/src/main/resources/application.yml`)
Modify `application.yml` files in all 9 backend services to use environment variables for hardcoded URLs (with fallbacks to localhost).

#### [MODIFY] `backend/api-gateway/src/main/resources/application.yml` & `CorsConfig.java`
- Update `eureka.client.serviceUrl.defaultZone` to `${EUREKA_URL:http://localhost:8761/eureka/}`
- Update `spring.security.oauth2.resourceserver.jwt.issuer-uri` to `${KEYCLOAK_ISSUER_URI:http://localhost:9090/realms/zomato-realm}`
- Update `CorsConfig.java` to allow `${FRONTEND_URL:http://localhost:4200}`

#### [MODIFY] `backend/discovery-server/src/main/resources/application.yml`
- Change `eureka.instance.hostname` to `${EUREKA_HOSTNAME:localhost}`

#### [MODIFY] `backend/user-service/src/main/resources/application.yml`
- Update `eureka.client.serviceUrl.defaultZone` to `${EUREKA_URL:http://localhost:8761/eureka/}`
- Update `spring.datasource.url` to `${USER_DB_URL:jdbc:h2:mem:zomatodb;DB_CLOSE_DELAY=-1}`

#### [MODIFY] `backend/review-service/src/main/resources/application.yml`
- Update `eureka.client.serviceUrl.defaultZone` to `${EUREKA_URL:http://localhost:8761/eureka/}`
- Update `spring.datasource.url` to `${REVIEW_DB_URL:jdbc:h2:mem:zomatodb;DB_CLOSE_DELAY=-1}`

#### [MODIFY] `backend/search-service/src/main/resources/application.yml`
- Update `eureka.client.serviceUrl.defaultZone` to `${EUREKA_URL:http://localhost:8761/eureka/}`
- Update `spring.elasticsearch.uris` to `${ELASTICSEARCH_URL:http://localhost:9200}`

#### [MODIFY] `backend/restaurant-service/src/main/resources/application.yml`
- Update `eureka.client.serviceUrl.defaultZone` to `${EUREKA_URL:http://localhost:8761/eureka/}`
- Update `spring.datasource.url` to `${RESTAURANT_DB_URL:jdbc:h2:mem:testdb}`

#### [MODIFY] `backend/recommendation-service/src/main/resources/application.yml`
- Update `eureka.client.serviceUrl.defaultZone` to `${EUREKA_URL:http://localhost:8761/eureka/}`
- Update `spring.datasource.url` to `${RECOMMENDATION_DB_URL:jdbc:postgresql://localhost:5432/zomatodb}`

#### [MODIFY] `backend/location-service/src/main/resources/application.yml`
- Update `eureka.client.serviceUrl.defaultZone` to `${EUREKA_URL:http://localhost:8761/eureka/}`

#### [MODIFY] `backend/ai-service/src/main/resources/application.yml`
- Update `eureka.client.serviceUrl.defaultZone` to `${EUREKA_URL:http://localhost:8761/eureka/}`

---

### 3. Dockerization

#### [NEW] `backend/*/Dockerfile` (For all 9 services)
Create a highly optimized multi-stage `Dockerfile` for each of the 9 backend services.
1. **Build Stage:** Use `maven:3.9-eclipse-temurin-17` to build the JAR.
2. **Runtime Stage:** Use a lightweight `eclipse-temurin:17-jre-alpine` for the runtime.
3. Expose the respective service port.
4. Set memory constraints (e.g., `-Xmx512m`) in the entrypoint to ensure the 24GB RAM limit on Oracle is respected.

---

### 4. Docker Compose Orchestration

#### [NEW] `docker/docker-compose.prod.yml`
Create a Docker Compose file to orchestrate all services:
- Define all 9 backend services using their respective `Dockerfile`s (`build: context: ../backend/<service>`).
- Define an internal network (`zomato-network`).
- Pass environment variables from `.env` to all containers.
- Map only `api-gateway` (port 8080) and `discovery-server` (port 8761) to the host.

---

### 5. CI/CD Pipeline

#### [NEW] `.github/workflows/deploy-backend.yml`
Create a GitHub Actions workflow:
- Trigger on push to `main` branch, filtering for changes in `backend/` and `docker/`.
- Use an SSH action (`appleboy/ssh-action`) to connect to the Oracle instance using secrets.
- Run a script on the remote server to pull the latest code and execute `docker-compose -f docker/docker-compose.prod.yml up -d --build`.

## Verification Plan
### Automated Tests
- N/A for deployment scripts, but we will test the Docker build process locally.
### Manual Verification
- Run `docker-compose -f docker/docker-compose.prod.yml build` to verify successful multi-stage builds.
- Ask the user to verify the generated `.env.template` and `.github/workflows/deploy-backend.yml` files.
