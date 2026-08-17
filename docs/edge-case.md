# Zomato AI Concierge - Edge Cases & Mitigation Strategies

This document outlines potential edge cases, failure modes, and mitigation strategies for the Zomato AI Concierge platform, based on the proposed architecture and frontend enhancements (Occasion Selector & Compare Functionality).

## 1. Frontend & UI Enhancements

### 1.1 Occasion Selector ("Quick Pills")
- **Rapid/Spam Clicks:** A user rapidly clicks multiple different occasion pills (e.g., "Date Night" then immediately "Cheap Eats").
  - **Mitigation:** Implement debouncing on the frontend. Cancel any in-flight AI requests if a new pill is clicked before the previous request completes.
- **Zero Results for Strict Constraints:** A user selects "Vegan Friendly" but their current location (handled by Location Service) has absolutely no vegan options within a reasonable radius.
  - **Mitigation:** The AI should gracefully explain the lack of exact matches and offer the closest alternatives (e.g., "I couldn't find strict vegan places nearby, but here are some vegetarian-friendly options slightly further away").
- **Backend AI Timeout:** The AI Orchestrator service takes too long to respond to a pill click.
  - **Mitigation:** Implement frontend timeout handling with a user-friendly error message, potentially falling back to a standard Elasticsearch query via the Search Service bypassing the AI.

### 1.2 Compare Functionality
- **Exceeding Selection Limits:** A user attempts to select 10+ restaurants for comparison.
  - **Mitigation:** Enforce a hard limit on the frontend (e.g., max 3 or 4 restaurants) to ensure the UI matrix remains readable on mobile devices and to prevent exceeding the LLM's context window for the "AI Verdict".
- **Missing Data Fields:** One of the selected restaurants is missing crucial data (e.g., no known price tier, or zero reviews to generate a vibe).
  - **Mitigation:** The Compare matrix should gracefully handle null values (e.g., displaying "N/A" or "Not enough data"). The AI Verdict should be instructed to ignore missing fields rather than hallucinating values.
- **Vastly Different Geographies:** User tries to compare a restaurant in New York with one in London.
  - **Mitigation:** The Location Agent needs to handle extreme distances gracefully. The Compare table's "Distance" field should reflect absolute distance from the user's current context, and the AI Verdict shouldn't penalize one purely based on inter-continental routing unless specifically requested.
- **AI Verdict Failure:** The LLM fails to generate the comparative summary.
  - **Mitigation:** The Compare view must be resilient and still display the raw data matrix (Rating, Price, Distance) even if the AI summary fails to generate.

## 2. Multi-Agent AI System

### 2.1 Agent Timeouts & Partial Failures
- **Scenario:** The Social Recommendation Agent takes too long to fetch graph data from the User Service, or the external Google Maps API times out for the Location Agent.
  - **Mitigation:** The AI Orchestrator must implement strict SLAs/timeouts for child agents. If a non-critical agent (like Social) times out, the Orchestrator should proceed with partial data (graceful degradation) rather than failing the entire user request.
### 2.2 Contradictory Agent Signals
- **Scenario:** The Budget Agent strongly recommends a fast-food place, but the Preference Agent strongly rejects it based on dietary history.
  - **Mitigation:** The Ranking Agent needs clearly defined, weighted resolution rules. Safety/Dietary constraints (Preference Agent) must always strictly override Budget or Social signals.
### 2.3 LLM Hallucinations (Review Analysis & Explanation)
- **Scenario:** The Review Analysis Agent hallucinates a popular dish that the restaurant does not actually serve, which is then surfaced by the Explanation Agent.
  - **Mitigation:** Implement strict RAG grounding. Prompt the Explanation Agent to *only* use facts retrieved from the Restaurant Service menu and the validated Review Service summaries. Include citation tracking if possible.
### 2.4 Context Window Exhaustion
- **Scenario:** A highly popular restaurant has tens of thousands of reviews, overwhelming the Review Analysis Agent.
  - **Mitigation:** The Review Service should pre-aggregate and summarize reviews periodically in the background (batch processing). The Agent should query these pre-calculated embeddings/summaries rather than raw text on the fly.

## 3. Backend & Data Infrastructure

### 3.1 Vector Search Anomalies
- **Scenario:** The `pgvector` similarity search returns completely irrelevant restaurants because the semantic embedding of a user's query ("quiet place for reading") mapped poorly to restaurant descriptions.
  - **Mitigation:** Implement hybrid search. Combine vector similarity (semantic) with traditional BM25/Elasticsearch keyword scoring (lexical) to ensure baseline relevance.
### 3.2 Eventual Consistency Delays
- **Scenario:** A user writes a highly negative review, but the elasticsearch index or vector embeddings haven't updated yet, causing the Recommendation Agent to still highly recommend it seconds later.
  - **Mitigation:** Acceptable for a highly scalable system, but critical updates (like a restaurant permanently closing) should bypass caches and immediately invalidate related vectors.
