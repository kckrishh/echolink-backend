package com.echolink.backend.Repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.echolink.backend.Entities.DMMessage;
import java.util.List;
import com.echolink.backend.Entities.DMConversation;

public interface DMMessageRepo extends JpaRepository<DMMessage, Long> {
    List<DMMessage> findAllByConversation(DMConversation conversation);
}
