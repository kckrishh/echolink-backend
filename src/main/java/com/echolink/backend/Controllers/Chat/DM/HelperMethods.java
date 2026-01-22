package com.echolink.backend.Controllers.Chat.DM;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import com.echolink.backend.Configs.Security.CustomUserDetails;
import com.echolink.backend.Entities.DMConversation;
import com.echolink.backend.Entities.DMParticipant;
import com.echolink.backend.Entities.User;
import com.echolink.backend.Enums.ParticipantStatus;
import com.echolink.backend.Enums.SignUpState;
import com.echolink.backend.Repo.DMConversationRepo;
import com.echolink.backend.Repo.DMParticipantRepo;

@Component
public class HelperMethods {

    private final DMConversationRepo conversationRepo;
    private final DMParticipantRepo participantRepo;

    public HelperMethods(DMConversationRepo conversationRepo, DMParticipantRepo participantRepo) {
        this.conversationRepo = conversationRepo;
        this.participantRepo = participantRepo;
    }

    public User getCurrentUser() {
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication()
                .getPrincipal();

        return userDetails.getUser();
    }

    public String getPairKeyOfDMConversation(Long currentUser, Long targetUser) {
        Long minId = Math.min(currentUser, targetUser);
        Long maxId = Math.max(currentUser, targetUser);
        return minId + "_" + maxId;
    }

    public DMConversation createDMConversation(String pairKey) {
        DMConversation conversation = new DMConversation();
        conversation.setPairKey(pairKey);
        conversation = conversationRepo.save(conversation);
        return conversation;
    }

    public DMParticipant createDMParticipant(DMConversation conversation, User user) {
        DMParticipant participant = new DMParticipant();
        participant.setConversation(conversation);
        participant.setUser(user);
        participant.setStatus(ParticipantStatus.NEUTRAL);
        participant.setMuted(false);
        participant.setBlocked(false);
        participant = participantRepo.save(participant);
        return participant;
    }

    public void requireCompleteUser(User user) {
        if (user.getSignUpState() != SignUpState.COMPLETE) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Complete signup before using this feature");
        }
    }

}
