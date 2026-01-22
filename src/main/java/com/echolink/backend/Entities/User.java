package com.echolink.backend.Entities;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import com.echolink.backend.Enums.SignUpState;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "Users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @CreationTimestamp
    private LocalDateTime createdAt;

    private String bio;

    private String avatar;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SignUpState signUpState;

}
