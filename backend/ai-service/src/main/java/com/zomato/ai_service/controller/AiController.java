package com.zomato.ai_service.controller;

import com.zomato.ai_service.dto.ChatRequest;
import com.zomato.ai_service.dto.ChatResponse;
import com.zomato.ai_service.service.AiOrchestratorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private static final Logger logger = LoggerFactory.getLogger(AiController.class);
    private final AiOrchestratorService aiOrchestratorService;

    public AiController(AiOrchestratorService aiOrchestratorService) {
        this.aiOrchestratorService = aiOrchestratorService;
    }

    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest request) {
        logger.info("Received AI chat request from userId: {}, query: {}", request.getUserId(), request.getQuery());
        // Mock identifying users in the conversation
        List<String> userIds = request.getUserId() != null ? List.of(request.getUserId()) : Collections.emptyList();
        
        ChatResponse response = aiOrchestratorService.orchestrateChat(userIds, request.getQuery(), request.getHistory(), request.getLat(), request.getLng());
        return ResponseEntity.ok(response);
    }
}
