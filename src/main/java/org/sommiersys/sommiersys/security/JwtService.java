package org.sommiersys.sommiersys.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class JwtService {

    @Value("${security.jwt.secret-key}")
    private String secretKey;

    public String extractUsername(String token) {
        log.info("Extracting username from token");
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        log.info("Extracting claim from token");
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        try {
            log.info("Parsing JWT token");
            return Jwts
                    .parserBuilder()
                    .setSigningKey(getSignIngKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (JwtException e) {
            log.error("Failed to parse token: {}", e.getMessage());
            throw e;
        }
    }

    public Boolean isTokenValid(String token, String usernameFromFilter) {
        try {
            log.info("Validating token for user: {}", usernameFromFilter);
            final String username = extractUsername(token);
            return username.equals(usernameFromFilter) && !isTokenExpired(token);
        } catch (JwtException e) {
            log.error("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    public List<GrantedAuthority> extractRoles(String token) {
        log.info("Extracting roles from token");
        Claims claims = extractAllClaims(token);
        var roles = (List<String>) claims.get("authorities");
        log.info("Roles found: {}", roles);

        return roles.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
    }

    private boolean isTokenExpired(String token) {
        log.info("Checking if token is expired");
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Key getSignIngKey() {
        log.info("Decoding secret key");
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
