package com.echolink.backend.Services.Chat.DM;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.boot.model.TypeDefinitionRegistryStandardImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.echolink.backend.Controllers.Chat.DM.HelperMethods;
import com.echolink.backend.Dtos.DM.Message.MessageDto;
import com.echolink.backend.Dtos.DM.Message.MessageRequestDto;
import com.echolink.backend.Dtos.DM.Message.SendMessageResult;
import com.echolink.backend.Dtos.DM.Message.TypingDto;
import com.echolink.backend.Dtos.DM.Message.TypingResult;
import com.echolink.backend.Entities.DMConversation;
import com.echolink.backend.Entities.DMMessage;
import com.echolink.backend.Entities.DMParticipant;
import com.echolink.backend.Entities.User;
import com.echolink.backend.Enums.MessageStatus;
import com.echolink.backend.Enums.ParticipantStatus;
import com.echolink.backend.Repo.DMConversationRepo;
import com.echolink.backend.Repo.DMMessageRepo;
import com.echolink.backend.Repo.DMParticipantRepo;
import com.echolink.backend.Repo.UserRepo;

import jakarta.transaction.Transactional;

@Service
public class DMMessageService {

    private final UserRepo userRepo;
    private final DMConversationRepo conversationRepo;
    private final DMParticipantRepo participantRepo;
    private final DMMessageRepo messageRepo;
    private final HelperMethods helperMethods;

    // private final HelperMethods helperMethods = new Help

    public DMMessageService(UserRepo userRepo, DMConversationRepo conversationRepo, DMParticipantRepo participantRepo,
            DMMessageRepo messageRepo, HelperMethods helperMethods) {
        this.userRepo = userRepo;
        this.conversationRepo = conversationRepo;
        this.participantRepo = participantRepo;
        this.messageRepo = messageRepo;
        this.helperMethods = helperMethods;
    }

    @Transactional
    public SendMessageResult sendMessage(MessageRequestDto messageRequestDto, Principal principal) throws Exception {
        Long currentUserId = Long.parseLong(principal.getName());
        User currentUser = userRepo.findById(currentUserId)
                .orElseThrow(() -> new Exception("user not found"));
        helperMethods.requireCompleteUser(currentUser);

        String targetUsername = messageRequestDto.getTargetUsername();
        User targetUser = userRepo.findByUsername(targetUsername);

        if (targetUser == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Target user not found");
        }

        if (currentUser.getId().equals(targetUser.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You cannot start a DM with yourself");
        }
        // Long currentUserId = currentUser.getId();
        Long targetUserId = targetUser.getId();

        String pairKey = helperMethods.getPairKeyOfDMConversation(currentUserId, targetUserId);

        DMConversation conversation = conversationRepo.findByPairKey(pairKey);

        if (conversation == null) {
            conversation = helperMethods.createDMConversation(pairKey);
        }

        DMParticipant currentParticipant = participantRepo.findByConversationAndUser(conversation, currentUser);

        if (currentParticipant == null) {
            currentParticipant = helperMethods.createDMParticipant(conversation, currentUser);
        }
        DMParticipant targetParticipant = participantRepo.findByConversationAndUser(conversation, targetUser);

        if (targetParticipant == null) {
            targetParticipant = helperMethods.createDMParticipant(conversation, targetUser);
        }

        targetParticipant.setUnreadCount(targetParticipant.getUnreadCount() + 1);
        boolean isMessageRequest = false;

        if (currentParticipant.getStatus() == ParticipantStatus.NEUTRAL
                && targetParticipant.getStatus() == ParticipantStatus.NEUTRAL) {
            isMessageRequest = true;
            currentParticipant.setStatus(ParticipantStatus.ACTIVE);
            targetParticipant.setStatus(ParticipantStatus.PENDING);
        } else if (currentParticipant.getStatus() == ParticipantStatus.PENDING
                && targetParticipant.getStatus() == ParticipantStatus.ACTIVE) {
            currentParticipant.setStatus(ParticipantStatus.ACTIVE);
        }

        participantRepo.save(currentParticipant);
        participantRepo.save(targetParticipant);
        DMMessage message = new DMMessage();
        message.setContent(messageRequestDto.getText());
        message.setConversation(conversation);
        message.setSentBy(currentUser);
        messageRepo.save(message);

        conversation.setLastMessagePreview(message.getContent());
        conversation.setLastMessageAt(message.getCreatedAt());
        conversation.setLastMessageSenderId(currentUserId);
        conversation.setLastMessageId(message.getId());
        conversationRepo.save(conversation);

        MessageDto messageDto = new MessageDto();
        messageDto.setMessageId(message.getId());
        messageDto.setConversationId(conversation.getId());
        messageDto.setContent(message.getContent());
        messageDto.setSenderId(currentUserId);
        messageDto.setSenderUsername(currentUser.getUsername());
        messageDto.setSenderAvatar(currentUser.getAvatar());
        ;

        SendMessageResult sendMessageResult = new SendMessageResult(messageDto, isMessageRequest);
        return sendMessageResult;
    }

    @Transactional
    public ResponseEntity<List<MessageDto>> getMessages(Long conversationId) {
        DMConversation conversation = conversationRepo.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found to load message"));

        boolean isParticipant = participantRepo.existsByConversationAndUser(conversation,
                helperMethods.getCurrentUser());

        if (!isParticipant) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not allowed to view this conversation");
        }

        List<DMMessage> messages = conversation.getDmMessages();

        List<MessageDto> result = new ArrayList<>();

        for (DMMessage message : messages) {
            MessageDto dto = new MessageDto();
            dto.setContent(message.getContent());
            dto.setConversationId(conversation.getId());
            dto.setCreatedAt(message.getCreatedAt());
            dto.setMessageId(message.getId());
            dto.setSenderId(message.getSentBy().getId());
            dto.setSenderUsername(message.getSentBy().getUsername());
            dto.setSenderAvatar(message.getSentBy().getAvatar());

            result.add(dto);
        }
        return ResponseEntity.ok(result);

    }

    @Transactional
    public TypingResult typing(TypingDto requestDto, Principal principal) {
        DMConversation conversation = conversationRepo.findById(requestDto.getConversationId())
                .orElseThrow(() -> new RuntimeException("Convo not found"));

        Long currentUserId = Long.parseLong(principal.getName());
        List<DMParticipant> participants = conversation.getDmParticipants();
        User otherUser = null;

        for (DMParticipant participant : participants) {
            if (!participant.getUser().getId().equals(currentUserId)) {
                otherUser = participant.getUser();
            }
        }
        TypingResult result = new TypingResult();
        result.setDto(requestDto);
        result.setOtherUserId(otherUser.getId());
        return result;

    }

}
