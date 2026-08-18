# Zomato UC Application Walkthrough

## Overview

This walkthrough summarizes the state of the Zomato UC (AI-Powered Intelligent Restaurant Recommendation & Discovery Platform) application. The platform replaces traditional filter-based restaurant search with an intelligent, conversational AI engine designed to answer complex context-driven queries like "Where should I eat today?"

> [!NOTE]
> The system is designed to provide highly personalized recommendations by understanding natural language and orchestrating multiple specialized AI agents.

## System Architecture Highlights

The application is built on a highly scalable microservices architecture incorporating modern Java standards and AI integrations:

*   **Microservices (Java 21 + Spring Boot 3.x):**
    *   `api-gateway`: System entry point handling routing and Keycloak authentication.
    *   `discovery-server`: Netflix Eureka server for service registration and discovery.
    *   `user-service`, `restaurant-service`, `review-service`, `location-service`: Services handling core domain operations.
    *   `search-service`: Enables fast text and geospatial queries using Elasticsearch.
    *   `recommendation-service`: Manages both traditional filtering and semantic (vector-based) recommendations.
    *   `ai-service`: Acts as the orchestrator for the multi-agent workflow using Spring AI.
*   **Multi-Agent AI Engine:** The `ai-service` uses an LLM (e.g., OpenAI, Gemini) to route natural language requests to specialized collaborative agents (Conversation, Intent, Location, Preference, Budget, Search, Review, and Recommendation).
*   **Data Layer:** 
    *   PostgreSQL with pgvector for core relational data and semantic embeddings.
    *   Elasticsearch for geospatial and fast text filtering.
    *   Redis for caching and session management.
*   **Frontend:** A responsive Angular application (`frontend/web-ui`).

## Functionality Details

The platform boasts several intelligent features designed to improve user experience:

1.  **Natural Language Search:** Translates conversational requests (e.g., "Find me a good family restaurant under Rs.1,000") into structured search criteria using the intent agent.
2.  **Location & Budget Intelligence:** Uses geospatial querying to find restaurants within a specific travel time and calculates estimated costs based on party size.
3.  **Review Summarization:** Employs AI to summarize thousands of reviews, highlighting what people love and common complaints, instead of making the user read individual reviews.
4.  **Personalized Recommendations:** Factors in historical behavior, food preferences, and dining occasions to offer personalized suggestions (e.g. "Because you like...", "What are you craving?").
5.  **Dynamic Conversational Refinement:** Allows users to continuously and contextually refine search results (e.g., searching for "Only vegetarian" followed by "Is parking available?").

## Getting Started: User Guide

If you are a new user wanting to run and interact with the application locally, follow these steps:

1.  **Start the Environment:** Run the `start_all.ps1` PowerShell script in the root directory. This will boot up all necessary databases via Docker Compose and spin up all Spring Boot microservices and the Angular frontend as background jobs.
2.  **Access the Application:** Open your web browser and navigate to the frontend at `http://localhost:4200`.
3.  **Authentication:** Upon trying to access secure areas, you will be redirected to the Keycloak login page. Authenticate using your credentials (or default test credentials if configured).
4.  **Interact with the AI:** Once logged in, you can start using the main conversational interface. Try asking it natural questions like "Suggest a romantic dinner place in downtown" and interact with the follow-up refinements.

## Service URLs for Developers

If you are a developer looking to verify, test, or debug the services, you can access the following local URLs once the `start_all.ps1` script has successfully started all components:

### Frontend & Infrastructure
*   **Frontend (Angular):** `http://localhost:4200`
*   **Keycloak (Auth Server):** `http://localhost:9090`
*   **PostgreSQL Database:** `localhost:5432`
*   **Elasticsearch:** `http://localhost:9200`
*   **Redis:** `localhost:6379`

### Backend Microservices
*   **API Gateway:** `http://localhost:8080` *(Primary entry point for frontend API requests)*
*   **Discovery Server (Eureka):** `http://localhost:8761` *(Visit this URL to view all registered and active microservices)*
*   **User Service:** `http://localhost:8081`
*   **Restaurant Service:** `http://localhost:8082`
*   **Search Service:** `http://localhost:8083`
*   **Location Service:** `http://localhost:8084`
*   **Review Service:** `http://localhost:8085`
*   **AI Service:** `http://localhost:8086`
*   **Recommendation Service:** `http://localhost:8087`

## Deployment Strategy

The application is fully containerized and orchestrated for seamless deployment:
*   **Dockerization:** Each microservice utilizes a multi-stage `Dockerfile`.
*   **Orchestration:** `docker/docker-compose.yml` configures all core backend dependencies.
*   **CI/CD:** Uses GitHub Actions (`deploy-backend.yml`) for automated SSH deployments to Oracle/AWS servers.
