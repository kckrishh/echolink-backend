package com.echolink.backend.Services.Chat.DM;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.server.ResponseStatusException;

import com.echolink.backend.Controllers.Chat.DM.HelperMethods;
import com.echolink.backend.Dtos.DM.Conversation.ConversationDto;
import com.echolink.backend.Dtos.DM.Conversation.ConversationRequestDto;
import com.echolink.backend.Dtos.DM.Conversation.ConversationResponseDto;
import com.echolink.backend.Dtos.DM.Message.SeenDto;
import com.echolink.backend.Entities.DMConversation;
import com.echolink.backend.Entities.DMParticipant;
import com.echolink.backend.Entities.User;
import com.echolink.backend.Enums.MessageStatus;
import com.echolink.backend.Enums.ParticipantStatus;
import com.echolink.backend.Repo.DMConversationRepo;
import com.echolink.backend.Repo.DMParticipantRepo;
import com.echolink.backend.Repo.UserRepo;

@Service
public class DMConversationService {
    private final UserRepo userRepo;
    private final DMConversationRepo conversationRepo;
    private final DMParticipantRepo participantRepo;

    private final HelperMethods helperMethods;

    public DMConversationService(UserRepo userRepo, DMConversationRepo conversationRepo,
            DMParticipantRepo participantRepo,
            HelperMethods helperMethods) {
        this.userRepo = userRepo;
        this.conversationRepo = conversationRepo;
        this.participantRepo = participantRepo;
        this.helperMethods = helperMethods;
    }

