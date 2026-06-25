package com.example.hotelbooking.Controllers.AiController;

import com.example.hotelbooking.DTOs.AI.ChatRequest;
import com.example.hotelbooking.Services.AI.AiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@Tag(name = "Chat Bot API's", description = "API's for users to interact and get data interacting with AI")
public class AiController {

    private final AiService aiService;

    @PostMapping("/chat")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "API for customers")
    public ResponseEntity<String> chat(@RequestBody ChatRequest request) {
        String response = aiService.customerChat(
                request.getMessage(),
                request.getContext(),
                request.getContextId()
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/summary")
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(summary = "API for managers")
    public ResponseEntity<String> managerSummary(@RequestBody(required = false) ChatRequest request) {
        String response = aiService.getManagerSummary(request != null ? request : new ChatRequest());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/owner-report")
    @PreAuthorize("hasRole('OWNER')")
    @Operation(summary = "API for owners")
    public ResponseEntity<String> ownerReport(@RequestBody(required = false) ChatRequest request) {
        String response = aiService.getOwnerReport(request != null ? request : new ChatRequest());
        return ResponseEntity.ok(response);
    }
}
