package com.echolink.backend.Controllers.Chat.DM;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.echolink.backend.Dtos.DM.Conversation.ConversationDto;
import com.echolink.backend.Dtos.DM.Conversation.ConversationRequestDto;
import com.echolink.backend.Dtos.DM.Conversation.ConversationResponseDto;
import com.echolink.backend.Dtos.DM.Message.SeenDto;
import com.echolink.backend.Entities.WSEvent;
import com.echolink.backend.Services.Chat.DM.DMConversationService;

@RequestMapping("/dm")
@RestController
public class DMConversationController {

    private final DMConversationService conversationService;
    private final SimpMessagingTemplate messagingTemplate;

    public DMConversationController(DMConversationService conversationService,
            SimpMessagingTemplate messagingTemplate) {
        this.conversationService = conversationService;
        this.messagingTemplate = messagingTemplate;
    }

    @PostMapping("/start")
    public ResponseEntity<ConversationResponseDto> startConversation(@RequestBody ConversationRequestDto requestDto) {
        return this.conversationService.startConversation(requestDto);
    }

    @GetMapping("/conversations")
    public ResponseEntity<List<ConversationDto>> getConversations() {
        System.out.println("reached the conversations");
        return this.conversationService.getConversations();
    }

    @GetMapping("/pendingConversations")
    public ResponseEntity<List<ConversationDto>> getPendingConversations() {
        System.out.println("reached the pedning conversations");
        return conversationService.getPendingConversations();
    }

    @GetMapping("/{conversationId}/getSingleConversation")
    public ResponseEntity<ConversationDto> getSingleConversation(@PathVariable Long conversationId) {
        return this.conversationService.getSingleConversation(conversationId);
    }

    @PostMapping("/{conversationId}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long conversationId) {
        SeenDto seen = this.conversationService.markAsReadAndReturnSeen(conversationId);
        messagingTemplate.convertAndSendToUser(seen.getOtherUserId().toString(), "/queue/seen",
                new WSEvent("DM_SEEN", seen));
        System.out.println("--------------------------------------");
        System.out.println("--------------------------------------message seen------------------------------");
        System.out.println("--------------------------------------");
        return ResponseEntity.noContent().build();
    }
}
