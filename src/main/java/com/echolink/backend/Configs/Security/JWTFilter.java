package com.echolink.backend.Configs.Security;

import java.io.IOException;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.auth0.jwt.interfaces.DecodedJWT;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JWTFilter extends OncePerRequestFilter {
    private final String HEADER = "Authorization";
    private final CustomUserDetailsManager userDetailsManager;

    public JWTFilter(CustomUserDetailsManager userDetailsManager) {
        this.userDetailsManager = userDetailsManager;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String token = request.getHeader(HEADER);
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);

            try {
                DecodedJWT decodedJWT = JWTUtil.decodeJWT(token);
                String username = decodedJWT.getSubject();

                if (username != null) {
                    UserDetails userDetails = userDetailsManager.loadUserByUsername(username);

                    if (userDetails == null) {
                        throw new BadCredentialsException("User not found, please log in again");
                    }
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } else {
                    throw new BadCredentialsException("Invalid token, username not found");
                }
            } catch (Exception e) {
                System.out.println("JWT validation failed: " + e.getMessage());
            }
        }
        filterChain.doFilter(request, response);
    }
}
