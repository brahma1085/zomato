# Implementation Plan: Location + Search Filtering & AI Notes

## Goal Description
1. **Combine Location & Search Context**: Ensure that when a user searches for restaurants near a specific location (using coordinates or a place name), the results are filtered to match the user's semantic search context (e.g., "Italian", "pizza", "cozy").
2. **Add AI Notes**: Augment the API response so that each recommended restaurant includes a customized "AI Note" explaining why it's a good fit for the user's preferences.

## Proposed Changes

---

### Recommendation Service (`recommendation-service`)

#### [MODIFY] [RecommendationService.java](file:///d:/GenAI/Practice/Zomato_UC/backend/recommendation-service/src/main/java/com/zomato/recommendation_service/service/RecommendationService.java)
- Update the `getRecommendations` method's location-fetching logic.
- **Current Behavior**: If location/lat/lng is provided, it returns all nearby restaurants and completely bypasses the semantic vector search.
- **New Behavior**: 
  - Fetch semantically similar restaurants using the `vectorStore` (top 20 candidates based on `context`).
  - Fetch nearby restaurants using `restaurantServiceClient`.
  - Perform an intersection: Return the local restaurants that are also present in the semantic similarity search results.
  - If the intersection is empty (e.g. vector DB isn't fully populated), fallback to a basic text search filter on the local restaurants using the user's `context` string.

---

### AI Orchestrator Service (`ai-service`)

#### [MODIFY] [AiOrchestratorService.java](file:///d:/GenAI/Practice/Zomato_UC/backend/ai-service/src/main/java/com/zomato/ai_service/service/AiOrchestratorService.java)
- In the `orchestrateChat` method, after retrieving the `finalCandidates` (the top 3 ranked restaurants), iterate through them.
- For each candidate, call a new or updated method on `RecommendationAgent` to generate a brief "AI Note" explaining why it was chosen based on the user's preferences.
- Inject this note into the candidate's map with the key `"aiNotes"` so it gets returned in the final `ChatResponse`.

#### [MODIFY] [RecommendationAgent.java](file:///d:/GenAI/Practice/Zomato_UC/backend/ai-service/src/main/java/com/zomato/ai_service/agent/RecommendationAgent.java)
- Add a method (e.g., `generateAiNote(Map<String, Object> parameters, Map<String, Object> restaurant)`) to generate a concise, 1-2 sentence note for a single restaurant.

## Verification Plan

### Automated Tests
- Run existing tests using `python e2e_tests.py` and `python test_api.py` to ensure no regression.

### Manual Verification
- Test the `/api/ai/chat` endpoint manually using `curl` or Postman with `lat`, `lng`, and a specific `query` (e.g., "Italian") to verify that the returned restaurants are both nearby and actually Italian.
- Check that the `aiNotes` field exists in the response and contains meaningful explanations.
