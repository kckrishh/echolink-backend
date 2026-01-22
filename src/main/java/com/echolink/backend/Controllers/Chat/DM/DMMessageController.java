package com.echolink.backend.Controllers.Chat.DM;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.echolink.backend.Dtos.DM.Message.MessageDto;
import com.echolink.backend.Repo.UserRepo;
import com.echolink.backend.Services.Chat.DM.DMMessageService;

@RestController
@RequestMapping("/dm")
public class DMMessageController {

    private DMMessageService messageService;

    public DMMessageController(DMMessageService messageService) {
        this.messageService = messageService;
    }

    @GetMapping("/{conversationId}/messages")
    public ResponseEntity<List<MessageDto>> getMessages(@PathVariable Long conversationId) {
        System.out.println("reached the get MESSAGES");
        return this.messageService.getMessages(conversationId);
    }
}
