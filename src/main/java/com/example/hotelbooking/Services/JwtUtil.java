package com.example.hotelbooking.Services;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class JwtUtil {

    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.expiration}")
    private Long expiration;

    public String generateToken(UserDetails userDetails){
        Map<String, Object> claims = new HashMap<>();
        return buildToken(claims, userDetails.getUsername());
    }

    private String buildToken(Map<String, Object> claims, String subject){

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date())
                .setExpiration((new Date(System.currentTimeMillis() + expiration)))
                .signWith(SignatureAlgorithm.HS256, secret)
                .compact();
    }

    public boolean validateToken (String token, UserDetails userDetails){

        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isExpiredToken(token);
    }

    public String extractUsername(String token){

        return extractClaims(token, Claims::getSubject);
    }

    private <T> T extractClaims (String token, Function<Claims, T> resolver){

        return resolver.apply(parseToken(token));
    }

    private Claims parseToken(String token){

        return Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
    }

    private boolean isExpiredToken(String token){

        return extractClaims(token, Claims::getExpiration).before(new Date());
    }


}
