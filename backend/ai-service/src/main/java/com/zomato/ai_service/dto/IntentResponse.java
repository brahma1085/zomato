package com.zomato.ai_service.dto;

import java.util.Map;

public class IntentResponse {
    private String intentType; // e.g., SEARCH, RECOMMENDATION, QUESTION
    private Map<String, Object> extractedParameters; // e.g., {"cuisine": "Italian", "location": "Koramangala"}
    private String rawAiResponse;

    public IntentResponse() {
    }

    public String getIntentType() {
        return intentType;
    }

    public void setIntentType(String intentType) {
        this.intentType = intentType;
    }

    public Map<String, Object> getExtractedParameters() {
        return extractedParameters;
    }

    public void setExtractedParameters(Map<String, Object> extractedParameters) {
        this.extractedParameters = extractedParameters;
    }

    public String getRawAiResponse() {
        return rawAiResponse;
    }

    public void setRawAiResponse(String rawAiResponse) {
        this.rawAiResponse = rawAiResponse;
    }
}
