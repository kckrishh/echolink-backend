package com.echolink.backend.Entities;

import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;

import com.echolink.backend.Enums.MessageStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.Data;

@Entity
@Data
public class DMConversation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String pairKey;

    @CreationTimestamp
    private LocalDateTime createdAt;

    private LocalDateTime lastMessageAt;

    private String lastMessagePreview;

    private Long lastMessageId;

    private Long lastMessageSenderId;

    @OneToMany(mappedBy = "conversation")
    private List<DMParticipant> dmParticipants;

    @OneToMany(mappedBy = "conversation")
    private List<DMMessage> dmMessages;

}