    public ResponseEntity<ConversationResponseDto> startConversation(@RequestBody ConversationRequestDto requestDto) {
        User currentUser = helperMethods.getCurrentUser();
        helperMethods.requireCompleteUser(currentUser);

        User targetUser = userRepo.findByUsername(requestDto.getUsername());
        if (targetUser == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Target user not found");
        }

        if (currentUser.getId().equals(targetUser.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You cannot start a DM with yourself");
        }

        Long currentId = currentUser.getId();
        Long targetId = targetUser.getId();
        String pairKey = helperMethods.getPairKeyOfDMConversation(currentId, targetId);

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

        ConversationResponseDto response = new ConversationResponseDto();
        response.setConversationId(conversation.getId());
        response.setPairKey(conversation.getPairKey());
        response.setLastMessagePreview(conversation.getLastMessagePreview());
        response.setTarget_username(targetUser.getUsername());
        response.setTarget_userAvatar(targetUser.getAvatar());

        return ResponseEntity.ok(response);
    }

    public ResponseEntity<List<ConversationDto>> getConversations() {
        User currentUser = helperMethods.getCurrentUser();

        helperMethods.requireCompleteUser(currentUser);

        List<DMParticipant> myParticipantRows = participantRepo.findForConversationList(currentUser,
                List.of(ParticipantStatus.PENDING, ParticipantStatus.NEUTRAL, ParticipantStatus.BLOCKED));

        List<ConversationDto> result = new ArrayList<>();

        for (DMParticipant myParticipant : myParticipantRows) {
            DMConversation conversation = myParticipant.getConversation();
            ParticipantStatus status = myParticipant.getStatus();

            List<DMParticipant> participantInThisConversation = conversation.getDmParticipants();

            User otherUser = null;
            for (DMParticipant p : participantInThisConversation) {
                if (!p.getUser().getId().equals(currentUser.getId())) {
                    otherUser = p.getUser();
                    break;
                }
            }

            ConversationDto dto = new ConversationDto();
            dto.setConversationId(conversation.getId());
            dto.setOtherUserId(otherUser.getId());
            dto.setOtherUsername(otherUser.getUsername());
            dto.setOtherUserAvatar(otherUser.getAvatar());
            dto.setLastMessagePreview(conversation.getLastMessagePreview());
            dto.setLastMessageAt(conversation.getLastMessageAt());
            dto.setStatus(status);
            dto.setUnreadCount(myParticipant.getUnreadCount());
            dto.setLastReadAt(myParticipant.getLastReadAt());

            result.add(dto);
        }
        return ResponseEntity.ok(result);
    }

    public ResponseEntity<List<ConversationDto>> getPendingConversations() {
        User currentUser = helperMethods.getCurrentUser();
        helperMethods.requireCompleteUser(currentUser);

        List<DMParticipant> myParticipantRows = participantRepo.findForConversationList(currentUser,
                List.of(ParticipantStatus.ACTIVE, ParticipantStatus.NEUTRAL, ParticipantStatus.BLOCKED));
        List<ConversationDto> result = new ArrayList<>();

        for (DMParticipant myParticipant : myParticipantRows) {
            DMConversation conversation = myParticipant.getConversation();
            ParticipantStatus status = myParticipant.getStatus();

            if (conversation.getLastMessagePreview() == null || conversation.getLastMessageAt() == null) {
                continue;
            }
            List<DMParticipant> participantInThisConversation = conversation.getDmParticipants();
            User otherUser = null;
            for (DMParticipant p : participantInThisConversation) {
                if (!p.getUser().getId().equals(currentUser.getId())) {
                    otherUser = p.getUser();
                    break;
                }
            }
            ConversationDto dto = new ConversationDto();

            dto.setConversationId(conversation.getId());
            dto.setOtherUserId(otherUser.getId());
            dto.setOtherUsername(otherUser.getUsername());
            dto.setOtherUserAvatar(otherUser.getAvatar());
            dto.setLastMessagePreview(conversation.getLastMessagePreview());
            dto.setStatus(status);
            dto.setLastMessageAt(conversation.getLastMessageAt());

            result.add(dto);
        }
        return ResponseEntity.ok(result);
    }

    public ResponseEntity<ConversationDto> getSingleConversation(Long id) {
        User currentUser = helperMethods.getCurrentUser();
        helperMethods.requireCompleteUser(currentUser);

        DMConversation dmConversation = this.conversationRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));
        // return ResponseEntity.ok(dmConversation);
        List<DMParticipant> dmParticipants = dmConversation.getDmParticipants();
        ConversationDto conversationDto = new ConversationDto();
        conversationDto.setConversationId(dmConversation.getId());
        conversationDto.setLastMessageAt(dmConversation.getLastMessageAt());
        conversationDto.setLastMessagePreview(dmConversation.getLastMessagePreview());
        for (DMParticipant dmParticipant : dmParticipants) {
            if (!dmParticipant.getUser().equals(currentUser)) {
                conversationDto.setOtherUserId(dmParticipant.getUser().getId());
                conversationDto.setOtherUsername(dmParticipant.getUser().getUsername());
                conversationDto.setOtherUserAvatar(dmParticipant.getUser().getAvatar());
            } else {
                conversationDto.setStatus(dmParticipant.getStatus());
            }
        }
        return ResponseEntity.ok(conversationDto);
    }

    public SeenDto markAsReadAndReturnSeen(Long conversationId) {
        User currentUser = helperMethods.getCurrentUser();
        helperMethods.requireCompleteUser(currentUser);

        DMConversation conversation = conversationRepo.findById(conversationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        DMParticipant participant = participantRepo.findByConversationAndUser(conversation, currentUser);

        if (participant == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Participant not found");
        }

        participant.setUnreadCount(0);
        participant.setLastReadAt(LocalDateTime.now());
        participantRepo.save(participant);

        Long lastMessageId = conversation.getLastMessageId();

        User otherUser = null;
        for (DMParticipant p : conversation.getDmParticipants()) {
            if (!p.getUser().getId().equals(currentUser.getId())) {
                otherUser = p.getUser();
                break;
            }
        }

        SeenDto seen = new SeenDto();
        seen.setConversationId(conversationId);
        seen.setLastReadMessageId(lastMessageId);
        seen.setSeenByUserId(currentUser.getId());
        seen.setOtherUserId(otherUser.getId());

        return seen;

    }

}
