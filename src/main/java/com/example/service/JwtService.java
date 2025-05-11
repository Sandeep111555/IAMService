package com.example.service;

import com.example.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

@Service
public class JwtService {
    private final SecretKey secretKey;
    public JwtService(){
        try{
            //generate secure key for Hmac sha256 algo
            KeyGenerator keyGenerator = KeyGenerator.getInstance("HmacSHA256");
            secretKey = keyGenerator.generateKey();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
    //Generate JWT token using User object
    public String generateToken(User user){
        Map<String,String> claims = new HashMap<>();
        claims.put("firstName",user.getFirstName());
        claims.put("lastName",user.getLastName());
        return Jwts.builder()
                .claims()
                .add(claims)
                .subject(user.getUserName())
                .issuedAt(new Date((System.currentTimeMillis())))
                .expiration(new Date(System.currentTimeMillis() + 60 * 120 * 120))
                .and()
                .signWith(secretKey)
                .compact();
    }

    public String extractUserNameFromBearerToken(String authToken) {
        //we are a passing function as an argument for reusability
        return extractClaim(authToken, Claims::getSubject);
    }
    private <T> T extractClaim(String token, Function<Claims, T> claimResolver){
        Claims allClaims = extractAllClaims(token);
         return claimResolver.apply(allClaims);
    }
    private Claims extractAllClaims(String token){
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }    public boolean validateToken(String token, UserDetails userDetails){
        final String username = extractUserNameFromBearerToken(token);
        return Objects.nonNull(userDetails) && username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }
    private boolean isTokenExpired(String token){
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }
}
