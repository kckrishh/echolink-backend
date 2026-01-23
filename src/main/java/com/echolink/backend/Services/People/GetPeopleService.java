package com.echolink.backend.Services.People;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.echolink.backend.Controllers.Chat.DM.HelperMethods;
import com.echolink.backend.Dtos.DM.Conversation.ConversationRequestDto;
import com.echolink.backend.Dtos.DM.Conversation.ConversationResponseDto;
import com.echolink.backend.Dtos.People.SearchPeopleResponseDto;
import com.echolink.backend.Entities.DMConversation;
import com.echolink.backend.Entities.DMParticipant;
import com.echolink.backend.Entities.User;
import com.echolink.backend.Enums.ParticipantStatus;
import com.echolink.backend.Repo.DMConversationRepo;
import com.echolink.backend.Repo.DMParticipantRepo;
import com.echolink.backend.Repo.UserRepo;
import com.echolink.backend.Services.Chat.DM.DMConversationService;

import jakarta.transaction.Transactional;

@Service
public class GetPeopleService {
    private final UserRepo userRepo;
    private final HelperMethods helperMethods;
    private final DMParticipantRepo dmParticipantRepo;
    private final DMConversationRepo conversationRepo;
    private final DMConversationService conversationService;

    public GetPeopleService(UserRepo userRepo, HelperMethods helperMethods, DMConversationRepo conversationRepo,
            DMConversationService conversationService, DMParticipantRepo dmParticipantRepo) {
        this.userRepo = userRepo;
        this.helperMethods = helperMethods;
        this.conversationRepo = conversationRepo;
        this.conversationService = conversationService;
        this.dmParticipantRepo = dmParticipantRepo;
    }

    @Transactional
    public ResponseEntity<SearchPeopleResponseDto> search(String query) {
        User currentUser = helperMethods.getCurrentUser();
        helperMethods.requireCompleteUser(currentUser);

        if (currentUser == null) {
            System.out.println("current user not found");
        }

        User otherUser = userRepo.findByUsername(query);
        if (otherUser == null) {
            System.out.println("Other user not found");
        }

        Long currentUserId = currentUser.getId();
        Long otherUserId = otherUser.getId();

        SearchPeopleResponseDto searchPeopleResponseDto = new SearchPeopleResponseDto();
        searchPeopleResponseDto.setUsername(otherUser.getUsername());
        searchPeopleResponseDto.setCreatedAt(otherUser.getCreatedAt());
        searchPeopleResponseDto.setBio(otherUser.getBio());
        searchPeopleResponseDto.setAvatar(otherUser.getAvatar());

        String pairKey = helperMethods.getPairKeyOfDMConversation(currentUserId, otherUserId);
        DMConversation dmConversation = conversationRepo.findByPairKey(pairKey);

        if (dmConversation == null) {
            ResponseEntity<ConversationResponseDto> dto = conversationService
                    .startConversation(new ConversationRequestDto(query));
            searchPeopleResponseDto.setConversationId(dto.getBody().getConversationId());
            dmConversation = conversationRepo.findById(dto.getBody().getConversationId()).orElseThrow();
        } else {
            searchPeopleResponseDto.setConversationId(dmConversation.getId());
        }

        List<DMParticipant> participants = dmConversation.getDmParticipants();
        for (DMParticipant participant : participants) {
            if (participant.getUser().getUsername().equals(currentUser.getUsername())) {
                searchPeopleResponseDto.setStatus(participant.getStatus());
                break;
            }
        }
        System.out.println(searchPeopleResponseDto);

        return ResponseEntity.ok(searchPeopleResponseDto);
    }

    public ResponseEntity<List<SearchPeopleResponseDto>> getFriends() {
        User currentUser = helperMethods.getCurrentUser();
        helperMethods.requireCompleteUser(currentUser);

        List<DMParticipant> myParticipants = this.dmParticipantRepo.findAllByUser(currentUser);
        List<SearchPeopleResponseDto> result = new ArrayList<>();
        for (DMParticipant myParticipant : myParticipants) {
            if (!myParticipant.getStatus().equals(ParticipantStatus.ACTIVE)) {
                continue;
            }
            DMConversation currConversation = myParticipant.getConversation();
            List<DMParticipant> participantsInThisConversation = currConversation.getDmParticipants();
            User friendUser = null;
            for (DMParticipant p : participantsInThisConversation) {
                if (!p.getUser().equals(currentUser) && p.getStatus().equals(ParticipantStatus.ACTIVE)) {
                    friendUser = p.getUser();
                    SearchPeopleResponseDto dto = new SearchPeopleResponseDto();
                    dto.setUsername(friendUser.getUsername());
                    dto.setCreatedAt(friendUser.getCreatedAt());
                    dto.setConversationId(currConversation.getId());
                    dto.setBio(friendUser.getBio());
                    dto.setAvatar(friendUser.getAvatar());
                    result.add(dto);
                    break;
                }
            }
        }

        return ResponseEntity.ok(result);
    }
}
