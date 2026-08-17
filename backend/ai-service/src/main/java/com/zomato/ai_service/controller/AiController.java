package com.zomato.ai_service.controller;

import com.zomato.ai_service.dto.ChatRequest;
import com.zomato.ai_service.dto.ChatResponse;
import com.zomato.ai_service.service.AiOrchestratorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final AiOrchestratorService aiOrchestratorService;

    public AiController(AiOrchestratorService aiOrchestratorService) {
        this.aiOrchestratorService = aiOrchestratorService;
    }

    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest request) {
        // Mock identifying users in the conversation
        List<String> userIds = request.getUserId() != null ? List.of(request.getUserId()) : Collections.emptyList();
        
        ChatResponse response = aiOrchestratorService.orchestrateChat(userIds, request.getQuery(), request.getHistory(), request.getLat(), request.getLng());
        return ResponseEntity.ok(response);
    }
}
