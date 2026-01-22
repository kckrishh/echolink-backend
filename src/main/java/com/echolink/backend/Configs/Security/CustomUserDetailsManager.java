package com.echolink.backend.Configs.Security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.UserDetailsManager;

import com.echolink.backend.Entities.User;
import com.echolink.backend.Repo.UserRepo;

@Configuration
public class CustomUserDetailsManager implements UserDetailsManager {

    private final UserRepo userRepo;

    public CustomUserDetailsManager(UserRepo userRepo) {
        this.userRepo = userRepo;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = this.userRepo.findByEmail(username);
        CustomUserDetails userDetails = new CustomUserDetails(user);
        if (user == null) {
            throw new UsernameNotFoundException("User not found: " + username);
        }
        return userDetails;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return NoOpPasswordEncoder.getInstance();
    }

    @Override
    public void changePassword(String oldPassword, String newPassword) {
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication()
                .getPrincipal();
        Long userId = userDetails.getUser().getId();
        User currentUser = userRepo.findById(userId).orElseThrow(() -> new UsernameNotFoundException(newPassword));

        if (!currentUser.getPassword().equals(oldPassword)) {
            throw new IllegalArgumentException("Old password does not match");
        }
        if (currentUser.getPassword().equals(newPassword)) {
            throw new IllegalArgumentException("New password must be different");
        }

        if (newPassword == null || newPassword.isBlank()) {
            throw new IllegalArgumentException("Password fields cannot be empty");
        }
        currentUser.setPassword(newPassword);
        userRepo.save(currentUser);
    }

    @Override
    public void createUser(UserDetails user) {
        CustomUserDetails userDetails = (CustomUserDetails) user;
        User u = userDetails.getUser();

        if (u.getEmail() == null || u.getEmail().isBlank()
                || u.getUsername() == null || u.getUsername().isBlank()
                || u.getPassword() == null || u.getPassword().isBlank()) {
            throw new IllegalArgumentException("Missing fields");
        }

        if (userRepo.existsByEmail(u.getEmail()))
            throw new IllegalArgumentException("Email taken");
        if (userRepo.existsByUsername(u.getUsername()))
            throw new IllegalArgumentException("Username taken");
        userRepo.save(u);
    }

    @Override
    public void deleteUser(String username) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deleteUser'");
    }

    @Override
    public void updateUser(UserDetails user) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'updateUser'");
    }

    @Override
    public boolean userExists(String username) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'userExists'");
    }

}
