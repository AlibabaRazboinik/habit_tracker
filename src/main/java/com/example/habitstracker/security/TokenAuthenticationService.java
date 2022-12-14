package com.example.habitstracker.security;

import java.security.Key;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.example.habitstracker.constants.JWTClaims;
import com.example.habitstracker.exceptions.auth.EmptyAuthorizationHeaderException;
import com.example.habitstracker.exceptions.auth.IncorrectClaimsException;
import com.example.habitstracker.exceptions.auth.IncorrectJWTException;
import com.example.habitstracker.exceptions.auth.SkippedAuthorizationHeaderException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.MalformedJwtException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

/**
 * Сервис для работы с JWT
 */
@Component
public class TokenAuthenticationService {
    private final long tokenExpiry;
    private final String secret;
    private final String TOKEN_PREFIX = "Bearer";
    private final JwtParser jwtParser;

    @Autowired
    public TokenAuthenticationService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expire}") long expire) {
        this.secret = secret;
        jwtParser = Jwts.parserBuilder().setSigningKey(buildKey()).build();
        tokenExpiry = expire;
    }

    public void addAuthentication(HttpServletResponse response, String username, Long id) {
        var jwt = Jwts.builder()
                .setSubject(username)
                .claim(JWTClaims.USER_ID, id)
                .setExpiration(new Date(System.currentTimeMillis() + tokenExpiry))
                .signWith(buildKey())
                .compact();
        response.addHeader(HttpHeaders.AUTHORIZATION, TOKEN_PREFIX + " " + jwt);
    }

    // Синтаксический разбор токена
    public Authentication getAuthentication(HttpServletRequest request) {
        var token = getAuthorizationToken(request);
        token = getJWT(token);
        String username = extractUsername(token);
        return new UsernamePasswordAuthenticationToken(username, extractUserCredentials(token), List.of());
    }

    private Key buildKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private String getAuthorizationToken(HttpServletRequest request) {
        String token = request.getHeader(HttpHeaders.AUTHORIZATION);
        if(token == null)
            throw new SkippedAuthorizationHeaderException();
        return token;
    }

    private String getJWT(String token) {
        token = token.replace(TOKEN_PREFIX, "");
        if(token.isBlank())
            throw new EmptyAuthorizationHeaderException();
        return token;
    }

    private String extractUsername(String token) {
        String username;
        try {
            username = extractBody(token).getSubject();
        } catch (MalformedJwtException exception) {
            throw new IncorrectJWTException();
        }

        if (username == null)
            throw new IncorrectClaimsException();
        return username;
    }

    private Claims extractBody(String token) {
        return jwtParser.parseClaimsJws(token).getBody();
    }

    private UserCredentials extractUserCredentials(String token) {
        var body = extractBody(token);
        var username = body.getSubject();
        var id = Long.parseLong(body.get(JWTClaims.USER_ID).toString());
        return new UserCredentials(id, username);
    }
}
