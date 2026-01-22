package com.echolink.backend.Controllers.People;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.echolink.backend.Dtos.People.SearchPeopleResponseDto;
import com.echolink.backend.Services.People.GetPeopleService;

@RestController
@RequestMapping("/people")
public class GetPeople {

    private final GetPeopleService getPeopleService;

    public GetPeople(GetPeopleService getPeopleService) {
        this.getPeopleService = getPeopleService;
    }

    @GetMapping("/search")
    public ResponseEntity<SearchPeopleResponseDto> searchPeople(@RequestParam String query) {
        return getPeopleService.search(query);
    }

    @GetMapping("/getFriends")
    public ResponseEntity<List<SearchPeopleResponseDto>> getFriends() {
        return getPeopleService.getFriends();
    }
}
