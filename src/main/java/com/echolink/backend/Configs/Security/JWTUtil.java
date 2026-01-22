package com.echolink.backend.Configs.Security;

import java.util.Date;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;

@Component
public class JWTUtil {
    private static final String SECRET_KEY = "krish_kabish";
    private static final long EXPIRATION_TIME = 1000 * 60 * 60 * 10;

    public static String generateToken(UserDetails userDetails) {
        return JWT.create().withSubject(userDetails.getUsername()).withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .sign(Algorithm.HMAC256(SECRET_KEY));
    }

    public static DecodedJWT decodeJWT(String token) {
        JWTVerifier verifier = JWT.require(Algorithm.HMAC256(SECRET_KEY)).build();
        return verifier.verify(token);
    }
}
