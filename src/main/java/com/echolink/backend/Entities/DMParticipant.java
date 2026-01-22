package com.echolink.backend.Entities;

import java.time.LocalDateTime;

import com.echolink.backend.Enums.MessageStatus;
import com.echolink.backend.Enums.ParticipantStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;

@Entity
@Data
public class DMParticipant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn
    private DMConversation conversation;

    @ManyToOne
    @JoinColumn
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ParticipantStatus status;

    private boolean muted;

    private boolean blocked;

    private int unreadCount = 0;

    private LocalDateTime lastReadAt;

    private Long lastReadMessageId;

}
