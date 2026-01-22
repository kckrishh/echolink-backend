package com.echolink.backend.Repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.echolink.backend.Entities.DMConversation;
import com.echolink.backend.Entities.DMParticipant;
import com.echolink.backend.Entities.User;
import com.echolink.backend.Enums.ParticipantStatus;

import java.util.List;

public interface DMParticipantRepo extends JpaRepository<DMParticipant, Long> {
    DMParticipant findByUserAndConversation(User user, DMConversation conversation);

    DMParticipant findByConversationAndUser(DMConversation conversation, User user);

    List<DMParticipant> findAllByUser(User user);

    boolean existsByConversationAndUser(DMConversation conversation, User user);

    @Query("""
              SELECT p
              FROM DMParticipant p
              JOIN p.conversation c
              WHERE p.user = :user
                AND p.status NOT IN :skip
              ORDER BY c.lastMessageAt DESC NULLS LAST, c.createdAt DESC
            """)
    List<DMParticipant> findForConversationList(
            @Param("user") User user,
            @Param("skip") List<ParticipantStatus> skip);

}
