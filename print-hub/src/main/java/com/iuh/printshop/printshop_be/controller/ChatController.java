package com.iuh.printshop.printshop_be.controller;

import com.iuh.printshop.printshop_be.dto.ai.ChatResponse;
import com.iuh.printshop.printshop_be.service.AiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
@Tag(name = "Chat", description = "AI Chat API")
public class ChatController {

    private final AiService aiService;

    @PostMapping
    @Operation(summary = "Chat with AI", description = "Send a message to AI and get response with product recommendations")
    public ResponseEntity<ChatResponse> chat(@RequestBody String userInput) {
        ChatResponse response = aiService.processUserInput(userInput);
        return ResponseEntity.ok(response);
    }
}

