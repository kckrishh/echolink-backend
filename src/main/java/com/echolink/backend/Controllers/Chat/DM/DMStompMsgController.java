package com.echolink.backend.Controllers.Chat.DM;

import java.security.Principal;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.echolink.backend.Dtos.DM.Message.MessageDto;
import com.echolink.backend.Dtos.DM.Message.MessageRequestDto;
import com.echolink.backend.Dtos.DM.Message.SendMessageResult;
import com.echolink.backend.Dtos.DM.Message.TypingDto;
import com.echolink.backend.Dtos.DM.Message.TypingResult;
import com.echolink.backend.Entities.User;
import com.echolink.backend.Entities.WSEvent;
import com.echolink.backend.Repo.UserRepo;
import com.echolink.backend.Services.Chat.DM.DMMessageService;

@Controller
public class DMStompMsgController {

    private final DMMessageService messageService;
    private final SimpMessagingTemplate messagingTemplate;
    private final UserRepo userRepo;

    public DMStompMsgController(DMMessageService messageService, SimpMessagingTemplate messagingTemplate,
            UserRepo userRepo) {
        this.messageService = messageService;
        this.messagingTemplate = messagingTemplate;
        this.userRepo = userRepo;
    }

    @MessageMapping("/chat.sendMessage")
    public void sendMessage(MessageRequestDto messageRequestDto, Principal principal) throws Exception {
        SendMessageResult result = this.messageService.sendMessage(messageRequestDto, principal);
        User targetUser = this.userRepo.findByUsername(messageRequestDto.getTargetUsername());
        String eventType = result.isRequest() ? "DM_REQUEST" : "DM_MESSAGE";

        this.messagingTemplate.convertAndSendToUser(targetUser.getId().toString(), "/queue/messages",
                new WSEvent<>(eventType, result.getMessage()));
        this.messagingTemplate.convertAndSendToUser(result.getMessage().getSenderId().toString(), "/queue/messages",
                new WSEvent<>(eventType, result.getMessage()));
    }

    @MessageMapping("/chat.typing")
    public void typing(TypingDto requestDto, Principal principal) {
        TypingResult result = messageService.typing(requestDto, principal);
        TypingDto response = result.getDto();
        this.messagingTemplate.convertAndSendToUser(result.getOtherUserId().toString(), "/queue/typing",
                new WSEvent<>("DM_TYPING", response));
    }
}
