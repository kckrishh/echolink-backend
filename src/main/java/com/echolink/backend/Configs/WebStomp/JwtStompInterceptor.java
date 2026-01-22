package com.echolink.backend.Configs.WebStomp;

import java.security.Principal;
import java.util.Optional;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.echolink.backend.Configs.Security.JWTUtil;
import com.echolink.backend.Entities.User;
import com.echolink.backend.Repo.UserRepo;

public class JwtStompInterceptor implements ChannelInterceptor {
    private final UserRepo userRepo;

    public JwtStompInterceptor(UserRepo userRepo) {
        this.userRepo = userRepo;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authHeader = accessor.getFirstNativeHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                System.out.println("Missing or invalid Authorization header");
                throw new IllegalArgumentException("Missing or invalid Authorization header");
            }
            try {

                String token = authHeader.substring(7);
                DecodedJWT decodedJWT = JWTUtil.decodeJWT(token);
                String email = decodedJWT.getSubject();
                if (email == null) {
                    throw new IllegalArgumentException("Token subject is null");
                }
                if (email != null) {
                    User user = userRepo.findByEmail(email);
                    if (user == null) {
                        throw new RuntimeException("User not found for WS");
                    }
                    Principal wsPrincipal = () -> user.getId().toString();
                    accessor.setUser(wsPrincipal);
                }
            } catch (Exception e) {
                System.err.println("JWT validation failed: " + e.getMessage());
                e.printStackTrace();
                throw new IllegalArgumentException("Invalid JWT token: " + e.getMessage());
            }
        }
        return message;
    }
}