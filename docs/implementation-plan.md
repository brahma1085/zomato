# Zomato UC - Detailed Implementation Plan & Current State

This document outlines the current functionality, architecture, and deployment plan for the Zomato UC (AI-Powered Intelligent Restaurant Recommendation & Discovery Platform).

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

## 2. System Architecture
The application follows a highly scalable microservices architecture.

### Multi-Agent AI Architecture
The `ai-service` serves as the orchestrator. It uses an LLM (OpenAI/Claude/Gemini) through Spring AI to route natural language requests to specialized agents:
- **Conversation Agent & Intent Agent:** Parses intent and extracts structured criteria.
- **Location, Preference, Budget & Search Agents:** Gathers specific data constraints.
- **Review & Social Agents:** Analyzes text sentiment and social signals.
- **Recommendation, Ranking & Explanation Agents:** Scores results and provides a human-readable explanation for why a restaurant matches the user's intent.

### Backend Microservices (Java 21 + Spring Boot 3.x)
1. **api-gateway:** Entry point, handles routing and Keycloak authentication.
2. **discovery-server:** Netflix Eureka server for service registry.
3. **user-service:** Manages user profiles, preferences, and history.
4. **restaurant-service:** Manages core restaurant catalog, menus, and operating hours.
5. **search-service:** Provides fast text and geospatial queries via Elasticsearch.
6. **review-service:** Manages user reviews and integrates with AI for summarization.
7. **recommendation-service:** Handles traditional filtering and semantic (vector) recommendations.
8. **location-service:** Integrates with external Maps APIs.
9. **ai-service:** Orchestrates the multi-agent workflow using Spring AI.

### Data & Storage
- **PostgreSQL (+ pgvector):** Core relational data and vector embeddings for semantic search.
- **Elasticsearch:** Fast geospatial, text, and filtering engine.
- **Redis:** Caching and session management.

## 3. Technology Stack
- **Frontend:** Angular/React (`frontend/web-ui`)
- **Backend:** Java 21, Spring Boot 3.x
- **AI Integration:** Spring AI, OpenAI/Gemini APIs
- **Database:** PostgreSQL (with pgvector), Elasticsearch, Redis
- **Security:** Keycloak, OAuth2, JWT
- **Containerization & Orchestration:** Docker, Docker Compose
- **Scripting & E2E Testing:** Python (`python/e2e_tests.py`), PowerShell scripts

## 4. Deployment Implementation Plan
The application uses Docker Compose for orchestration.

### 4.1. Environment Configuration
The `.env` (generated from `.env.template`) configures secrets and URLs for the environment, including:
- `EUREKA_URL`, `KEYCLOAK_ISSUER_URI`, `FRONTEND_URL`
- `USER_DB_URL`, `REVIEW_DB_URL`, `RESTAURANT_DB_URL`, `RECOMMENDATION_DB_URL`
- `ELASTICSEARCH_URL`, `GEMINI_API_KEY`

### 4.2. Dockerization
Each of the 9 microservices is built using a multi-stage `Dockerfile`:
1. **Build Stage:** `maven:3.9-eclipse-temurin-17` builds the application.
2. **Runtime Stage:** Lightweight `eclipse-temurin:17-jre-alpine` runs the `.jar`.
Memory constraints are applied (e.g., `-Xmx512m`) to optimize resource consumption.

### 4.3. Docker Compose Orchestration
The deployment relies on `docker/docker-compose.yml` (and `docker-compose.prod.yml`):
- Provisions `postgres` (with `pgvector`), `elasticsearch`, `redis`, and `keycloak`.
- Connects all backend services via a custom internal network (`zomato-network`).
- Maps necessary host ports (e.g., `8080` for `api-gateway`, `8761` for `discovery-server`).

### 4.4. CI/CD Pipeline
- Automated scripts (`start_all.ps1`, `restart_services.ps1`) manage local multi-service testing.
- A GitHub Actions workflow (`deploy-backend.yml`) handles SSH-based deployment to an Oracle/AWS server using the `docker-compose.prod.yml` configuration.

## 5. Logging Mechanism Implementation
To ensure production readiness, the application implements structured and pervasive logging across both the backend and frontend.

### Backend (Spring Boot SLF4J + Logback)
- `System.out.println` and `System.err.println` have been removed in favor of standard SLF4J loggers.
- **Controllers:** `UserController`, `SearchController`, `ReviewController`, `RestaurantController`, `RecommendationController`, `LocationController`, and `AiController` all initialize `org.slf4j.Logger`. They trace incoming requests and parameters at the `INFO` level.
- **Exception Handling:** Hardcoded `e.printStackTrace()` occurrences have been replaced with `logger.error("Exception message", e)` to securely preserve and format full stack traces.
- Console logging is automatically formatted with timestamps, thread names, log levels, and class names via Spring Boot.

### Frontend (Angular)
- Structured logging using `console.info` and `console.error` has been added to key components and services (`app.ts`, `location.service.ts`, `restaurant-detail.component.ts`, `ai-curated-results.component.ts`).
- Lifecycle events, state changes, external API calls, and OAuth logic now output trace logs for easier debugging and user flow tracking.

## 6. API Documentation (Swagger/OpenAPI)
To facilitate seamless API exploration and testing without breaking existing functionality, Swagger (OpenAPI 3) has been integrated into the backend microservices.

### Implementation Details
- The `springdoc-openapi-starter-webmvc-ui` dependency (version `2.6.0`) is included in the `pom.xml` of each core service (`user-service`, `restaurant-service`, `ai-service`, etc.) as well as the `api-gateway`.
- **Accessing the UI:** Developers can interact with the API endpoints of any service by navigating to `http://localhost:<service-port>/swagger-ui.html` locally.
- **Auto-configuration:** By leveraging Spring Boot 3 auto-configuration, Swagger surfaces all REST controllers and data schemas automatically without requiring extensive manual boilerplate annotations.
