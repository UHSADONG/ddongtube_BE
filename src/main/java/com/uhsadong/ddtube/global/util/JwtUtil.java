package com.uhsadong.ddtube.global.util;

import com.uhsadong.ddtube.domain.dto.UserSimpleDTO;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Base64;
import java.util.Date;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class JwtUtil {

    @Value("${jwt.access-expiration}")
    private long accessExpirationMillis;
    @Value("${jwt.secret}")
    private String secretKey;

    // 토큰 생성
    public String generateAccessToken(String userCode, String playlistCode, String userName) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + accessExpirationMillis);

        return Jwts.builder()
            .setSubject(userCode)
            .claim("playlistCode", playlistCode)
            .claim("userName", userName)
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .signWith(convertSecretToKey(secretKey))
            .compact();
    }

    public UserSimpleDTO getUserSimpleDataInJwt(String token) {
        return UserSimpleDTO.builder()
            .userCode(getUserCodeInJwt(token))
            .playlistCode(getPlaylistCodeInJwt(token))
            .userName(getUserNameInJwt(token))
            .build();
    }

    // 토큰에서 subject(email 등) 추출
    public String getUserCodeInJwt(String token) {
        return parseClaims(token).getSubject();
    }

    public String getUserNameInJwt(String token) {
        return parseClaims(token).get("userName", String.class);
    }

    public String getPlaylistCodeInJwt(String token) {
        return parseClaims(token).get("playlistCode", String.class);
    }

    // 토큰 유효성 검증
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.info("JWT expired: {}", String.valueOf(e));
        } catch (JwtException e) {
            log.info("Invalid JWT: {}", String.valueOf(e));
        }
        return false;
    }


    private Key convertSecretToKey(String secret) {
        byte[] keyBytes = Base64.getEncoder().encode(secret.getBytes());
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // Claims 파싱
    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
            .setSigningKey(convertSecretToKey(secretKey))
            .build()
            .parseClaimsJws(token)
            .getBody();
    }
}
