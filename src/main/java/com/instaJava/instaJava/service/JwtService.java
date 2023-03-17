package com.instaJava.instaJava.service;

import java.security.Key;
import java.time.Clock;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JwtService {

	private final Clock clock;
	private static final String SECRET_KEY = "7638792F423F4428472B4B6250655368566D597133743677397A244326462948";

	public String extractUsername(String token) {
		return extractClaim(token,Claims::getSubject);
	}

	public <T> T extractClaim(String token, Function<Claims,T> claimsResolver) {
		final Claims claims = extractAllClaims(token);
		return claimsResolver.apply(claims);
	}
	
	public String generateToken(UserDetails userDetails) {
		return generateToken(new HashMap<>(),userDetails);
	}
	
	public String generateToken(Map<String,Object> extraClaims,UserDetails userDetails) {
		String accessToken = Jwts.builder()
				.setClaims(extraClaims)
				.setSubject(userDetails.getUsername())
				.setIssuedAt(new Date(clock.millis()))
				.setExpiration(new Date(clock.millis() + 10 * 60 * 1000 ))// 10 min //I have to test this.
				.signWith(getSignKey(), SignatureAlgorithm.HS256)
				.compact();
		return accessToken;
	}
	
	public String generateRefreshToken(UserDetails userDetails) {
		String refreshToken = Jwts.builder()
				.setSubject(userDetails.getUsername())
				.setExpiration(new Date(clock.millis() + 30 * 60 * 1000 ))// 30 min
				.signWith(getSignKey(), SignatureAlgorithm.HS256)
				.compact();
		return refreshToken;
	}
	
	public boolean isTokenValid(String token,UserDetails userDetails) {//we need userDetails to see if the tokens belongs to this user
		final String username = this.extractUsername(token);
		return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
	}
	
	private boolean isTokenExpired(String token) {
		//if expiration is before current time means that is expired and return true
		//If expiration is after current time means that is not expired and return false
		return extractExpiration(token).before(new Date(clock.millis()));
	}
	
	private Date extractExpiration(String token) {
		return extractClaim(token,Claims::getExpiration);
	}
	

	private Claims extractAllClaims(String token) {
		return Jwts.parserBuilder()
				.setSigningKey(getSignKey())
				.build()
				.parseClaimsJws(token)
				.getBody();
	}

	private Key getSignKey() {
		byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
		return Keys.hmacShaKeyFor(keyBytes);
	}
	
	
}
