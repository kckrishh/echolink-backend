package com.echolink.backend.Repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.echolink.backend.Entities.DMConversation;

public interface DMConversationRepo extends JpaRepository<DMConversation, Long> {
    DMConversation findByPairKey(String pairKey);

}