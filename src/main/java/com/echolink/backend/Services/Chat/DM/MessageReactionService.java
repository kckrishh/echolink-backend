package com.echolink.backend.Services.Chat.DM;

import java.security.Principal;
import java.util.Optional;

import org.hibernate.boot.model.source.internal.hbm.Helper;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.echolink.backend.Controllers.Chat.DM.HelperMethods;
import com.echolink.backend.Dtos.DM.Message.MessageReactionWrapper;
import com.echolink.backend.Dtos.DM.Message.ReactionRequestDto;
import com.echolink.backend.Dtos.DM.Message.ReactionResponseDto;
import com.echolink.backend.Entities.DMMessage;
import com.echolink.backend.Entities.MessageReaction;
import com.echolink.backend.Entities.User;
import com.echolink.backend.Enums.ReactionType;
import com.echolink.backend.Repo.DMMessageRepo;
import com.echolink.backend.Repo.MessageReactionRepo;
import com.echolink.backend.Repo.UserRepo;

import jakarta.transaction.Transactional;

@Service
public class MessageReactionService {

    private final HelperMethods helperMethods;
    private final DMMessageRepo messageRepo;
    private final MessageReactionRepo reactionRepo;
    private final UserRepo userRepo;

    public MessageReactionService(HelperMethods helperMethods, DMMessageRepo messageRepo,
            MessageReactionRepo reactionRepo, UserRepo userRepo) {
        this.helperMethods = helperMethods;
        this.messageRepo = messageRepo;
        this.reactionRepo = reactionRepo;
        this.userRepo = userRepo;
    }

    @Transactional
    public MessageReactionWrapper toggleReaction(ReactionRequestDto dto, Principal principal) {
        Long currUserId = Long.parseLong(principal.getName());
        User currUser = userRepo.findById(currUserId).orElseThrow(() -> new RuntimeException("User not found"));
        DMMessage reactedMessage = messageRepo.findById(dto.getMessageId())
                .orElseThrow(() -> new RuntimeException("Message not found"));
        Long conversationid = reactedMessage.getConversation().getId();
        Long otherUserId = reactedMessage.getConversation().getDmParticipants().stream()
                .map(p -> p.getUser().getId())
                .filter(id -> !id.equals(currUserId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Other user not found in conversation"));

        Optional<MessageReaction> existingOpt = reactionRepo.findByMessage_IdAndReactedBy_Id(reactedMessage.getId(),
                currUserId);
        String action;
        ReactionType finalType = dto.getType();

        if (existingOpt.isEmpty()) {
            MessageReaction newReaction = new MessageReaction();
            newReaction.setMessage(reactedMessage);
            newReaction.setReactedBy(currUser);
            newReaction.setReactionType(dto.getType());

            reactionRepo.save(newReaction);
            action = "ADDED";
        } else {
            MessageReaction existing = existingOpt.get();

            if (existing.getReactionType() == dto.getType()) {
                reactionRepo.delete(existing);
                action = "REMOVED";
                finalType = null;

            } else {
                // ✅ CHANGE
                existing.setReactionType(dto.getType());
                action = "CHANGED";
                reactionRepo.save(existing);
            }
        }
        ReactionResponseDto res = new ReactionResponseDto();
        res.setMessageId(reactedMessage.getId());
        res.setConversationId(reactedMessage.getConversation().getId());
        res.setReactedBy(currUserId);
        res.setType(finalType);
        res.setAction(action);

        MessageReactionWrapper wrapper = new MessageReactionWrapper(res, otherUserId);

        return wrapper;
    }
}
