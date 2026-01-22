package com.echolink.backend.Repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.echolink.backend.Entities.User;

public interface UserRepo extends JpaRepository<User, Long> {
    User findByEmail(String email);

    User findByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);
}
