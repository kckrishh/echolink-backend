package com.echolink.backend.Controllers.Security;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.echolink.backend.Controllers.Chat.DM.HelperMethods;
import com.echolink.backend.Entities.User;
import com.echolink.backend.Repo.UserRepo;

@RequestMapping("/auth")
@RestController
public class ChangeProfile {
    private final UserRepo userRepo;
    private final HelperMethods helperMethods;

    public ChangeProfile(UserRepo userRepo, HelperMethods helperMethods) {
        this.userRepo = userRepo;
        this.helperMethods = helperMethods;
    }

    @PostMapping("/changeAvatar")
    public void changeAvatar(String newAvatarUrl) {
        User user = this.helperMethods.getCurrentUser();
        User currentUser = this.userRepo.findByEmail(user.getEmail());
        currentUser.setAvatar(newAvatarUrl);
        userRepo.save(currentUser);
    }
}
