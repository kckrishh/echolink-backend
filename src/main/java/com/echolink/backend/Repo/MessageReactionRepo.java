package com.echolink.backend.Repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.echolink.backend.Entities.MessageReaction;

public interface MessageReactionRepo extends JpaRepository<MessageReaction, Long> {
    Optional<MessageReaction> findByMessage_IdAndReactedBy_Id(Long messageId, Long reactedById);

    List<MessageReaction> findByMessage_Conversation_Id(Long conversationId);
}
