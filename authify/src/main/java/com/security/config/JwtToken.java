package com.security.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class JwtToken {

    private static final long ACCESS_TOKEN_EXPIRATION = 15 * 60 * 1000;         // 15 mins
    private static final long REFRESH_TOKEN_EXPIRATION = 24 * 60 * 60 * 1000;   // 24 hours

    private static final String ACCESS_TOKEN_TYPE = "access_token";
    private static final String REFRESH_TOKEN_TYPE = "refresh_token";

    @Value("${jwt.secret.key}")
    private String secretKey;

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes());
    }



    // Generate Token
    public String generateToken(UserDetails userDetails, boolean isAccessToken) {

        long expTime=isAccessToken ? ACCESS_TOKEN_EXPIRATION : REFRESH_TOKEN_EXPIRATION;

        String tokenType=isAccessToken ? ACCESS_TOKEN_TYPE : REFRESH_TOKEN_TYPE;
        Map<String ,Object> claims=new HashMap<>();
        claims.put("typ",tokenType);
        String token = Jwts.builder()
                .setClaims(claims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expTime))
                .signWith(Keys.hmacShaKeyFor(secretKey.getBytes()), SignatureAlgorithm.HS256)
                .compact();
        return token;
    }

    // Get username from token

    // ✅ Extract email (subject)
    public String extractEmail(String token) {
        String username=Jwts.parser()
                .setSigningKey(secretKey.getBytes())
                .parseClaimsJws(token).getBody().getSubject();
        return username;
    }



    // validate Token

    public boolean validateToken(String token){

        if(this.isTokenExpired(token)){
            return false;
        }

        try {
            Jwts.parser().setSigningKey(secretKey.getBytes()).parseClaimsJws(token);
            return true;
        } catch(Exception e) {
            e.printStackTrace();
            return false;
        }

    }

    public boolean isTokenExpired(String token) {
        Date expiration = Jwts.parser()
                .setSigningKey(secretKey.getBytes())
                .parseClaimsJws(token).getBody().getExpiration();
        return expiration.before(new Date());

    }

    public boolean isRefreshToken(String token){
        Claims claims = Jwts.parser()
                .setSigningKey(secretKey.getBytes())
                .parseClaimsJws(token)
                .getBody();

        String tokenType = (String) claims.get("typ");
        if(tokenType==null) return false;
        return tokenType.equals(REFRESH_TOKEN_TYPE);
    }

    public boolean isAccessToken(String token){
        Claims claims = Jwts.parser()
                .setSigningKey(secretKey.getBytes())
                .parseClaimsJws(token)
                .getBody();

        String tokenType = (String) claims.get("typ");

        if(tokenType==null) return false;
        return tokenType.equals(ACCESS_TOKEN_TYPE);
    }

    // Generate only access token
    public String generateAccessToken(UserDetails userDetails) {
        return generateToken(userDetails, true);
    }

    // Overloaded validation method
    public boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractEmail(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }



}
