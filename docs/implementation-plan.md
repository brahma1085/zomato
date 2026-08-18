# Zomato UC - Detailed Implementation Plan & AWS Deployment Strategy

This document outlines the architecture, current state, and step-by-step AWS deployment implementation plan for the Zomato UC platform (AI-Powered Intelligent Restaurant Recommendation & Discovery Platform).

---

## 1. Project Overview & Current Functionality
The platform replaces traditional filter-based restaurant search with an intelligent, conversational AI engine. Instead of answering "Search for restaurants," it answers "Where should I eat today?" based on deep context.

### Core Features Implemented:
- **Natural Language Search:** Converts natural queries (e.g., "Find me a good family restaurant under Rs.1,000") into structured search criteria.
- **Location Intelligence:** Uses geospatial querying to find restaurants within a specific radius or travel time.
- **Budget Intelligence:** Calculates estimated dining costs based on party size.
- **Review Intelligence:** Summarizes thousands of reviews to highlight "What people love" and "Common complaints."
- **Personalized Recommendations:** Factors in historical behavior, food preferences, and dining occasions. Features include "What are you craving?", "Because you like...", and "Try something new".
- **Multi-Agent Orchestration:** Specialized agents (Location, Preference, Budget, Search, Review) collaborate to score, rank, and explain recommendations.
- **Conversational Refinement:** Refine search results dynamically (e.g., "Only vegetarian" -> "Is parking available?").

---

## 2. System Architecture
The application follows a highly scalable microservices architecture.

### Multi-Agent AI Architecture
The `ai-service` serves as the orchestrator. It uses an LLM (OpenAI/Claude/Gemini/Groq) through Spring AI to route natural language requests to specialized agents:
- **Conversation Agent & Intent Agent:** Parses intent and extracts structured criteria.
- **Location, Preference, Budget & Search Agents:** Gathers specific data constraints.
- **Review & Social Agents:** Analyzes text sentiment and social signals.
- **Recommendation, Ranking & Explanation Agents:** Scores results and provides a human-readable explanation for why a restaurant matches the user's intent.

### Backend Microservices (Java 21 + Spring Boot 3.x)
1. **api-gateway:** Entry point, handles routing, CORS, and Keycloak authentication.
2. **discovery-server:** Netflix Eureka server for service registry.
3. **user-service:** Manages user profiles, preferences, and history.
4. **restaurant-service:** Manages core restaurant catalog, menus, and operating hours.
5. **search-service:** Provides fast text and geospatial queries via Elasticsearch / OpenSearch.
6. **review-service:** Manages user reviews and integrates with AI for summarization.
7. **recommendation-service:** Handles traditional filtering and semantic (vector) recommendations using `pgvector`.
8. **location-service:** Integrates with external Maps APIs.
9. **ai-service:** Orchestrates the multi-agent workflow using Spring AI.

---

## 3. AWS Architecture & Deployment Strategy

### Target Cloud Topology
```text
Vercel (Angular Frontend)
   |
   | HTTPS
   v
AWS Application Load Balancer (ALB)
   |
   +--> ECS Task: Keycloak (Identity Provider)
   |        |
   |        v
   |    RDS PostgreSQL (keycloak_db)
   |
   +--> ECS Task: API Gateway (Edge Router)
            |
            v (Internal VPC Service Discovery via Eureka)
         +-- user-service ----------> RDS PostgreSQL (user_db)
         +-- restaurant-service ----> RDS PostgreSQL (restaurant_db)
         +-- review-service --------> RDS PostgreSQL (review_db)
         +-- recommendation-service -> RDS PostgreSQL + pgvector (recommendation_db)
         +-- search-service --------> Amazon OpenSearch Service
         +-- location-service ------> External Maps API
         +-- ai-service ------------> Groq/Gemini LLM API
```

---

## 4. Key Architectural Analysis & Suggestions

Based on the analysis of `deployment-plans.md` and `implementation-changes.md`:

### 4.1 Cost Control & AWS Free Tier Optimization
- **Single RDS PostgreSQL Instance**: Rather than launching 5 separate database servers, a single multi-tenant RDS PostgreSQL instance (`db.t4g.micro` or `db.t3.micro`) will host separate databases (`user_db`, `restaurant_db`, `review_db`, `recommendation_db`, `keycloak_db`).
- **PostgreSQL `pgvector` Support**: AWS RDS PostgreSQL 15+ supports `pgvector`. `recommendation-service` will issue `CREATE EXTENSION IF NOT EXISTS vector;` on connection.
- **JVM Container Memory Tuning**: Spring Boot services are configured with container-aware heap bounds (`JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=70.0 -XX:+UseContainerSupport"`) to fit inside 512MB RAM tasks on ECS Fargate or EC2 `t4g.small` container instances.
- **Eureka Discovery Retention**: Retaining `discovery-server` on ECS within the VPC security group (`sg-ecs`) allows Spring Cloud Gateway to resolve microservices dynamically (`lb://SERVICE-NAME`) without modifying gateway routing code.

### 4.2 Application Code & Configuration Changes Required
1. **Multi-Profile Spring Boot Setup (`application-aws.yml`)**:
   - Introduce `application-aws.yml` in `user-service`, `restaurant-service`, `review-service`, `recommendation-service`, `search-service`, `api-gateway`, `ai-service`, and `location-service`.
   - Add PostgreSQL drivers to `pom.xml` files for H2-only services (`user-service`, `restaurant-service`, `review-service`).
   - Configure PostgreSQL Dialect (`org.hibernate.dialect.PostgreSQLDialect`) when profile `aws` is active, keeping H2 dialect for default/local profile.
2. **CORS & Authentication Externalization**:
   - `api-gateway` will consume `${FRONTEND_URL}` for dynamic CORS origins to support the Vercel app domain.
   - `KEYCLOAK_ISSUER_URI` will be externalized for public ALB Keycloak endpoint validation.
3. **OpenSearch Connection Resilience**:
   - `search-service` will support optional `${SEARCH_USERNAME}` and `${SEARCH_PASSWORD}` alongside `${SEARCH_ENDPOINT}` to connect seamlessly to Amazon OpenSearch Service.
4. **AWS Infrastructure Templates**:
   - Provide parameterized ECS Task Definitions (`deployment/aws/ecs-task-definitions.json`).
   - Provide database provisioning script (`deployment/aws/rds-init.sql`).
   - Provide environment variable template (`deployment/aws/.env.aws.example`).

---

## 5. Definition of Done for AWS Readiness
- [x] All 9 backend microservices support externalized environment variables for database, search, and auth.
- [x] `application-aws.yml` profiles added to support RDS PostgreSQL & Amazon OpenSearch Service.
- [x] PostgreSQL dependencies added to POM files where missing.
- [x] Actuator `/actuator/health` exposed across all services for ALB/ECS health checks.
- [x] Dynamic CORS configured on API Gateway for Vercel frontend integration.
- [x] RDS Database initialization script and ECS Task Definitions created in `deployment/aws/`.
- [x] Local development environment using H2 / local Docker remains 100% functional and untouched.
